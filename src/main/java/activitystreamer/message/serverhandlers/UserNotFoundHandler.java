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

public class UserNotFoundHandler extends MessageHandler {

	private final Control control;

	public UserNotFoundHandler(Control control) {
		this.control = control;
	}

	/**
	 * |- validate message
	 * |- whether the login information in pending list
	 * |- whether all servers reply NOT FOUND
	 * 		NO: Ignore and waiting
	 * 		YES: |- whether owner is the server itself
	 * 			 	|- Yes: send back LOGIN_FAILED message to corresponding user
	 * 			 	|- No: send USER_NOT_FOUND to the server who sends USER_ENQUIRY
	 *
	 * @param json
	 * @param connection
	 * @return
	 */
	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("USER_NOT_FOUND message is received.");
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
			String error = String.format("USER_NOT_FOUND command missing information username='%s' secret='%s' owner='%s", username, secret,owner);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult l = UserLoginHandler.enquiryRequestHashmap.get(username);

		// whether the register information in pending list
		if(l == null){
			Control.log.info("No login request / enquiry information received for user '{}' or the enquiry is processed already, ignore this message", username);
			return true;
		}

		// whether all servers allowed
		if(!l.addDeny()) {
			Control.log.info("Although USER_NOT_FOUND is received, not all servers reply, continue waiting future information...");
			return true;
		}

		// here means this server receives LOCK ALLOWED from all other servers
		// if owner is the server itself
		if (owner.equals(control.getIdentifier())) {
			try {
				String info = String.format("User '%s' does not exist in this system", username);
				Control.log.info(info);
				Connection c = l.getFrom();
				c.sendLoginFailedMsg(info);
				c.closeCon();
				control.connectionClosed(c);
				UserLoginHandler.enquiryRequestHashmap.remove(username);
				return true;
			} catch (Exception e) {
				Control.log.info("The client sending login request was disconnected.");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		// if not owner, send USER_NOT_FOUND to "from" server
		try {
			l.getFrom().sendUserNotFoundMsg(username, secret, owner);
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
