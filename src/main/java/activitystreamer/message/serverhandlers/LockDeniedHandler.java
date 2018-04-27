package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
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
		Control.log.info("Lock Denied message is recieved");
		String username = null;
		String secret = null;

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
		}catch (NullPointerException | UnsupportedOperationException e){
			String error = String.format("Lock denied command missing information username=[%s] secret=[%s]", username, secret);
			failHandler(error, connection);
			return false;
		}

		control.removeUser(username);

		BroadcastResult lockRequest = UserRegisterHandler.registerLockHashMap.get(username);

		// If lock request is not from this server
		if (lockRequest == null ) {
			Control.log.info("Transform lock allowed to the system username=[{}]", username);
			control.broadcastToServers(json.toString(),connection);
			return true;
		}else{// If this is the server who sent lockRequest
			lockRequest.addDeny();
			try {
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
