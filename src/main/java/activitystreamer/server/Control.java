package activitystreamer.server;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class Control extends Thread {
	public static final Logger log = LogManager.getLogger("serverLogger");

	private static boolean term = false;
	private static Listener listener; // why static
	private static ArrayList<Connection> connections;
	private HashMap<String, User> userList; // <String, user Class>,
	private HashMap<MessageType, MessageHandler> handlerMap;
//	private ServerTextFrame serverTextFrame;
	private String identifier;

	protected static Control control = null;


	public static Control getInstance() {
		if (control == null) {
			control = new Control();
			control.initialHandlers();
		}
		return control;
	}

	public Control() {
		// Initialize user list
		userList = new HashMap<>();

		// initialize the connections array
		connections = new ArrayList<Connection>();

		// initialize the identifier
		identifier = null;

		// connect to another remote server if remote host is provided, or just start listener to provide services
		if (Settings.getRemoteHostname() != null) {
			// if remote server host provided, connect to it and then wait for response to start listener.
			initiateConnection();
		} else {
			this.startListener();
		}
	}

	public void startListener() {
		// start a listener
		try {
			listener = new Listener();
//			serverTextFrame = new ServerTextFrame();
			identifier = listener.getSocketAdr();
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: " + e1);
			System.exit(-1);
		}
	}


	public void initiateConnection() {
		// make a connection to another server if remote hostname is supplied
		try {
			Socket s = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
			Connection c = outgoingConnection(s);
			// Authen itself to remote server
			String serverRegister = MessageGenerator.generateAuthen(Settings.getSecret());
			c.writeMsg(serverRegister);
			startListener();
			identifier = Settings.socketAddress(s);
		} catch (IOException e) {
			log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
			System.exit(-1);
		}

	}

	// TODO initialize message handlers
	private void initialHandlers() {
		this.handlerMap = new HashMap<>();
		this.handlerMap.put(MessageType.INVALID_MESSAGE, new ServerInvalidHandler(this));

		// Message from Clients
		this.handlerMap.put(MessageType.REGISTER, new UserRegisterHandler(this));
		this.handlerMap.put(MessageType.LOGIN, new UserLoginHandler(this));

		this.handlerMap.put(MessageType.LOGOUT, new UserLogoutHandler(this));
		this.handlerMap.put(MessageType.ACTIVITY_MESSAGE, new ActivityRequestHandler(this));

		// Message from Servers
		this.handlerMap.put(MessageType.AUTHENTICATE, new ServerAuthenRequestHandler(this));
		this.handlerMap.put(MessageType.AUTHENTICATION_FAIL, new ServerAuthenFailedHandler(this));
//		this.handlerMap.put(MessageType.AUTHENTICATE_SUCCESS, new ServerAuthenSuccHandler(this));

		this.handlerMap.put(MessageType.SERVER_ANNOUNCE, new ServerAnnounceHandler(this));
		this.handlerMap.put(MessageType.ACTIVITY_BROADCAST, new ActivityBroadcastHandler(this));

		this.handlerMap.put(MessageType.USER_ENQUIRY, new UserEnqieryHandler(this));

		this.handlerMap.put(MessageType.LOCK_REQUEST, new LockRequestHandler(this));
		this.handlerMap.put(MessageType.LOCK_ALLOWED, new LockAllowedHandler(this));
		this.handlerMap.put(MessageType.LOCK_DENIED, new LockDeniedHandler(this));

	}

	/*
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con, String msg) {
		JsonParser parser = new JsonParser();
		boolean isSucc = false ;
		try {
			JsonObject json = parser.parse(msg).getAsJsonObject();
			MessageType m = MessageType.valueOf(json.get("command").getAsString());
			MessageHandler h = handlerMap.get(m);
			if (h != null) {
				isSucc = h.processMessage(json, con);
			} else {
				log.error("Cannot find message handler for message type '{}'", m.name());
			}
		} catch (IllegalStateException e) {
			String info = String.format("Invalid message '%s'", msg);
			log.error(info);
			isSucc = false;
			String invalidMsg = MessageGenerator.generateInvalid(info);
			con.writeMsg(invalidMsg);

		}
		return isSucc;
	}

	/*
	 * The connection has been closed by the other party.
	 */
	public synchronized void connectionClosed(Connection con) {
		if (!term){
			log.info("Remove connection {} from list",con.getSocket().getRemoteSocketAddress());
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
		return c;

	}

	//TODO handle boradcast task
	public synchronized void broadcastToAll(String msg,Connection from) {
		for(Connection c:connections){
			if(c != from & (c.isAuthedServer() || c.isAuthedClient())) {
				c.writeMsg(msg);
			}
		}
	}

	public synchronized void broadcastToServer(String msg,Connection from) {
		for(Connection c:connections){
			if(c.isAuthedServer() && c != from) {
				c.writeMsg(msg);
			}
		}
	}

	// broadcastToAll lock request
	public  void broadcastLockRequest(User u,Connection from) {
		String lockRequest = MessageGenerator.generateLockRequest(u.getUsername(),u.getSecret(),identifier);
		broadcastToAll(lockRequest,from);

	}

	public  void broadcastActivity(Activity a,Connection from) {

	}

	public  void broadcastEnquiry(User u,Connection from) {
		String enquiryRequest = MessageGenerator.generateUserEnqueryRequest(u.getUsername(),u.getSecret(),identifier);
		broadcastToAll(enquiryRequest,from);

	}



	//TODO check user exists
	public synchronized boolean checkUserExists(String username) {
		return userList.containsKey(username);
	}

	//TODO add user
	public synchronized boolean addUser(User user) {
		if(!checkUserExists(user.getUsername())){
			userList.put(user.getUsername(),user);
			return true;
		}else{
			log.info("User '{}' exists, reject register.",user.getUsername());
			return false;
		}
	}

	public User getUser(String username, String secret){
		User u =  userList.get(username);
		if (u.getUsername().equals(secret)){
			return u;
		}else{
			return null;
		}
	}

	@Override
	public void run() {
		log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
		while (!term) {
			// do something with 5 second intervals in between
			// TODO boradcast SERVER_ANNOUNCE
			try {
				Thread.sleep(Settings.getActivityInterval());
			} catch (InterruptedException e) {
				log.info("received an interrupt, system is shutting down");
				break;
			}
			if (!term) {
				log.debug("doing activity");
				term = doActivity();
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

	public boolean doActivity() {
		return false;
	}

	public final void setTerm(boolean t) {
		term = t;
	}

	public final ArrayList<Connection> getConnections() {
		return connections;
	}


	public int getClientLoads() {
		int load = 0;
		for(Connection c:connections){
			if(c.isAuthedClient()){
				load++;
			}
		}
		return load;
	}

	public int getServerLoads(Connection exclude) {
		int load = 0;
		for(Connection c:connections){
			if(exclude !=c && c.isAuthedServer()){
				load++;
			}
		}
		return load;
	}

	public String getIdentifier(){
		return identifier;
	}

	//TODO UI information refresh
	public void refreshUnauthCon(){

	}

	public void refreshUserInfo(){
		String html = "<table class='table table-bordered'> <thead>%s</thead><tbody>%S</tbody>";
		StringJoiner sj = new StringJoiner("");
//		sj.add(User.tableHeader());
		for(Map.Entry<String,User>e:userList.entrySet()){
			sj.add(e.getValue().toString());
		}
//		serverTextFrame.setUserAreaText(String.format(html,User.tableHeader(),sj.toString()));

	}

	public void refreshServerInfo(){

	}

	public void refreshUI(){
		refreshServerInfo();
		refreshUnauthCon();
		refreshUserInfo();
	}

	private class UIRefresher extends Thread{
		@Override
		public void run() {
			refreshUI();
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}
}
