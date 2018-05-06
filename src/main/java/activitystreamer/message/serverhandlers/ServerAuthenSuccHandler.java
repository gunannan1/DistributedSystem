package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerAuthenSuccHandler extends MessageHandler {

	private final Control control;

	public ServerAuthenSuccHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Authen Succ message received from {}:{}",
				connection.getRemoteServerHost(),connection.getRemoteServerPort());
		try{

			Control.log.info("Initialise user list when joining this system");
			HashMap<String, User> userList = new HashMap<>();
			JsonArray userJsonArray = json.getAsJsonArray("user_list");
			if (userJsonArray == null) {
				connection.sendInvalidMsg("Invalid AUTHENTICATION_SUCC message");
				return false;
			}
			for (JsonElement oneUser : userJsonArray) {
				JsonObject info = oneUser.getAsJsonObject();
				String username = info.get("username").getAsString();
				String secret = info.get("secret").getAsString();
				userList.put(username,new User(username,secret));
			}
			control.setUserList(userList);
			connection.setAuthed(true);
			control.setProvideService(true); // begin to provide services to clients/servers
			return true;
		}catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Invaid AUTHENTICATION_SUCC  message received:%s", json.toString());
			Control.log.info(error);
			return false;
		}
	}
}
