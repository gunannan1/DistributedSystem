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

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
			u = new User(username,secret);
		}catch (NullPointerException e){
			String error = String.format("Lock allowed command missing information username='%s' secret='%s'", username, secret);
			failHandler(error, connection);
			return false;
		}
		catch (UnsupportedOperationException e){
			String error = String.format("Lock allowed command missing information username='%s' secret='%s'", username, secret);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult lockRequest = UserRegisterHandler.registerLockHashMap.get(username);
		BroadcastResult loginRequest = UserLoginHandler.enquiryRequestHashmap.get(username);
		BroadcastResult l = lockRequest == null ? loginRequest:lockRequest;

		// whether the register information in pending list
		if(l == null){
			// just ignore to align with Aaron's server
			Control.log.info("No register information received for user '{}' or the register information is processed already, ignore this message", username);
			return true;
		}

		// whether all servers allowed
		l.addAllow();
		if( l.getResult() == BroadcastResult.LOCK_STATUS.PENDING) {
			Control.log.info("Although LOCK ALLOWED is received, not all servers reply, continue waiting future information...");
			return true;
		}

		// here means this server receives LOCK responses (user not found) from all other servers
		// if owner is the server itself
		if (!l.getFrom().isAuthedServer()) {
			Control.log.info("LOCK ALLOWEDs from all servers are received, send REGISTER_SUCCESS to the client");
			try {
				BroadcastResult.LOCK_STATUS searchStatus = l.getResult();

				return BroadcastResult.processLock(searchStatus,loginRequest,lockRequest,u);
			} catch (Exception e) {
				Control.log.info("The client sending register request was disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		Control.log.info("LOCK ALLOWEDs from all servers are received, send LOCK_ALLOW to the server who sends LOCK_REQUEST");
		// if not owner, send lockAllow to "from" server
		try {
			l.getFrom().sendLockAllowedMsg(username, secret);
			UserRegisterHandler.registerLockHashMap.remove(username);
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
