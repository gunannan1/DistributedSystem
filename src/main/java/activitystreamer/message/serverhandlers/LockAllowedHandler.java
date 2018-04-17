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

public class LockAllowedHandler extends MessageHandler {

	private final Control control;

	public LockAllowedHandler(Control control) {
		this.control = control;
	}

	/**
	 * |- validate message
	 * |- whether the register information in pending list
	 * |- whether all servers allowed
	 * |- whether owner is the server itself
	 * 		|- Yes: send back RegisterSucc message to corresponding user
	 * 		|- No: send LockAllow to the server who sends lock request
	 * @param json
	 * @param connection
	 * @return
	 */
	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("Lock allowed is received from {}",connection.getSocket().getRemoteSocketAddress());
		User u = null;
		String username = null;
		String secret = null;
		String owner = null;

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
			owner = json.get("owner").getAsString();
			u = new User(username,secret);
		}catch (NullPointerException e){
			String error = String.format("Lock allowed command missing information username='%s' secret='%s' owner='%s", username, secret,owner);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult l = UserRegisterHandler.registerLockHashMap.get(username);

		// whether the register information in pending list
		if(l == null){
			Control.log.info("No register information received for user '{}' or the register information is processed already, ignore this message", username);
			return true;
		}

		// whether all servers allowed
		if( !l.addAllow()) {
			Control.log.info("Although LOCK ALLOWED is received, not all servers reply, continue waiting future information...");
			return true;
		}



		// here means this server receives LOCK ALLOWED from all other servers
		// if owner is the server itself
		if (owner.equals(control.getIdentifier())) {
			Control.log.info("LOCK ALLOWEDs from all servers are received, send REGISTER_SUCCESS to the client");
			control.addUser(u);
			try {
				Control.log.info("User '{}' registered successfully.", username);
				l.getFrom().sendRegisterSuccMsg(username);
				UserRegisterHandler.registerLockHashMap.remove(username);
				return true;
			} catch (Exception e) {
				Control.log.info("The client sending register request was disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		Control.log.info("LOCK ALLOWEDs from all servers are received, send LOCK_ALLOW to the server who sends LOCK_REQUEST");
		// if not owner, send lockAllow to "from" server
		try {
			l.getFrom().sendLockAllowedMsg(username, secret, owner);
			return true;

		} catch (Exception e) {
			Control.log.info("The server sending lock request request is disconnected");
			return true; // do not close any connection as closing connection should be handled in other way
		}



	}
	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		this.control.connectionClosed(connection);
	}
}
