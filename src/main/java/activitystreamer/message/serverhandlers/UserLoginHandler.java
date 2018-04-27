package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
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
	 */
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {

		Control.log.info("login message is received from {}",connection.getSocket().getRemoteSocketAddress());

		User newUser;
		String username = null;
		String secret = null;


		// 1. check if both username & secret exist
		try {
			username = json.get("username").getAsString();

			//2.check anonymous login
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

				//Control.log.info("user '{}' login successfully", username);
				return true;
			}
			secret = json.get("secret").getAsString();
		}
		catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Information missing for login, username=[%s] secret=[%s]",username,secret);
			Control.log.info(error);
			connection.sendInvalidMsg(error);
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		newUser = new User(username,secret);


		// 3. Check if user exists locally
		User localUser = this.control.getUser(username);
		if (localUser != null) {
			// if secret is correct
			if(localUser.getSecret().equals(secret)) {
				connection.setAuthed(true);
				connection.setUser(localUser);
				connection.sendLoginSuccMsg(String.format("login successfully as user [%s]", username));
				//check redirect
				if (redirectCheck(connection, username)) {
					return true;
				}
				Control.log.info("login successfully as user [{}]", username);
				return true;
			}else{
				String info = String.format("Secret [%s] does not match for user [%s]",secret,username);
				Control.log.info(info);
				connection.sendLoginFailedMsg(info);
				connection.closeCon();
				this.control.connectionClosed(connection);
				return false;
			}
		}
		connection.sendLoginFailedMsg(String.format("User [%s] does not exist.",username));
		Control.log.info("User [{}] does not exist.",username);
		connection.closeCon();
		this.control.connectionClosed(connection);
		return false;
	}

	public static boolean redirectCheck(Connection connection,String username){
		Control control = Control.getInstance();
		if(Control.getInstance().findRedirectServer()!=null){
			String redirectServer = control.findRedirectServer();
			Control.log.info("Redirection is triggered, redirect user to server {}",redirectServer);
			control.doRedirect(connection,redirectServer,username);
			return true;
		}
		return false;
	}

}
