package activitystreamer.client;

import Message_Vivian.*;
import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.message.clienthandlers.ClientFailedMessageHandler;
import activitystreamer.util.Settings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private TextFrame textFrame;
	private Socket s = null;
	private HashMap<MessageType, MessageHandler> handlerMap;

	BufferedReader in;
	BufferedWriter out;
	private String serverId;


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
		this.in = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
		this.out = new BufferedWriter(new OutputStreamWriter(this.s.getOutputStream()));
//		// new and open UI
//		textFrame = new TextFrame();
		start();

	}

	private void initMessageHandlers() {
		// Initialize handlers for messages that client may receive
		clientSolution.handlerMap = new HashMap<>();
		clientSolution.handlerMap.put(MessageType.LOGIN_FAILED, new ClientFailedMessageHandler(this));
//		clientSolution.handlerMap.put(MessageType.LOGIN_SUCCESS,);
//		clientSolution.handlerMap.put(MessageType.REDIRECT,);
		clientSolution.handlerMap.put(MessageType.REGISTER_FAILED, new ClientFailedMessageHandler(this));
//		clientSolution.handlerMap.put(MessageType.REGISTER_SUCCESS,);
		clientSolution.handlerMap.put(MessageType.AUTHENTICATION_FAIL, new ClientFailedMessageHandler(this));
	}

	// TODO estimate connection
	private Socket connectToServer(String host, int port) {

		try {
			if(this.s == null) {
				return new Socket(host, port);
			}
			return this.s;
		} catch (IOException e) {
			log.error("Cannot connect to server {} via port {}", host, port);
		}

		return null;
	}


	public boolean sendRegisterRequest() {
		// TODO use existing socket to sendRegisterRequest
		this.s = connectToServer(Settings.getRemoteHostname(), Settings.getRemotePort());
		try {
			this.in = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(this.s.getOutputStream()));
			this.out.write(MessageGenerator.generateRegister(MessageType.REGISTER, Settings.getUsername(), Settings.getSecret()));
		} catch (IOException e) {
			e.printStackTrace();
			//TODO add log
		}
		return false;
	}

