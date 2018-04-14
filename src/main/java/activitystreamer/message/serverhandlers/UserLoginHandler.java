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
	public static HashMap<String, LoginResult> enquiryRequestHashmap = new HashMap<>();;
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


		Control.log.info("login message is received");

		String username = json.get("username").getAsString();
		String secret = json.get("secret").getAsString();

		//1. Check if anoymous user
		if (username.equals("anonymous")) {
			connection.sendLoginSuccMsg(String.format("user %s login successfully", username));
			connection.setAuthed(true);
			connection.setUser(new User(username, ""));
			connection.sendLoginSuccMsg(String.format("login successfully as user '%s '", username));
			Control.log.info("login successfully as user '{}'", username);
			return true;

		}

		// 2. check if user exists locally
		if (username.isEmpty() || secret.isEmpty()){
			Control.log.info("Information missing for loging, username='{}' secret='{}'",username,secret);
		}



		if (!username.isEmpty() && !secret.isEmpty()) {

			User u = this.control.getUser(username, secret);
			if (u != null) {
				connection.setAuthed(true);
				connection.setUser(u);
				connection.sendLoginSuccMsg(String.format("login successfully as user '%s '", username));
				Control.log.info("login successfully as user '{}'", username);
				return true;
			}
			//3. TODO check if any remote servers
//			else if (control.getServerLoads() > 0) {
//
//			}
			else {
				String info = String.format("username(%s) does not exists or secret(%s) does not match", username, secret);
				Control.log.info(info);
				connection.sendLoginFailedMsg(info);
				connection.closeCon();
				this.control.connectionClosed(connection);
			}
		} else{
			Control.log.info("Invalid login message received.");
			connection.sendInvalidMsg("Invalid login message");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		return true;
	}

	class LoginResult {
		private int enqueryServerCount;
		private int allowedServerCount;

		public LoginResult(int enqueryServerCount) {
			this.enqueryServerCount = enqueryServerCount;
			this.allowedServerCount = 0;
		}

		public boolean getLockAllow() {
			this.allowedServerCount += 1;
			return this.allowedServerCount == this.enqueryServerCount;
		}
	}


}
