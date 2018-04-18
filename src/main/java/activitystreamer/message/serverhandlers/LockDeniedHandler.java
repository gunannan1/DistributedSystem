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

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
		}catch (NullPointerException e){
			String error = String.format("Lock denied command missing information username='%s' secret='%s'", username, secret);
			failHandler(error, connection);
			return false;
		}
		catch (UnsupportedOperationException e){
			String error = String.format("Lock denied command missing information username='%s' secret='%s'", username, secret);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult lockRequest = UserRegisterHandler.registerLockHashMap.get(username);
		BroadcastResult loginRequest = UserLoginHandler.enquiryRequestHashmap.get(username);
		BroadcastResult l = lockRequest == null ? loginRequest:lockRequest;
		if (l == null ) {
			// just ignore to align with Aaron's server
			Control.log.error("No register information received for user '{}'", username);
			return true;
		}

		// whether all servers allowed
		l.addDeny();
		if( l.getResult() == BroadcastResult.LOCK_STATUS.PENDING) {
			Control.log.info("LOCK DEINED (user found) is received, not all servers reply, continue waiting future information...");
			return true;
		}

		// whether owner is the server itself, if the 'from' connection is not a server, then it is the user who sends register request
		if (!l.getFrom().isAuthedServer()) {
			try {

				// If it is a register request
				if(lockRequest != null){
					Control.log.info("User {} register failed, username exists in this system.", username);
					Control.log.info("Connection will be closed.");
					l.getFrom().sendRegisterFailedMsg(username);
					l.getFrom().closeCon();
					control.connectionClosed(l.getFrom());
					UserRegisterHandler.registerLockHashMap.remove(username);
				}

				// If it is a login enquiry
				// loginRequest must not be null if code runs here
				if(l.getResult() == BroadcastResult.LOCK_STATUS.USER_FOUND){
					User originUser = l.getUser();
					if(originUser.getSecret().equals(secret)) {
						Control.log.info("User {} login successfully.", username);
						l.getFrom().sendLoginSuccMsg(String.format("login successfully as user '%s'", username));
						l.getFrom().setAuthed(true);

					}else{
						Control.log.info("User {} login failed due to unmatched secret.", username);
						Control.log.info("Connection will be closed.");
						l.getFrom().sendLoginFailedMsg(String.format("secret does not match '%s'",originUser.getSecret()));
						l.getFrom().closeCon();
						control.connectionClosed(l.getFrom());
					}
					UserLoginHandler.enquiryRequestHashmap.remove(username);
				}else {
					Control.log.info("User {} does not exist in this system.", username);
					Control.log.info("Connection will be closed.");
					l.getFrom().sendLoginFailedMsg(String.format("User '%s' does not exist '%s'",username));
					l.getFrom().closeCon();
					control.connectionClosed(l.getFrom());
				}
				return true;
			} catch (Exception e) {
				Control.log.info("The client sending register request is disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		// if not owner, send lockDenied to "from" server
		try {

			l.getFrom().sendLockDeniedMsg(username, secret);
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
