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

public class UserFoundHandler extends MessageHandler {

	private final Control control;

	public UserFoundHandler(Control control) {
		this.control = control;
	}
	/**
	 * |- validate message
	 * |- whether owner is the server itself
	 * 		|- Yes: send back User_Found message to corresponding user
	 * 		|- No: send User_Found to the server who sends enquiry message
	 * @param json
	 * @param connection
	 * @return
	 */
	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("USER_FOUND message is recieved");
		String username = null;
		String secret = null;
		String owner = null;

		// Validate message
		try{
			username = json.get("username").getAsString();
			secret = json.get("secret").getAsString();
			owner = json.get("owner").getAsString();
		}catch (NullPointerException e){
			String error = String.format("USER_FOUND command missing information username='%s' secret='%s' owner='%s", username, secret,owner);
			failHandler(error, connection);
			return false;
		}


		BroadcastResult l = UserLoginHandler.enquiryRequestHashmap.get(username);
		if (l == null) {
			// this should not happen
			Control.log.error("No enquiry request received for user '%s'", username);
			return false;
		}


		// whether owner is the server itself
		if (owner.equals(control.getIdentifier())) {
			try {
				String info = String.format("User '%s' login successfully.", username);
				Connection c = l.getFrom();
				c.setAuthed(true);
				c.sendLoginSuccMsg(info);

				//check redirect
				if(this.control.findRedirectServer()!=null){
					this.control.doRedirect(l.getFrom(),this.control.findRedirectServer(),username);
				}
				return true;
			} catch (Exception e) {
				Control.log.info("The client sending login request was disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		// if not owner, send USER_FOUND to "from" server
		try {

			l.getFrom().sendUserFoundMsg(username, secret, owner);
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
