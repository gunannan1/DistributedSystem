package activitystreamer.server;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.serverhandlers.ServerFailedMessageHandler;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Control extends Thread {
	private static final Logger log = LogManager.getLogger();

	private static boolean term = false;
	private static Listener listener; // why static
	private Connection parentServer;
	private boolean isAuthed;
	private static ArrayList<Connection> connections;
	private HashMap<String, User> userList; // <username, user Class>,  please note that anonymous users will not be stored here
	private HashMap<MessageType, MessageHandler> handlerMap;


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

		// connect to another remote server if remote host is provided, or just start listener to provide services
		if (Settings.getRemoteHostname() != null) {
			// if remote server host provided, connect to it and then wait for response to start listener.
			initiateConnection();
		}else{
			this.startListener();
		}


	}

	public void startListener(){
		// start a listener
		try {
			listener = new Listener();
		} catch (IOException e1) {
			log.fatal("failed to startup a listening thread: " + e1);
			System.exit(-1);
		}
	}

	// TODO initialize message handlers
	private void initialHandlers() {
		this.handlerMap = new HashMap<>();
		this.handlerMap.put(MessageType.INVALID_MESSAGE, new ServerFailedMessageHandler(this));

	}


	public void initiateConnection() {
		// make a connection to another server if remote hostname is supplied
			try {
				Connection c = outgoingConnection(new Socket(Settings.getRemoteHostname(), Settings.getRemotePort()));
				// Authen itself to remote server
				String serverRegister = MessageGenerator.generateAuthen(MessageType.AUTHENTICATE, Settings.getSecret());
				c.writeMsg(serverRegister);
			} catch (IOException e) {
				log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
				System.exit(-1);
			}

	}

	/*
	 * Processing incoming messages from the connection.
	 * Return true if the connection should close.
	 */
	public synchronized boolean process(Connection con, String msg) {
		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(msg).getAsJsonObject();
		MessageType m = MessageType.valueOf(json.get("command").getAsString());
		MessageHandler h = handlerMap.get(m);
		if (h != null) {
			return h.processMessage(json, con);
		} else {
			log.error("Cannot find message handler for message type '{}'", m.name());
			return false;
		}

	}

	/*
	 * The connection has been closed by the other party.
	 */
	public synchronized void connectionClosed(Connection con) {
		if (!term) connections.remove(con);
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
	public synchronized void broadcast() {

	}

	//TODO check user exists
	public synchronized boolean checkUserExists(String username) {
		return userList.containsKey(username);
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

	public synchronized void addTask() {

	}
}
