package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonObject;

/**
 * UserFoundHandler
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

		Control.log.info("USER_FOUND message is received from {}",connection.getSocket().getRemoteSocketAddress());
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
			Control.log.error("No enquiry request received or request is processed for user '%s'", username);
			return true;
		}


		// whether owner is the server itself
		if (owner.equals(control.getIdentifier())) {
			try {
				Control.log.info("USER_FOUND received, allow the login request from user {}",username);
				String info = String.format("User '%s' login successfully.", username);
				Connection c = l.getFrom();
				c.setAuthed(true);
				c.sendLoginSuccMsg(info);

				//check redirect
				if(this.control.findRedirectServer()!=null){
					String redirectServer = this.control.findRedirectServer();
					Control.log.info("Redirection is triggered, redirect user to server {}",redirectServer);
					this.control.doRedirect(l.getFrom(),redirectServer, username);
				}
				return true;
			} catch (Exception e) {
				Control.log.info("The client who sent login request was disconnected");
				return true; // do not close any connection as closing connection should be handled in other way
			}
		}

		// if not owner, send USER_FOUND to "from" server
		try {
			Control.log.info("Send USER_FOUND back to the server who sent ENQUIRY_REQUEST");
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
