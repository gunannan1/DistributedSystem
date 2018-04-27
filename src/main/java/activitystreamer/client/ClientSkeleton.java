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
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class ClientSkeleton extends Thread {
	public static final Logger log = LogManager.getLogger("clientLogger");
	private static ClientSkeleton clientSolution;
	private ClientTextFrame textFrame;
	private Socket s = null;
	private HashMap<MessageType, MessageHandler> handlerMap;
	private boolean term ;

	BufferedReader inreader;
	BufferedWriter outwriter;
//	private String serverId;


	public static ClientSkeleton getInstance(){
		if (clientSolution == null) {
			clientSolution = new ClientSkeleton();
			clientSolution.initMessageHandlers();
		}
		return clientSolution;
	}

	public ClientSkeleton() {

		// TODO initialise socket

		this.s = connectToServer(Settings.getRemoteHostname(), Settings.getRemotePort());
		try {
			this.inreader = new BufferedReader(new InputStreamReader(new DataInputStream(this.s.getInputStream())));
			this.outwriter = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
			start();
		} catch (IOException e) {
			log.error("Cannot connect to remote server {}", Settings.getRemoteHostname());
		}



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
		//add Activity Broadcast
		clientSolution.handlerMap.put(MessageType.ACTIVITY_BROADCAST,new ClientActivityBroadcastHandler(this));

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
			System.exit(1);
		}

		return null;
	}
	public void redirectToServer(String host, int port)
	{
        try {
			this.s.close();
			this.s = new Socket(host, port);
			this.inreader = new BufferedReader(new InputStreamReader(new DataInputStream(this.s.getInputStream())));
			this.outwriter = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
        	log.error("Cannot redirect to server{} via port{}",host, port);
			e.printStackTrace();
			System.exit(1);
		}

	}


	public void startUI(String str) {
		if(textFrame == null) {
			textFrame = new ClientTextFrame();
			textFrame.setOutputText(str);
		}
	}

	//show received content in UI
	public void showOutput(JsonObject obj)
	{
		if(textFrame != null) textFrame.setOutputText(obj);
	}


	public void disconnect() {
			term = true;
			if(textFrame!=null) textFrame.dispose();
	}

	//client receive thread
	public void run() {
		try {
			String data;
			boolean status = true;
			while (status && (data = inreader.readLine()) != null) {
				try {
					log.info("Receive data [{}]", data);
					JsonParser parser = new JsonParser();
					JsonObject json = parser.parse(data).getAsJsonObject();
					//display received content in UI
					String command = json.get("command").getAsString();
					MessageType commandType = MessageType.valueOf(command);
					MessageHandler h = this.handlerMap.get(commandType);
					if (h != null) {
						status = h.processMessage(json, null);
					} else {
						log.error("No handler for message:{}", command);
					}
				} catch (IllegalStateException e) {
					String info = String.format("Invalid message [%s]", data);
					log.error(info);
					this.sendInvalidMsg(info);
					this.disconnect();
				}
			}
			String closeInfo = "\nConnection" + Settings.socketAddress(s) + " closed by remote server";
			log.info(closeInfo);
			if (this.textFrame!=null) this.textFrame.setOutputText(closeInfo);
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
			this.disconnect();
		}

	}

	public void sendRegisterMsg() {
		log.info("send register to server with user={} secret={}",Settings.getUsername(), Settings.getSecret());
		String registerStr = MessageGenerator.register(Settings.getUsername(),Settings.getSecret());
		this.writeMsg(registerStr);
	}

	public void sendLoginMsg() {
		log.info("send login to server with user={} secret={}",Settings.getUsername(), Settings.getSecret());
		String loginStr = MessageGenerator.login(Settings.getUsername(),Settings.getSecret());
		this.writeMsg(loginStr);
	}

	public void sendAnonymousLoginMsg() {
		log.info("send register to server with user={} secret=N/A","anonymous");
		String anonymouStr = MessageGenerator.anonymousLogin(Settings.getUsername());
		this.writeMsg(anonymouStr);
	}

	public void sendLogoutMsg() {
		log.info("send logout to server");
		String logoutMsg = MessageGenerator.generateLogout();
		this.writeMsg(logoutMsg);
	}
	public void sendInvalidMsg(String info) {
		log.info("send invalid message to server with info={}",info);
		String invalidStr = MessageGenerator.invalid(info);
		this.writeMsg(invalidStr);
	}

	@SuppressWarnings("unchecked")
	public void sendActivityObject(JsonObject activityObj) {
		log.info("send activity to server with activity={}",activityObj.toString());
			String actStr = MessageGenerator.generateActMessage(Settings.getUsername(),Settings.getSecret(),activityObj);
			log.debug(actStr);
			this.writeMsg(actStr);
	}

	public String getLocalAddress(){
		return String.format("%s:%s",this.s.getLocalAddress(),this.s.getLocalPort());
	}


}
