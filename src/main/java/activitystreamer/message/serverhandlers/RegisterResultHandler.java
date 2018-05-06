package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonObject;

/**
 * RegisterResultHandler
 * <p>
 * Author Ning Kang
 * Date 6/5/18
 */

public class RegisterResultHandler extends MessageHandler {

	private final Control control;

	public RegisterResultHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Register result message received from {}",connection.getSocket().getRemoteSocketAddress());
		try{
			BroadcastResult.REGISTER_RESULT status = BroadcastResult.REGISTER_RESULT.valueOf(json.get("result").getAsString());
			String username = json.get("username").getAsString();
			String secret = json.get("secret").getAsString();

			// remove user from register process
			UserRegisterHandler.registerLockHashMap.remove(username);

			// if a SUCC message is received, add this user into local storage
			if(status == BroadcastResult.REGISTER_RESULT.SUCC){
				control.addUser(new User(username,secret));
			}
			control.broadcastToServers(json.toString(),connection);
			return true;
		}catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Invalid result broadcast message [%s]",json.toString());
			Control.log.info(error);
			connection.sendInvalidMsg(error);
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}


	}
}
