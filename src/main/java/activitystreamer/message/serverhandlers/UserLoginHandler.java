package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class UserLoginHandler extends MessageHandler {
	public static HashMap<String, BroadcastResult> enquiryRequestHashmap = new HashMap<>();;
	private final Control control;

	public UserLoginHandler(Control control) {
		this.control = control;
	}


	/**
	 * 1. check if anoymous user
	 * 2. check if both username & secret exist
	 * 3. check if user exists locally
	 * 4. check if any remote servers
	 * 		NO - login failed
	 * 		YES - broadcastToAll for user information
	 */
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {

		Control.log.info("login message is received from {}",connection.getSocket().getRemoteSocketAddress());

		User newUser = null;
		String username = json.get("username").getAsString();
		JsonElement secretJson = json.get("secret");
		String secret = secretJson == null ? null:secretJson.getAsString();

		//1. Check if anoymous user
		if (username.equals("anonymous")) {
			connection.setAuthed(true);
			connection.setUser(new User(username, ""));
			connection.sendLoginSuccMsg(String.format("login successfully as user '%s '", username));

			//check redirect
			if(this.control.findRedirectServer()!=null){
				String redirectServer = this.control.findRedirectServer();
				Control.log.info("Redirection is triggered, redirect user to server {}",redirectServer);
				this.control.doRedirect(connection,redirectServer, username);
				return true;
			}

			Control.log.info("user '{}' login successfully", username);
			return true;

		}

		// 2. check if both username & secret exist
		if (username.isEmpty() || secret.isEmpty()){
			Control.log.info("Information missing for loging, username='{}' secret='{}'",username,secret);
			connection.sendInvalidMsg("Invalid login message " + json.toString());
			Control.log.info("Close connection with {}",connection.getSocket().getRemoteSocketAddress());
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		newUser = new User(username,secret);
		// 3. Check if user exists locally
		User localUser = this.control.authUser(username, secret);
		if (localUser != null) {
			connection.setAuthed(true);
			connection.setUser(localUser);
			connection.sendLoginSuccMsg(String.format("login successfully as user '%s '", username));

			//check redirect
			if(this.control.findRedirectServer()!=null){
				String redirectServer = this.control.findRedirectServer();
				Control.log.info("Redirection is triggered, redirect user to server {}",redirectServer);
				this.control.doRedirect(connection,redirectServer,username);
				return true;
			}
			Control.log.info("login successfully as user '{}'", username);
			return true;
		}

		// 4. check if any remote servers
		 if (control.getServerLoads(null) > 0) {
			Control.log.info("Remote servers exist, will check with other servers for user '{}' ", username);

			BroadcastResult loginResult = new BroadcastResult(connection,control.getServerLoads(connection),newUser);
			UserLoginHandler.enquiryRequestHashmap.put(username, loginResult);

			connection.setUser(newUser);
			connection.setAuthed(false);

			// broadcastToAll lock request and then waiting for lock_allow & lock_denied, this register process will be handled by LockAllowedHandler & LockDeniedHandler
			control.broadcastEnquiry(control.getIdentifier(),newUser,connection);
			return true;
		}

		// Should not run to here
		Control.log.error("Wired things happened in class UserLoginHandler");
		return false;

	}

}
