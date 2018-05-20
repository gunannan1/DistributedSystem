package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerAuthenSuccHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Authen Succ message received from {}:{}",
				connection.getRemoteServerHost(),connection.getRemoteServerPort());
		try{

			Control.log.info("Initialise user list when joining this system");
//			HashMap<String, User> userList = new HashMap<>();
//			JsonArray userJsonArray = json.getAsJsonArray("user_list");
//			if (userJsonArray == null) {
//				connection.sendInvalidMsg("Invalid AUTHENTICATION_SUCC message");
//				return false;
//			}
//			for (JsonElement oneUser : userJsonArray) {
//				JsonObject info = oneUser.getAsJsonObject();
//				String username = info.get("username").getAsString();
//				String secret = info.get("secret").getAsString();
//				userList.put(username,new User(username,secret));
//			}
//			control.setUserList(userList);
			// TODO datalayer sync data
			DataLayer.getInstance().syncAllUserData(json.get("user_list").getAsJsonArray());
			connection.setAuthed(true);
			Control.getInstance().setProvideService(true); // begin to provide services to clients/servers
			NetworkLayer.getNetworkLayer().startListener();
			Control.getInstance().startUI();
			return true;
		}catch (Exception e) {
			String error = String.format("Invaid AUTHENTICATION_SUCC  message received:%s", json.toString());
			Control.log.info(error);
			return false;
		}
	}
}
