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

	public static String invalid(String info) {
		return generate(MessageType.INVALID_MESSAGE, info);
	}

	public static String authFail(String info) {
		return generate(MessageType.AUTHENTICATION_FAIL, info);
	}

	public static String registerFail(String username) {
		return generate(MessageType.REGISTER_FAILED, String.format("%s is already registered with the system",username));
	}

	public static String loginFail(String info) {
		return generate(MessageType.LOGIN_FAILED, info);
	}

	public static String registerSucc(String username) {
		return generate(MessageType.REGISTER_SUCCESS, String.format("register success for %s",username));
	}

	public static String loginSucc(String info) {
		return generate(MessageType.LOGIN_SUCCESS, info);
	}

	public static String authen(String secret) {
		return generate(MessageType.AUTHENTICATE, secret);
	}

	
	// LOCK Message types
	public static String lockRequest(String username, String secret) {
		return generate(MessageType.LOCK_REQUEST,username,secret);
	}
	public static String lockDenied(String username, String secret) {
		return generate(MessageType.LOCK_DENIED,username,secret);
	}
	public static String lockAllowed(String username, String secret) {
		return generate(MessageType.LOCK_ALLOWED,username,secret);
	}

	public static String register(String username, String secret) {
		return generate(MessageType.REGISTER, username, secret);
	}

	public static String login(String username, String secret) {
		return generate(MessageType.LOGIN, username, secret);
	}

	public static String anonymousLogin(String username) {
		JsonObject json = new JsonObject();
		json.addProperty("command", MessageType.LOGIN.name());
		json.addProperty("username", username);
		return json.toString();
	}

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
//			case LOGIN: //sendAnonymousLoginMsg
//				json.addProperty("command", messageType.name());
//				json.addProperty("username", infoOrSecret);
//				return json.toString();
			default:
				log.error("Invalid message type [{}] with parameter 'info' {}", messageType, infoOrSecret);

		}
		return json.toString();
	}

	// For LOCK_ALLOWED,LOCK_DENIED,LOCK_REQUEST,REGISTER,LOGIN
	private static String generate(MessageType messageType, String username, String secret) {
		JsonObject json = new JsonObject();
		switch (messageType) {
			case REGISTER:
			case LOGIN:
			case LOCK_DENIED:

			case LOCK_REQUEST:
				json.addProperty("command", messageType.name());
				json.addProperty("username", username);
				json.addProperty("secret", secret);
				return json.toString();
			case LOCK_ALLOWED:
				json.addProperty("command", messageType.name());
				json.addProperty("username", username);
				json.addProperty("secret", secret);
//				json.addProperty("server",Settings.getServerId());
				return json.toString();

			default:
				log.error("Invalid message type [{}] with parameter username=[{}], secret=[{}] ", messageType, username, secret);

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
		json.addProperty("hostname", host);
		json.addProperty("port", port);

		return json.toString();
	}

	// for ACTIVITY_BROADCAST
	public static String generateActBroadcast(Activity act) {
		JsonObject json = new JsonObject();

		json.addProperty("command", MessageType.ACTIVITY_BROADCAST.name());
		json.add("activity", act.toJson());

		return json.toString();
	}

	// for ACTIVITY_MESSAGE
	public static String generateActMessage(String username, String secret, JsonObject act) {
		JsonObject json = new JsonObject();

		json.addProperty("command", MessageType.ACTIVITY_MESSAGE.name());
		json.addProperty("username", username);
		json.addProperty("secret", secret);
		json.add("activity", act);

		return json.toString();
	}

}
