package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class LockDeniedHandler extends MessageHandler {

	private final Control control;

	public LockDeniedHandler(Control control) {
		this.control = control;
	}
	/**
	 * |- validate message
	 * |- whether owner is the server itself
	 * 		|- Yes: send back RegisterFailed message to corresponding user
	 * 		|- No: send LockDenied to the server who sends lock request
	 * @param json
	 * @param connection
	 * @return
	 */
	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("Lock Denied message is recieved");
		String username = null;
		String secret = null;
		String owner = null;

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
			owner = json.get("owner").getAsString();
		}catch (NullPointerException e){
			String error = String.format("Lock denied command missing information username='%s' secret='%s' owner='%s", username, secret,owner);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult l = UserRegisterHandler.registerLockHashMap.get(username);
		if (l != null) {
			// whether owner is the server itself
			if (owner.equals(control.getIdentifier())) {
				try {
					l.getFrom().sendRegisterFailedMsg(username);
					return true;
				} catch (Exception e) {
					Control.log.info("The client sending register request is disconnected");
					return true; // do not close any connection as closing connection should be handled in other way
				}
			}

			// if not owner, send lockAllow to "from" server
			try {

				l.getFrom().sendLockDeniedMsg(username, secret, owner);
				return true;

			} catch (Exception e) {
				Control.log.info("The server sending lock request request is disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}

		}

		// this should not happen
		Control.log.error("No register information received for user '%s'", username);
		return false;

	}
	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		this.control.connectionClosed(connection);
	}
}
