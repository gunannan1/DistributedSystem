package activitystreamer.client;

import activitystreamer.message.Activity;
import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.clienthandlers.*;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ClientSkeleton extends Thread {
	public static final Logger log = LogManager.getLogger("clientLogger");
	private static ClientSkeleton clientSolution;
	private ClientTextFrame textFrame;
	private Socket s = null;
	private HashMap<MessageType, MessageHandler> handlerMap;

	BufferedReader inreader;
	BufferedWriter outwriter;
//	private String serverId;


	public static ClientSkeleton getInstance() throws IOException {
		if (clientSolution == null) {
			clientSolution = new ClientSkeleton();
			clientSolution.initMessageHandlers();
		}
		return clientSolution;
	}

	public ClientSkeleton() throws IOException {

		// TODO initialise socket

		this.s = connectToServer(Settings.getRemoteHostname(), Settings.getRemotePort());
		this.inreader = new BufferedReader(new InputStreamReader(new DataInputStream(this.s.getInputStream())));
		this.outwriter = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
		start();

	}

	private void initMessageHandlers() {
		// Initialize handlers for messages that client may receive
		clientSolution.handlerMap = new HashMap<>();

		clientSolution.handlerMap.put(MessageType.LOGIN_FAILED, new LoginFailedHandler(this));
		clientSolution.handlerMap.put(MessageType.LOGIN_SUCCESS, new LoginSuccHandler(this));
		clientSolution.handlerMap.put(MessageType.REDIRECT,new RedirectHandler(this));
		clientSolution.handlerMap.put(MessageType.REGISTER_FAILED, new RegisterFailedHandler(this));
		clientSolution.handlerMap.put(MessageType.REGISTER_SUCCESS,new RegisterSuccHandler(this));
		clientSolution.handlerMap.put(MessageType.AUTHENTICATION_FAIL, new ClientAuthenFailedHandler(this));
		clientSolution.handlerMap.put(MessageType.INVALID_MESSAGE, new ClientInvalidHandler(this));

	}

	// TODO estimate connection
	private Socket connectToServer(String host, int port) {
		try {
			if (this.s == null) {
				return new Socket(host, port);
			}
			return this.s;
		} catch (IOException e) {
			log.error("Cannot connect to server {} via port {}", host, port);
		}

		return null;
	}


	public void startUI() {
		if(textFrame != null) {
			textFrame = new ClientTextFrame();
			UILogAppender.setTextArea(this.textFrame.getLogTextArea());
		}
	}


	public void disconnect() {
		// TODO close socket, close TextFrame
		try {
			sendLogoutMsg();
			s.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//client receive thread
	public void run() {
		try {
			String data;
			while ((data = inreader.readLine()) != null) {
				try {
					log.debug("Receive data {}", data);
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(data).getAsJsonObject();
					String command = json.get("command").getAsString();
					MessageType commandType = MessageType.valueOf(command);
					MessageHandler h = this.handlerMap.get(commandType);
					if (h != null) {
						h.processMessage(json, null);
					} else {
						log.error("No hander for message:{}", command);
					}
				} catch (IllegalStateException e) {
					String info = String.format("Invalid message '%s'", data);
					log.error(info);
					this.sendInvalidMsg(info);
					this.disconnect();
				}
			}
			log.info("Connection" + Settings.socketAddress(s) + " closed by remote server" );
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
			log.error("Connection" + Settings.socketAddress(s) + " closed with exception " + e);
			this.disconnect();
		}
	}

	public synchronized void writeMsg(String info){
		try {
			this.outwriter.write(info);
			this.outwriter.newLine();
			this.outwriter.flush();

		} catch (IOException e) {
			log.error("Error sending message {}",info);
			log.error("Client exists {}",info);
			this.disconnect();
		}

	}

	public void sendRegisterMsg() {
		log.info("send register to server with user={} secret={}",Settings.getUsername(), Settings.getSecret());
		String registerStr = MessageGenerator.generateRegister(Settings.getUsername(),Settings.getSecret());
		this.writeMsg(registerStr);
	}

	public void sendLoginMsg() {
		log.info("send login to server with user={} secret={}",Settings.getUsername(), Settings.getSecret());
		String loginStr = MessageGenerator.generateLogin(Settings.getUsername(),Settings.getSecret());
		this.writeMsg(loginStr);
	}

	public void sendAnonymousLoginMsg() {
		log.info("send register to server with user={} secret=N/A","anonymous");
		String anonymouStr = MessageGenerator.generateAnonymousLogin("anonymous");
		this.writeMsg(anonymouStr);
	}

	public void sendLogoutMsg() {
		log.info("send logout to server");
		String logoutMsg = MessageGenerator.generateLogout();
		this.writeMsg(logoutMsg);
	}
	public void sendInvalidMsg(String info) {
		log.info("send invalid message to server with info={}",info);
		String invalidStr = MessageGenerator.generateInvalid(info);
		this.writeMsg(invalidStr);
	}

	@SuppressWarnings("unchecked")
	public void sendActivityObject(Activity activityObj) {
		log.info("send activity to server with activity={}",activityObj.toJsonString());
			String actStr = MessageGenerator.generateActMessage(Settings.getUsername(),Settings.getSecret(),activityObj);
			this.writeMsg(actStr);

	}

}