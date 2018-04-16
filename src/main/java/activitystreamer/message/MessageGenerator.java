package activitystreamer.message;

import activitystreamer.util.Settings;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MessageGenerator
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class MessageGenerator {
	protected static final Logger log = LogManager.getLogger();

	public static String generateInvalid(String info) {
		return generate(MessageType.INVALID_MESSAGE, info);
	}

	public static String generateAuthFail(String info) {
		return generate(MessageType.AUTHENTICATION_FAIL, info);
	}

	public static String generateRegisterFail(String username) {
		return generate(MessageType.REGISTER_FAILED, String.format("%s is already registered with the system",username));
	}

	public static String generateLoginFail(String info) {
		return generate(MessageType.LOGIN_FAILED, info);
	}

	public static String generateRegisterSucc(String username) {
		return generate(MessageType.REGISTER_SUCCESS, String.format("register success for %s",username));
	}

	public static String generateLoginSucc(String info) {
		return generate(MessageType.LOGIN_SUCCESS, info);
	}

	public static String generateAuthen(String secret) {
		return generate(MessageType.AUTHENTICATE, secret);
	}

	
	// LOCK Message types
	public static String generateLockRequest(String username, String secret,String serverIdentifier) {
		return generate(MessageType.LOCK_REQUEST,username,secret,serverIdentifier);
	}
	public static String generateLockDenied(String username, String secret,String serverIdentifier) {
		return generate(MessageType.LOCK_DENIED,username,secret,serverIdentifier);
	}
	public static String generateLockAllowed(String username, String secret,String serverIdentifier) {
		return generate(MessageType.LOCK_ALLOWED,username,secret,serverIdentifier);
	}

	// User enquiry types
	public static String generateUserEnqueryRequest(String username, String secret,String serverIdentifier) {
		return generate(MessageType.USER_ENQUIRY,username,secret,serverIdentifier);
	}
	public static String generateUserFound(String username, String secret,String serverIdentifier) {
		return generate(MessageType.USER_FOUND,username,secret,serverIdentifier);
	}
	public static String generateUserNotFound(String username, String secret,String serverIdentifier) {
		return generate(MessageType.USER_NOT_FOUND,username,secret,serverIdentifier);
	}

	public static String generateRegister(String username, String secret) {
		return generate(MessageType.REGISTER, username, secret);
	}

	public static String generateLogin( String username, String secret) {
		return generate(MessageType.LOGIN, username, secret);
	}

	public static String generateAnonymousLogin( String username) {
		return generate(MessageType.LOGIN, username);
	}


	private static String generate(MessageType messageType, String username,String secret, String owner){
		JsonObject json = new JsonObject();
		json.addProperty("command", messageType.name());
		json.addProperty("username", username);
		json.addProperty("secret", secret);
		json.addProperty( "owner" , owner);
		return json.toString();
	}

//	public static String generateActivityMsg(Activity act,String username,String secret){
//		JsonObject json = new JsonObject();
//		json.addProperty("command", MessageType.ACTIVITY_MESSAGE.name());
//		json.addProperty("username", username);
//		json.addProperty("secret", secret);
//		json.addProperty( "activity" ,act.toJsonString());
//		return json.toString();
//	}

	private static String generate(MessageType messageType, String infoOrSecret) {
		JsonObject json = new JsonObject();
		switch (messageType) {
			case INVALID_MESSAGE:
			case AUTHENTICATION_FAIL:
			case REGISTER_FAILED:
			case LOGIN_FAILED:
			case REGISTER_SUCCESS:
			case LOGIN_SUCCESS:
				json.addProperty("command", messageType.name());
				json.addProperty("info", infoOrSecret);
				return json.toString();
			case AUTHENTICATE:
				json.addProperty("command", messageType.name());
				json.addProperty("secret", infoOrSecret);
				return json.toString();
			case LOGIN:
				json.addProperty("command", messageType.name());
				json.addProperty("username", infoOrSecret);
				return json.toString();
			default:
				log.error("Invalid message type '{}' with parameter 'info' {}", messageType, infoOrSecret);

		}
		return json.toString();
	}

	// For LOCK_ALLOWED,LOCK_DENIED,LOCK_REQUEST,REGISTER,LOGIN
	private static String generate(MessageType messageType, String username, String secret) {
		JsonObject json = new JsonObject();
		switch (messageType) {
			case REGISTER:
			case LOGIN:
				json.addProperty("command", messageType.name());
				json.addProperty("username", username);
				json.addProperty("secret", secret);
				return json.toString();
			default:
				log.error("Invalid message type '{}' with parameter username='{}', secret='{}' ", messageType, username, secret);

		}
		return json.toString();
	}

	// for REDIRECT
	public static String generateRedirect(String hostname, int port) {
		JsonObject json = new JsonObject();
		json.addProperty("command",MessageType.REDIRECT.name());
		json.addProperty("hostname", hostname);
		json.addProperty("port", port);
		return json.toString();
	}

	// for LOGOUT
	public static String generateLogout() {
		JsonObject json = new JsonObject();

		json.addProperty("command", MessageType.LOGOUT.name());

		return json.toString();
	}

	// for SERVER_ANNOUNCE
	public static String generateAnnounce(String id, int load, String host, int port) {
		JsonObject json = new JsonObject();

		json.addProperty("command", MessageType.SERVER_ANNOUNCE.name());
		json.addProperty("id", id);
		json.addProperty("load", load);
		json.addProperty("host", host);
		json.addProperty("port", port);

		return json.toString();
	}

	// for ACTIVITY_BROADCAST
	public static String generateActBroadcast(Activity act) {
		JsonObject json = new JsonObject();

		json.addProperty("command", MessageType.ACTIVITY_BROADCAST.name());
		json.addProperty("activity", act.toJsonString());

		return json.toString();
	}

	// for ACTIVITY_MESSAGE
	public static String generateActMessage(String username, String secret, Activity act) {
		JsonObject json = new JsonObject();

		json.addProperty("command", MessageType.ACTIVITY_MESSAGE.name());
		json.addProperty("username", username);
		json.addProperty("secret", secret);
		json.addProperty("activity", act.toString());

		return json.toString();
	}

}
