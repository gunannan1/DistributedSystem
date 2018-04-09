package activitystreamer.message;

import activitystreamer.server.Connection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

/**
 * ClientHandler
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public abstract class MessageHandler {
	private String command;
	protected static final Logger log = LogManager.getLogger();

	public abstract boolean processMessage(JsonObject json,Connection connection);

	public void setCommand(String command) {
		this.command = command;
	}

//	public String toJsonString() {
//		return new Gson().toJson(this);
//	}

//	public static MessageHandler JsonToHandler(JSONObject json) {
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
//				return new RegisterFailedHandler(json);
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
//		return null;
//	}
}
