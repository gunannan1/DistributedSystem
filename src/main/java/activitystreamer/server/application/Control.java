package activitystreamer.server.application;

import activitystreamer.UILogAppender;
import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.networklayerhandlers.ServerInvalidHandler;
import activitystreamer.message.serverhandlers.*;
import activitystreamer.server.datalayer.*;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.IMessageConsumer;
import activitystreamer.server.networklayer.NetworkLayer;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.nio.ch.Net;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;

public class Control extends Thread implements IMessageConsumer {
	public static final Logger log = LogManager.getLogger("serverLogger");

	private static boolean term = false;
	//	private Listener listener; // CHANGE: from static to an instance variable
	private UIRefresher uiRefresher;
	//	private ArrayList<Connection> connections; // CHANGE: from static to an instance variable
//	private HashMap<String, User> userList; // <String, user Class>,
	private HashMap<MessageType, MessageHandler> handlerMap;
	private ServerTextFrame serverTextFrame;
	private NetworkLayer networkLayer;
	private DataLayer dataLayer;
	//	private String identifier;
//	private HashMap<String, ServerState> serverStateList;

	private boolean provideService;//TODO enhance: indicate whether the system is providing normal service

	protected static Control control = null;


	public static Control getInstance() {
		if (control == null) {
			control = new Control();
		}
		return control;
	}

	private Control() {
		// Should not provide services
		provideService = false;
		networkLayer = NetworkLayer.getNetworkLayer();
		dataLayer = DataLayer.getInstance();

		// initialize the request list for register and login
		UserRegisterHandler.registerLockHashMap = new HashMap<>();

		// updateOrInsert message handlers
		initialHandlers();

		// init consumers


		// connect to another remote server if remote host is provided, or just start listener to provide services
		if (Settings.getRemoteHostname() != null) {
			// if remote server host provided, connect to it and then wait for response to start listener.
			networkLayer.initiateConnection();
		} else {
			setProvideService(true);
			networkLayer.startListener();
			startUI();
		}
		start();
	}

//	public void startListener() {
//		// start a listener
//		try {
//			if(listener == null) {
//				listener = new Listener();
//			}
//			// Show UI for testing
//			startUI();
//		} catch (IOException e1) {
//			log.fatal("failed to startup a listening thread: " + e1);
//			System.exit(-1);
//		}
//	}


//	public void initiateConnection() {
//		// make a connection to another server if remote hostname is supplied
//		try {
//			String remoteHost = Settings.getRemoteHostname();
//			int remotePort = Settings.getRemotePort();
//			Socket s = new Socket(remoteHost, remotePort);
//			Connection c = outgoingConnection(s);
//			c.setServer(true);
//			c.setAuthed(false, remoteHost, remotePort);
////			c.setMain(true);
//			// Authen itself to remote server
//			c.sendAuthMsg(Settings.getSecret());
////			String serverRegister = MessageGenerator.authen(Settings.getSecret());
////			c.writeMsg(serverRegister);
////			identifier = Settings.getLocalHostname() + Settings.getLocalPort();
//		} catch (IOException e) {
//			log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
//			listener.setTerm(true);
//			System.exit(-1);
//		}
//
//	}

	private void initialHandlers() {
		this.handlerMap = new HashMap<>();
		this.handlerMap.put(MessageType.INVALID_MESSAGE, new ServerInvalidHandler());

		// Message from Clients
		this.handlerMap.put(MessageType.REGISTER, new UserRegisterHandler());
		this.handlerMap.put(MessageType.LOGIN, new UserLoginHandler());
		/* used for accept register result from data layer */
		this.handlerMap.put(MessageType.USER_REGISTER_RESULT, new RegisterResultHandler());

		this.handlerMap.put(MessageType.LOGOUT, new UserLogoutHandler());
		this.handlerMap.put(MessageType.ACTIVITY_MESSAGE, new ActivityRequestHandler());

		// Message from Servers
		this.handlerMap.put(MessageType.AUTHENTICATE, new ServerAuthenRequestHandler());
		this.handlerMap.put(MessageType.AUTHENTICATION_FAIL, new ServerAuthenFailedHandler());
		this.handlerMap.put(MessageType.AUTHENTICATION_SUCC, new ServerAuthenSuccHandler());


//		this.handlerMap.put(MessageType.ACTIVITY_BROADCAST, new ActivityBroadcastHandler());

		NetworkLayer.getNetworkLayer().registerConsumer(handlerMap.keySet(), this);
	}