//
//	private boolean sendAuthoriseRequest(String username, String secret, Socket socket){
//		// TODO use existing socket to sendRegisterRequest
//
//		return false;
//	}
//
//	public boolean login(String username, String secret, Socket socket)
//    {
//        //receive LOGIN_SUCCESS-->return true
//        //receive AUTHENTICATION_FAIL-->return false
//
//        return false;
//    }

	public void startUI() {
		textFrame = new TextFrame();
	}


	@SuppressWarnings("unchecked")
	public void sendActivityObject(JSONObject activityObj) {

		try {
			ActivityMsg activityMsg = new ActivityMsg();
			activityMsg.setUsername(Settings.getUsername());
			activityMsg.setSecret(Settings.getSecret());
			activityMsg.setObject(activityObj.toString());
			activityMsg.setId(serverId);
			String activityMessage = activityMsg.toJsonString();
			out.write(activityMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO send message via socket

	}


	public void disconnect() {
		// TODO close socket, close TextFrame
		try {
			sendLogoutMsg();
			s.close();
			textFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//client receive thread
	public void run() {
		try {
			while (true) {
				String data = this.in.readLine();
				log.debug("Receive data {}", data);
				JsonParser parser = new JsonParser();
				JsonObject json = parser.parse(data).getAsJsonObject();
				String command = json.get("command").getAsString();
				MessageType commandType = MessageType.valueOf(command);
				MessageHandler h = this.handlerMap.get(commandType);
				h.processMessage(json, null);
			}
//            boolean term=false;
//            while(!term && data !=null)
//            {
//                term=ClientSkeleton.getInstance().process(data);
//
//            }
//            log.debug("Connection closed in "+Settings.socketAddress(s));

		} catch (IOException e) {
			e.printStackTrace();
			log.error("Connection" + Settings.socketAddress(s) + " closed with exception " + e);
		}


	}

	public synchronized boolean process(String receivedJsonStr) {
		log.debug("Client received:" + receivedJsonStr);
		JsonObject receivedJson;
		try {
			receivedJson = new Gson().fromJson(receivedJsonStr, JsonObject.class);
		} catch (JsonSyntaxException e) {
			log.debug(("Client received msg failed. Not Json format:" + e.getMessage()));
			return true;
		}
		if (!containCommandField(receivedJson)) {
			return true;
		}
		String command = receivedJson.get("command").getAsString();
//        switch (command)
//        {
//            case JsonMessage.ACTIVITY_BROADCAST:
//                log.info("Activity broadcast received");
//                textFrame.setOutputText(receivedJson);
//                return false;
//
//            case JsonMessage.REGISTER_SUCCESS:
//                return processRegisterSuccessMsg(receivedJson);
//
//
//            case JsonMessage.REGISTER_FAILED:
//                return processRegisterFailedMsg(receivedJson);
//
//            case JsonMessage.REDIRECT:
//                return processRedirectMsg(receivedJson);
//
//
//            case JsonMessage.AUTHENTICATION_FAIL:
//                log.info("Server fails to authenticate");
//                //close current connection
//                disconnect();
//                return true;
//
//
//            case JsonMessage.LOGIN_SUCCESS:
//                return processLoginSuccessMsg(receivedJson);
//
//            case JsonMessage.LOGIN_FAILED:
//                return processLoginFailedMsg(receivedJson);
//
//            case JsonMessage.INVALID_MESSAGE:
//                return processInvalidMsg(receivedJson);
//
//            default:
//                return processUnknownMsg(receivedJson);
//        }
		return false;
	}

//	public synchronized void handleMessage(String responseString){
//		JSONObject json = new JSONObject(resonseString);
//		MessageType messageType = null;
//		try {
//			messageType = MessageType.valueOf((String) json.get("command"));
//		} catch (IllegalArgumentException e) {
//			//TODO add log
//		}
//
//		switch (messageType) {
//			//TODO new different handler for different message
//
//			// Server related message types
//			case LOGIN:
//				break;
//			case LOGOUT:
//				break;
//			case CLIENT_AUTHENTICATE:
//				break;
//			case REGISTER:
//				return new RegisterHandler(json);
//			case REGISTER_SUCCESS:
//				break;
//			case REGISTER_FAILED:
//				break;
//			case ACTIVITY_BROADCAST:
//				break;
//			case AUTHENTICATE:
//				break;
//			case SERVER_ANNOUNCE:
//				break;
//
//			// Client related message types
//			case LOGIN_SUCCESS:
//				break;
//			case LOGIN_FAILED:
//				break;
//			case AUTHENTICATION_FAIL:
//				break;
//			case REDIRECT:
//				break;
//
//
//			// general message type
//			case INVALID_MESSAGE:
//				break;
//			case ACTIVITY_MESSAGE:
//				break;
//			default:
//				break;
//		}
//
//
//	}


	private boolean processUnknownMsg(JsonObject receivedJson) {
		log.info("Unknown message received");
		disconnect();
		return true;
	}

	private boolean processInvalidMsg(JsonObject receivedJson) {
		log.info("Client failed to send activity message to server");
		String info = receivedJson.get("info").getAsString();
		textFrame.showErrorMsg(info);
		disconnect();
		return true;
	}

	private boolean processLoginFailedMsg(JsonObject receivedJson) {
		log.info("Login failed");
		textFrame.setOutputText(receivedJson);
		disconnect();
		return true;
	}

	private boolean processLoginSuccessMsg(JsonObject receivedJson) {
		log.info("Login success received");
		log.debug("open GUI");
		textFrame.setOutputText(receivedJson);

		return false;
	}

	private boolean processRedirectMsg(JsonObject receivedJson) {
		log.info("Redirect");
		//close current connection
		disconnect();

		//setup new host and port number
		serverId = receivedJson.get("id").getAsString();
		String newHost = receivedJson.get("hostname").getAsString();
		int newPort = receivedJson.get("port").getAsInt();
		Settings.setRemoteHostname(newHost);
		Settings.setRemotePort(newPort);

		//connect
		return true;
	}

	private boolean processRegisterFailedMsg(JsonObject receivedJson) {
		log.info("Register failed");
		textFrame.setOutputText(receivedJson);
		disconnect();
		return true;

	}

	private boolean processRegisterSuccessMsg(JsonObject receivedJson) {
		log.info("Register success received");
		String info = receivedJson.get("info").getAsString();
		textFrame.setOutputText(receivedJson);
		disconnect();
		return true;

	}

	private boolean containCommandField(JsonObject receivedJsonObj) {
		if (!receivedJsonObj.has("command")) {
			InvalidMsg invalidMsg = new InvalidMsg();
			invalidMsg.setInfo("Message_Vivian must contain field command");
			try {
				this.out.write(invalidMsg.toJsonString());
				return false;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;

	}

	public void sendLoginMsg() {
		LoginMsg loginMsg = new LoginMsg();
		String loginStr = MessageGenerator.generateLogin(MessageType.LOGIN, Settings.getUsername(), Settings.getSecret());
		try {
			this.out.write(loginStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//
//        public void sendRegisterMessage() {
//            RegisterMsg registerMsg = new RegisterMsg();
//            registerMsg.setUsername(Settings.getUsername());
//            registerMsg.setSecret(Settings.getSecret());
//            String registerMessage = registerMsg.toJsonString();
//            try {
//                this.out.writeUTF(registerMessage);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }

	public void sendAnonymousLoginMsg() {
		AnonymousLoginMsg anonymousLoginMsg = new AnonymousLoginMsg();
		String anonymousMsg = anonymousLoginMsg.toJsonString();
		try {
			this.out.write(anonymousMsg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendLogoutMsg() {
		LogoutMsg logoutMsg = new LogoutMsg();
		logoutMsg.setUsername(Settings.getUsername());
		logoutMsg.setSecret(Settings.getSecret());
		try {
			this.out.write(logoutMsg.toJsonString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
