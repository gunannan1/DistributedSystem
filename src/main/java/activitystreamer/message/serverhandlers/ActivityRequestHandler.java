package activitystreamer.message.serverhandlers;

import activitystreamer.message.Activity;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ActivityRequestHandler extends MessageHandler {

	private final Control control;

	public ActivityRequestHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		//TODO need future work
		Control.log.info("Activity request received from {}", connection.getSocket().getRemoteSocketAddress());

		String username = json.get("username").getAsString();
		String secret = null;
		try {
			secret = json.get("secret").getAsString();
		} catch (UnsupportedOperationException e) {
			secret = null;
		}

		User conUser = connection.getUser();

		if (!connection.isAuthedClient()) {
			connection.sendAuthFailedMsg("The user has not logged in");
			Control.log.info("The user has not logged in");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (username == null) {
			connection.sendInvalidMsg("The message don't have username");
			Control.log.info("The message don't have username");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (!username.equals("anonymous") && secret == null) {
			connection.sendInvalidMsg("The message don't have secret and the user is not anonymous");
			Control.log.info("The message don't have secret and the user is not anonymous");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (json.get("activity") == null) {
			connection.sendInvalidMsg("The message does not have activity");
			Control.log.info("The message does not have activity");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (!username.equals("anonymous") &&
				(conUser == null || !conUser.getUsername().equals(username) || !conUser.getSecret().equals(secret))
				) {
			connection.sendAuthFailedMsg("The username and secret do not match the logged in the user");
			Control.log.info("The username and secret do not match the logged in the user");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		JsonObject actJson = json.get("activity").getAsJsonObject();
		Activity activity = new Activity(actJson, connection.getUser().getUsername());

		Control.log.info("Process activity from user {}", username);
		activity.setAuthenticated_user(username);

		Control.log.info("Broadcast activity from user {} to all clients/servers", username);
		for (Connection c : this.control.getConnections()) {
			if (c.isAuthedServer() || c.isAuthedClient()) {
				c.sendActivityBroadcastMsg(activity);
			}
		}

		return true;
	}
}
