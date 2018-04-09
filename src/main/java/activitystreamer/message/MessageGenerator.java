package activitystreamer.message;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * MessageGenerator
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class MessageGenerator {
	protected static final Logger log = LogManager.getLogger();

	public static String generateInvalid(MessageType messageType, String info) {
		return generate(messageType, info);
	}

	public static String generateAuthFail(MessageType messageType, String info) {
		return generate(messageType, info);
	}

	public static String generateRegisterFail(MessageType messageType, String info) {
		return generate(messageType, info);
	}

	public static String generateLoginFail(MessageType messageType, String info) {
		return generate(messageType, info);
	}

	public static String generateLRegisterSucc(MessageType messageType, String info) {
		return generate(messageType, info);
	}

	public static String generateLoginSucc(MessageType messageType, String info) {
		return generate(messageType, info);
	}

	public static String generateAuthen(MessageType messageType, String secret) {
		return generate(messageType, secret);
	}

	public static String generateLockAllow(MessageType messageType, String username, String secret) {
		return generate(messageType, username, secret);
	}

	public static String generateLockDenied(MessageType messageType, String username, String secret) {
		return generate(messageType, username, secret);
	}

	public static String generateLockRequest(MessageType messageType, String username, String secret) {
		return generate(messageType, username, secret);
	}

	public static String generateRegister(MessageType messageType, String username, String secret) {
		return generate(messageType, username, secret);
	}

	public static String generateLogin(MessageType messageType, String username, String secret) {
		return generate(messageType, username, secret);
	}


	private static String generate(MessageType messageType, String infoOrSecret) {
		JSONObject json = new JSONObject();
		switch (messageType) {
			case INVALID_MESSAGE:
			case AUTHENTICATION_FAIL:
			case REGISTER_FAILED:
			case LOGIN_FAILED:
			case REGISTER_SUCCESS:
			case LOGIN_SUCCESS:
				json.put("command", messageType.name());
				json.put("info", infoOrSecret);
				return json.toJSONString();
			case AUTHENTICATE:
				json.put("command", messageType.name());
				json.put("secret", infoOrSecret);
				return json.toJSONString();
			default:
				log.error("Invalid message type '{}' with parameter 'info' {}", messageType, infoOrSecret);

		}
		return json.toJSONString();
	}

	// For LOCK_ALLOWED,LOCK_DENIED,LOCK_REQUEST,REGISTER,LOGIN
	private static String generate(MessageType messageType, String username, String secret) {
		JSONObject json = new JSONObject();
		switch (messageType) {
			case LOCK_ALLOWED:
			case LOCK_DENIED:
			case LOCK_REQUEST:
			case REGISTER:
			case LOGIN:
				json.put("command", messageType.name());
				json.put("username", username);
				json.put("secret", secret);
				return json.toJSONString();
			default:
				log.error("Invalid message type '{}' with parameter username='{}', secret='{}' ", messageType, username, secret);

		}
		return json.toJSONString();
	}

	// for REDIRECT
	public static String generateRedirect(MessageType messageType, String hostname, int port) {
		JSONObject json = new JSONObject();
		json.put("command", messageType.name());
		json.put("hostname", hostname);
		json.put("port", port);
		return json.toJSONString();
	}

	// for LOGOUT
	public static String generateLogout(MessageType messageType) {
		JSONObject json = new JSONObject();

		json.put("command", messageType.name());

		return json.toJSONString();
	}

	// for SERVER_ANNOUNCE
	public static String generateAnnounce(MessageType messageType, String id, int load, String host, int port) {
		JSONObject json = new JSONObject();

		json.put("command", messageType.name());
		json.put("id", id);
		json.put("load", load);
		json.put("host", host);
		json.put("port", port);

		return json.toJSONString();
	}

	// for ACTIVITY_BROADCAST
	public static String generateActBroadcast(MessageType messageType, Activity act) {
		JSONObject json = new JSONObject();

		json.put("command", messageType.name());
		json.put("activity", act.toJsonString());

		return json.toJSONString();
	}

	// for ACTIVITY_MESSAGE
	public static String generateActMessage(MessageType messageType, String username, String secret, Activity act) {
		JSONObject json = new JSONObject();

		json.put("command", messageType.name());
		json.put("username", username);
		json.put("secret", secret);
		json.put("activity", act.toJsonString());

		return json.toJSONString();
	}


}