	/*
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con, JsonObject json) {
		boolean isSucc = false;
		try {
			MessageType m = MessageType.valueOf(json.get("command").getAsString());
			MessageHandler h = handlerMap.get(m);
			if (h != null) {
				isSucc = h.processMessage(json, con);
			} else {
				log.error("Cannot find message handler for message type [{}]", m.name());
			}
			// refresh UI
			refreshUI();
		} catch (IllegalStateException e) {
			String info = String.format("[Control]Invalid message [%s] in ", json.toString());
			log.error(info);
			isSucc = false;
			String invalidMsg = MessageGenerator.invalid(info);
			con.writeMsg(invalidMsg);
		}
		return isSucc;
	}

	public synchronized boolean process(Connection con, String data) {
		try {
//			Control.log.debug("receive data {}", data);
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(data).getAsJsonObject();
			return process(con, json);
		} catch (IllegalStateException e) {
			String info = String.format("Invalid message [%s]", data);
			log.error(info);
			String invalidMsg = MessageGenerator.invalid(info);
			con.writeMsg(invalidMsg);
			return false;
		}
	}

	@Override
	public void run() {
		log.info("using schedule check period of " + Settings.getActivityCheckInterval() + " milliseconds");
		while (!term) {
			// do something with 5 second intervals in between
			try {
				scheduleActivityCheck();
				Thread.sleep(Settings.getActivityCheckInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
		}
		networkLayer.closeAllConnections();
	}

	private void scheduleActivityCheck() {
		ArrayList<Connection> connectionList = new ArrayList<>(networkLayer.getConnectionList());
		for (Connection conn : connectionList) {
			if (conn.isAuthedClient()) {
				UserRow user = conn.getUser();
				ActivityRow activityRow = DataLayer.getInstance().pendingActivity(user.getUsername());
				int isChange = 0;
				if (activityRow != null) {
					for (Activity activity : activityRow.getActivityList()) {
						if (!activity.isDelivered()) {
							JsonObject json = activity.toJson();
							json.addProperty("command", MessageType.ACTIVITY_BROADCAST.name());
							conn.sendActivityBroadcastMsg(json.toString());
							activity.setDelivered(true);
							isChange += 1;
						}
					}
				}
				if(isChange > 0) activityRow.notifyChange(null);
			}
		}
	}


	public boolean doActivity() {

		return false;
	}

	public final void setTerm(boolean t) {
		term = t;
		if(t) {
			DataLayer.getInstance().setTerm(true);
			NetworkLayer.getNetworkLayer().setTerm(true);
			uiRefresher.interrupt();
		}
	}


	public boolean isProvideService() {
		return provideService;
	}

	public void setProvideService(boolean provideService) {
		this.provideService = provideService;
	}


	//if any other server's loads are at less than the own server, return the id of server which has least loads
	//else return null
	public ServerRow findRedirectServer() {
		ServerRow serverMin = dataLayer.getMinLoadServer();
		if (networkLayer.getClientLoads() - serverMin.getLoad() >= 2) {
			return serverMin;
		}
		return null;
	}


	public void doRedirect(Connection connection, ServerRow server, String username) {
		connection.sendRedirectMsg(server.getIp(), server.getPort());
		Control.log.info(" user [{}] needs redirect", username);
		connection.closeCon();
		networkLayer.connectionClosed(connection);
	}

	public void startUI() {
		if (serverTextFrame == null) {
			serverTextFrame = new ServerTextFrame();
			UILogAppender.setTextArea(serverTextFrame.getLogArea());
			uiRefresher = new UIRefresher();
			uiRefresher.start();
		}
	}


	private void refreshUI() {
		if (serverTextFrame != null) {
			ArrayList<Connection> copyConn = new ArrayList<>(networkLayer.getConnectionList());
			this.serverTextFrame.setLoadArea(dataLayer.getServerStateList().values());
			this.serverTextFrame.setServerArea(copyConn);
			this.serverTextFrame.setRegisteredArea(dataLayer.getAllUsers().values());
			this.serverTextFrame.setLoginUserArea(DataLayer.getInstance().getConnectedUsers());
		}
	}

//	public void closeAll() {
//		uiRefresher.interrupt();
//		term = true;
//		DataLayer.getInstance().setTerm(true);
//		NetworkLayer.getNetworkLayer().setTerm(true);
//	}

	private class UIRefresher extends Thread {
		private boolean isRun;

		UIRefresher() {
			isRun = true;
		}

		public void setTerm() {
			isRun = false;
		}

		@Override
		public void run() {
			while (isRun) {
				refreshUI();
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					log.info("refresh ui thread ends");
				}
			}

		}
	}
}
