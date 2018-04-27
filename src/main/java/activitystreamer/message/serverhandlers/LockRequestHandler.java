package activitystreamer.message.serverhandlers;

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

public class LockRequestHandler extends MessageHandler {

	private final Control control;

	public LockRequestHandler(Control control) {
		this.control = control;
	}

	/**
	 * |- validate message
	 * |- check if user exists locally
	 * |- check remotely
	 *
	 * @param json
	 * @param connection
	 * @return
	 */
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		//TODO need future work
		Control.log.info("Lock request received from {}", connection.getSocket().getRemoteSocketAddress());
		User newUser = null;
		String username = null;
		String secret = null;

		// Validate message
		try {
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();

		} catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Lock request command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}
		newUser = new User(username, secret);

		// Broadcast remotely if server load > 0 exclude the sending server
		Control.log.info("User [{}] does not exist in this server", username);
		if (control.getServerLoads() > 0) {
			Control.log.info("More server found, check with other servers(exclude the sending server)");
			control.broadcastLockRequest(newUser, connection);

		} else {
			Control.log.info("No other connecting servers(exclude the sending server), reply lockAllow message back");
			connection.sendLockAllowedMsg(username, secret);
		}

		// check locally
		User localUser = control.checkUserExists(username);
		if (localUser != null && !secret.equals(localUser.getSecret())) {
			Control.log.info("User [{}] exists in this server with a different secret, reply lock denied.", username);
			connection.sendLockDeniedMsg(username, localUser.getSecret());
			return true;
		} else {
			Control.log.info("User [{}] does not exist in this server, reply lock allowed.", username);
			control.addUser(newUser);
			connection.sendLockAllowedMsg(username, secret);
			return true;
		}

	}

	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		this.control.connectionClosed(connection);
	}
}
