package activitystreamer.server;

import activitystreamer.UILogAppender;
import activitystreamer.message.Activity;
import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.serverhandlers.*;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class Control extends Thread {
	public static final Logger log = LogManager.getLogger("serverLogger");

	private static boolean term = false;
	private Listener listener; // CHANGE: from static to an instance variable
	private UIRefresher uiRefresher;
	private ArrayList<Connection> connections; // CHANGE: from static to an instance variable
	private HashMap<String, User> userList; // <String, user Class>,
	private HashMap<MessageType, MessageHandler> handlerMap;
	private ServerTextFrame serverTextFrame;
	//	private String identifier;
	private HashMap<String, ServerState> serverStateList;
	private HashMap<String,Integer> activitySeq; //record activity sequence form client
	private HashMap<String,Queue<JsonObject>> activityQueue_Send;

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

		// Initialize user list
		userList = new HashMap<>();

		// initialize the connections array
		connections = new ArrayList<Connection>();

		// initialize the serverStateList
		serverStateList = new HashMap<>();

		// initialize the request list for register and login
		UserRegisterHandler.registerLockHashMap = new HashMap<>();

		// initialize the activitySeq
		activitySeq=new HashMap<>();

		// initialize the activityQueue
		activityQueue_Send=new HashMap<>();

		// add message handlers
		initialHandlers();

		// connect to another remote server if remote host is provided, or just start listener to provide services
		if (Settings.getRemoteHostname() != null) {
			// if remote server host provided, connect to it and then wait for response to start listener.
			initiateConnection();
		} else {
			setProvideService(true);
			this.startListener();
		}
		start();
	}

	public void startListener() {
		// start a listener
		try {
			if(listener == null) {
				listener = new Listener();
			}
			// Show UI for testing
			startUI();
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: " + e1);
			System.exit(-1);
		}
	}


	public void initiateConnection() {
		// make a connection to another server if remote hostname is supplied
		try {
			String remoteHost = Settings.getRemoteHostname();
			int remotePort = Settings.getRemotePort();
			Socket s = new Socket(remoteHost, remotePort);
			Connection c = outgoingConnection(s);
			c.setServer(true);
			c.setAuthed(false, remoteHost, remotePort);
//			c.setMain(true);
			// Authen itself to remote server
			c.sendAuthMsg(Settings.getSecret());
//			String serverRegister = MessageGenerator.authen(Settings.getSecret());
//			c.writeMsg(serverRegister);
//			identifier = Settings.getLocalHostname() + Settings.getLocalPort();
		} catch (IOException e) {
			log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
			listener.setTerm(true);
			System.exit(-1);
		}

	}

	private void initialHandlers() {
		this.handlerMap = new HashMap<>();
		this.handlerMap.put(MessageType.INVALID_MESSAGE, new ServerInvalidHandler(this));

		// Message from Clients
		this.handlerMap.put(MessageType.REGISTER, new UserRegisterHandler(this));
		this.handlerMap.put(MessageType.LOGIN, new UserLoginHandler(this));
		/* used for sync registered user list */
		this.handlerMap.put(MessageType.USER_REGISTER_RESULT,new RegisterResultHandler(this));

		this.handlerMap.put(MessageType.LOGOUT, new UserLogoutHandler(this));
		this.handlerMap.put(MessageType.ACTIVITY_MESSAGE, new ActivityRequestHandler(this));

		// Message from Servers
		this.handlerMap.put(MessageType.AUTHENTICATE, new ServerAuthenRequestHandler(this));
		this.handlerMap.put(MessageType.AUTHENTICATION_FAIL, new ServerAuthenFailedHandler(this));
		this.handlerMap.put(MessageType.AUTHENTICATION_SUCC, new ServerAuthenSuccHandler(this));

		this.handlerMap.put(MessageType.SERVER_ANNOUNCE, new ServerAnnounceHandler(this));
		this.handlerMap.put(MessageType.ACTIVITY_BROADCAST, new ActivityBroadcastHandler(this));

		// Messages for Register
		this.handlerMap.put(MessageType.LOCK_REQUEST, new LockRequestHandler(this));
		this.handlerMap.put(MessageType.LOCK_ALLOWED, new LockAllowedHandler(this));
		this.handlerMap.put(MessageType.LOCK_DENIED, new LockDeniedHandler(this));

		// Backup subsystem for High Available
		this.handlerMap.put(MessageType.BACKUP_LIST, new ServerBackupListHandler(this));

	}

	/*
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con, String msg) {
		Control.log.debug("received message [{}] from [{}]", msg, Settings.socketAddress(con.getSocket()));
		JsonParser parser = new JsonParser();
		boolean isSucc = false;
		try {
			JsonObject json = parser.parse(msg).getAsJsonObject();
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
			String info = String.format("Invalid message [%s]", msg);
			log.error(info);
			isSucc = false;
			String invalidMsg = MessageGenerator.invalid(info);
			con.writeMsg(invalidMsg);

		}
		return isSucc;
	}


	/*
	 * The connection has been closed by the other party.
	 */
	public synchronized void connectionClosed(Connection con) {
		if (!term) {
			log.info("Remove connection {} from list", con.getSocket().getRemoteSocketAddress());
			connections.remove(con);
		}
	}

	/*
	 * A new incoming connection has been established, and a reference is returned to it
	 */
	public synchronized Connection incomingConnection(Socket s) throws IOException {
		log.debug("incomming connection: " + Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
		return c;

	}

	/*
	 * A new outgoing connection has been established, and a reference is returned to it
	 */
	public synchronized Connection outgoingConnection(Socket s) throws IOException {
		log.debug("outgoing connection: " + Settings.socketAddress(s));
		Connection c = new Connection(s);
		connections.add(c);
//		c.sendAuthMsg(Settings.getSecret());
		return c;

	}

	public synchronized void broadcastToServers(String msg, Connection from) {
		for (Connection c : connections) {
			if (c != from && c.isAuthedServer()) {
				c.writeMsg(msg);
			}
		}
	}


	// broadcastToAll lock request
	public void broadcastLockRequest(User u, Connection from) {
		String lockRequest = MessageGenerator.lockRequest(u.getUsername(), u.getSecret());
		broadcastToServers(lockRequest, from);

	}


	public void broadcastEnquiry(User u, Connection from) {
		String enquiryRequest = MessageGenerator.lockRequest(u.getUsername(), u.getSecret());
		broadcastToServers(enquiryRequest, from);

	}


	public synchronized User checkUserExists(String username) {
		return userList.get(username);
	}

	public synchronized boolean addUser(User user) {
		if (checkUserExists(user.getUsername()) == null) {
			userList.put(user.getUsername(), user);
			        Comparator<JsonObject> comparator = (JsonObject j1, JsonObject j2)
                -> (j1.get("sequence").getAsInt()-j1.get("sequence").getAsInt());
			activityQueue_Send.put(user.getUsername(),new PriorityQueue<>(comparator));

			activitySeq.put(user.getUsername(),0);
			return true;
		} else {
			log.info("User [{}] exists, reject register.", user.getUsername());
			return false;
		}
	}

	public User getUser(String username) {
		return userList.get(username);
	}

	public HashMap<String, User> getUserList() {
		return userList;
	}

	public void setUserList(HashMap<String, User> userList) {
		this.userList = userList;
	}

	public HashMap<String,Integer> getActivitySeqMap() { return activitySeq; }

	public int getActivitySeq(String username){
		return activitySeq.get(username);
	}

	public void setActivitySeq(HashMap<String, Integer> activitySeq) {
		this.activitySeq = activitySeq;
	}

	public void updateActivitySeq(String username){
		activitySeq.put(username,activitySeq.get(username)+1);
	}

	public HashMap<String, Queue<JsonObject>> getActivityQueue_Send() {
		return activityQueue_Send;
	}



	@Override
	public void run() {
		log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
		while (!term) {
			// do something with 5 second intervals in between
			try {
				maintainServerState(Settings.getServerId(), Settings.getLocalHostname(),
						getClientLoads(), Settings.getLocalPort());
				sendServerAnnounce();
				sendBackupList();
				cleanServerStatusList();
				Thread.sleep(Settings.getActivityInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
		}
		log.info("closing " + connections.size() + " connections");
		// clean up
		for (Connection connection : connections) {
			connection.closeCon();
		}
		listener.setTerm(true);
		//TODO close All other threads.
	}

	private void sendServerAnnounce() {
		for (Connection c : connections) {
			if (c.isAuthedServer()) {
				c.sendAnnounceMsg(Settings.getServerId(), this.getClientLoads(),
						Settings.getLocalHostname(), Settings.getLocalPort());
			}
		}
	}

	/* High avaliable for project 2 */
	private void sendBackupList() {
		String msg = MessageGenerator.backupList(connections);
		for (Connection c : connections) {
			if (c.isAuthedServer() || c.isAuthedClient()) {
				c.writeMsg(msg);
			}
		}
	}

	public boolean doActivity() {

		return false;
	}

	public final void setTerm(boolean t) {
		term = t;
		listener.setTerm(true);
		uiRefresher.interrupt();
	}

//	public boolean isOutOfService(){
//		if(!control.isProvideService()){
//			String info = String.format("This server is temporarily out of service now, please try to connect later");
//			Control.log.info(info);
//			return true;
//		}
//		return false;
//	}

	public boolean isProvideService() {
		return provideService;
	}

	public void setProvideService(boolean provideService) {
		this.provideService = provideService;
	}

	public final ArrayList<Connection> getConnections() {
		return connections;
	}


	private int getClientLoads() {
		int load = 0;
		for (Connection c : connections) {
			if (c.isAuthedClient()) {
				load++;
			}
		}
		return load;
	}

	public int getServerLoads(Connection exclude) {
		int load = 0;
		for (Connection c : connections) {
			if (exclude != c && c.isAuthedServer()) {
				load++;
			}
		}
		return load;
	}


	public void maintainServerState(String id, String host, int load, int port) {
		if (serverStateList.containsKey(id)) {
			ServerState s = serverStateList.get(id);
			s.setLoad(load);
		} else {
			serverStateList.put(id, new ServerState(port, load, host, id));
		}
	}

	// If the load info of a server is not updated for more than 20 sec, then remove it.
	private void cleanServerStatusList(){
		for(String id : serverStateList.keySet()) {
			if((Calendar.getInstance().getTime().getTime()
					- serverStateList.get(id).getUpdateTime().getTime())/1000 > 20 ){
				serverStateList.remove(id);
			}
		}
	}

	public HashMap<String, ServerState> getServerStateList() {
		return serverStateList;
	}

	//if any other server's loads are at less than the own server, return the id of server which has least loads
	//else return null
	public String findRedirectServer() {
		int minLoad = this.getClientLoads();
		String minLoadServerId = null;
		for (String id : serverStateList.keySet()) {
			if (serverStateList.get(id).getLoad() < minLoad) {
				minLoad = serverStateList.get(id).getLoad();
				minLoadServerId = id;
			}
		}
		if (this.getClientLoads() - minLoad >= 2) {
			return minLoadServerId;
		}
		return null;
	}



	public void doRedirect(Connection connection, String id, String username) {
		connection.sendRedirectMsg(this.getServerStateList().get(id).getHost(),
				this.getServerStateList().get(id).getPort());
		Control.log.info(" user [{}] needs redirect", username);
		connection.closeCon();
		control.connectionClosed(connection);
//		UserLoginHandler.enquiryRequestHashmap.remove(username);
	}

	// UI information refresh
	public ServerTextFrame getServerTextFrame() {
		return serverTextFrame;
	}

	private void startUI() {
		serverTextFrame = new ServerTextFrame();
		UILogAppender.setTextArea(serverTextFrame.getLogArea());
		uiRefresher = new UIRefresher();
		uiRefresher.start();
	}


	private void refreshUI() {
		if (serverTextFrame != null) {
			ArrayList<Connection> copyConn = new ArrayList<>(connections);
			this.serverTextFrame.setLoadArea(serverStateList.values());
			this.serverTextFrame.setServerArea(copyConn);
			this.serverTextFrame.setRegisteredArea(userList.values());
			this.serverTextFrame.setLoginUserArea(copyConn);
		}
	}

	public void closeAll() {
		listener.interrupt();
		uiRefresher.interrupt();
		term = true;

	}

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
