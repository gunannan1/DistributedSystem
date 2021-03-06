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
		Control.log.info("Lock request received from {}", connection.getSocket().getRemoteSocketAddress());
		User newUser = null;
		String username = null;
		String secret = null;

		// Validate message
		try {
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();

		}
		catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Lock request command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}

		newUser = new User(username, secret);

		// check locally
		User localUser = control.checkUserExists(username);
		if (localUser != null) {
			Control.log.info("User [{}] exists in this server, reply lock denied (user found) request with secret in this server",username);
			connection.sendLockDeniedMsg(username, localUser.getSecret());
			return true;
		}

		// check lockrequest list
		if (UserRegisterHandler.registerLockHashMap.get(username)!=null){
			Control.log.info("User [{}] is already under register process, reject",username);
			connection.sendLockDeniedMsg(username, localUser.getSecret());
			return true;
		}
		
		// Check remotely if server load > 0 exclude the sending server
		Control.log.info("User [{}] does not exist in this server", username);
		if (control.getServerLoads(connection) > 0) {
			Control.log.info("More server found, check with other servers(exclude the sending server)");
			// add this requst to local requst hashmap
			BroadcastResult lockResult = new BroadcastResult(connection, control.getServerLoads(connection), newUser);
			UserRegisterHandler.registerLockHashMap.put(username, lockResult);
			control.broadcastLockRequest(newUser, connection);

		} else {
			Control.log.info("No other servers in this system(exclude the sending server), reply lockAllow message back");
			connection.sendLockAllowedMsg(username, secret);
			return true;
		}

		return true;
	}

	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		this.control.connectionClosed(connection);
	}
}
