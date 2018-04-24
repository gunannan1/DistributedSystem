package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageGenerator;
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
		}catch (NullPointerException | UnsupportedOperationException e){
			String error = String.format("Lock allowed command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}


		String allowedMsg = MessageGenerator.lockAllowed(username,secret);

		BroadcastResult lockRequest = UserRegisterHandler.registerLockHashMap.get(username);
		// If lock request is not from this server
		if (lockRequest == null ) {
			Control.log.info("Transform lock allowed to the system username=[{}]", username);
			control.broadcastToServers(allowedMsg,connection);
			return true;
		}else{ // If this is the server who sent lockRequest
			try {
				lockRequest.addAllow();
				BroadcastResult.LOCK_STATUS searchStatus = lockRequest.getResult();
				return BroadcastResult.processLock(searchStatus,lockRequest,new User(username,secret));
			} catch (Exception e) {
				Control.log.info("The client sending register request is disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}
	}
	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.sendInvalidMsg(error);
		connection.closeCon();
		this.control.connectionClosed(connection);
	}
}
