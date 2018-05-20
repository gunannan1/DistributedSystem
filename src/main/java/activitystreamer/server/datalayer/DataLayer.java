package activitystreamer.server.datalayer;

import activitystreamer.message.datasynchandlers.*;
import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.serverhandlers.UserRegisterHandler;
import activitystreamer.server.application.Control;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.IMessageConsumer;
import activitystreamer.server.networklayer.NetworkLayer;
import activitystreamer.util.Settings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class DataLayer extends Thread implements IMessageConsumer {
	public static final Logger log = Control.log;
	private static DataLayer dataLayer;
	private UserTable userTable;
	private ServerTable serverTable;
	private ActivityTable activityTable;
	private HashMap<MessageType, MessageHandler> handlerMap;
	private boolean term;

	public static DataLayer getInstance() {
		if (DataLayer.dataLayer != null) {
			return DataLayer.dataLayer;
		} else {
			DataLayer.dataLayer = new DataLayer();
			DataLayer.dataLayer.start();
			return DataLayer.dataLayer;
		}
	}

	private DataLayer() {
		userTable = new UserTable();
		serverTable = new ServerTable();
		handlerMap = new HashMap<>();
		activityTable = new ActivityTable();
		term = false;
		initialHandlers();
	}

	private void initialHandlers() {
		this.handlerMap.put(MessageType.SERVER_ANNOUNCE, new ServerAnnounceHandler());
		this.handlerMap.put(MessageType.LOCK_REQUEST, new LockRequestHandler());
		this.handlerMap.put(MessageType.LOCK_ALLOWED, new LockAllowedHandler());
		this.handlerMap.put(MessageType.LOCK_DENIED, new LockDeniedHandler());
		this.handlerMap.put(MessageType.USER_UPDATE, new UserUpdateHandler());
		this.handlerMap.put(MessageType.USER_SYNC, new UserSyncHandler());
		this.handlerMap.put(MessageType.ACTIVITY_BROADCAST, new ActivityBroadcastHandler());

		NetworkLayer.getNetworkLayer().registerConsumer(handlerMap.keySet(), this);
	}

	@Override
	public boolean process(Connection con, JsonObject json) {
		boolean isSucc = false;
		try {
			MessageType m = MessageType.valueOf(json.get("command").getAsString());
			MessageHandler h = handlerMap.get(m);
			if (h != null) {
				isSucc = h.processMessage(json, con);
			} else {
				log.info("[DataLayer]Ignore message type [{}]", m.name());
			}
		} catch (IllegalStateException e) {
			String info = String.format("Invalid message [%s]", json.toString());
			log.error(info);
			isSucc = false;
			String invalidMsg = MessageGenerator.invalid(info);
			con.writeMsg(invalidMsg);
		}
		return isSucc;
	}

	@Override
	public void run() {
		log.info("using announce interval period of " + Settings.getAnnounceInterval() + " milliseconds");
		while (!term) {
			// do something with 5 second intervals in between
			try {
				updateCurrentLoad();
				Thread.sleep(Settings.getAnnounceInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
		}
	}

	public void setTerm(boolean term) {
		this.term = term;
	}

	private void updateCurrentLoad() {
		ServerRow info = DataLayer.getInstance().updateOrInsert(
				new ServerRow(
						Settings.getServerId(),
						NetworkLayer.getNetworkLayer().getClientLoads(),
						Settings.getLocalHostname(),
						Settings.getLocalPort())
		);
		info.notifyChange();
	}

	/************************************************************************************************************************/
	/* Server related API */
	public ServerRow getMinLoadServer() {
		return serverTable.getMinLoadServer();
	}

	public void maintainServerState(String id, String host, int load, int port) {
		ServerRow newRow = new ServerRow(id, load, host, port);
		serverTable.updateOrInsert(newRow);
	}

	public HashMap<String, ServerRow> getServerStateList() {
		return serverTable.getAll();
	}

	public ServerRow updateOrInsert(ServerRow server) {
		return serverTable.updateOrInsert(server);
	}


	/************************************************************************************************************************/
	/* User Related API */
	public UserRow updateOrInsert(UserRow user) {
		return userTable.updateOrInsert(user);
	}


	public UserRow getUserByName(String username) {
		return userTable.selectById(username);

	}

	public BroadcastResult.REGISTER_RESULT registerUser(String username, String secret, Connection from) {

		// 2.1 check if username under register process( in the register list but not approved)
		if (UserRegisterHandler.registerLockHashMap.containsKey(username)) {
			return BroadcastResult.REGISTER_RESULT.FAIL_UNDER_REGISTER;
		}

		// 2.1.1 check if any remote servers exists
		// TODO a time-out limition should be set
		int serverLoads = NetworkLayer.getNetworkLayer().getServerLoads(null);
		if (serverLoads > 0) {
			BroadcastResult lockResult = new BroadcastResult(from, serverLoads, new UserRow(username, secret));
			UserRegisterHandler.registerLockHashMap.put(username, lockResult);
			// broadcastToAll lock request and then waiting for lock_allow & lock_denied, this register process will be handled by LockAllowedHandler & LockDeniedHandler
			String lockRequest = MessageGenerator.lockRequest(username, secret);
			NetworkLayer.getNetworkLayer().broadcastToServers(lockRequest, from);
			return BroadcastResult.REGISTER_RESULT.PROCESSING;
		}

		// register successfully if no above condision
		DataLayer.log.info("No additional server connected, send REGISTER_SUCC for user:{} ", username);
		DataLayer.log.info("Add user {} into local register user list", username);
		UserRow newUser = new UserRow(username, secret);
		DataLayer.getInstance().updateOrInsert(newUser);
		return BroadcastResult.REGISTER_RESULT.SUCC;
	}

	public HashMap<String, UserRow> getAllUsers() {
		return userTable.getAll();
	}

	public ArrayList<UserRow> getConnectedUsers() {
		return userTable.connectedUserList();
	}

	public void syncAllUserData(JsonArray json) throws Exception {
		for (JsonElement je : json) {
			UserRow userRow = new UserRow(je.getAsJsonObject());
			userTable.updateOrInsert(userRow);
		}
	}

	public void markUserOnline(String username, boolean online) {
		userTable.markUserOnline(username, online);
	}

	/************************************************************************************************************************/
	/* Activity Related API */
	public void insertActivity(Activity activity, Connection from) {
		activityTable.insertActivity(activity);
		JsonObject actJson = activity.toJson();
		actJson.addProperty("command", MessageType.ACTIVITY_BROADCAST.name());
		NetworkLayer.getNetworkLayer().broadcastToServers(actJson.toString(), from);
	}

	public ActivityRow pendingActivity(String name) {
		return activityTable.selectById(name);
	}
}
