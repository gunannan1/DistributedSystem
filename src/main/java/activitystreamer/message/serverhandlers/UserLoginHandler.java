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

public class UserLoginHandler extends MessageHandler {

	public static int lockRequestId = 0;
	private final Control control;

	public UserLoginHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {

		// TODO need future work

		String username = json.get("username").getAsString();
		String secret = json.get("secret").getAsString();
		if (username.equals("anonymous")) {
//			if(secret != null){
			//TODO add anonymous
			connection.sendLoginSuccMsg(String.format("user %s login successfully", username));
			connection.setAuthed(true);
			connection.setServer(false);

//			}
		} else if (!username.isEmpty() && !secret.isEmpty()) {
			if (this.control.checkSecret(username, secret)) {
				connection.sendLoginSuccMsg(String.format("user %s login successfully", username));
				this.control.markUserConnected(username);
				connection.setAuthed(true);
				connection.setServer(false);
			} else {
				String info = String.format("username(%s) does not exists or secret(%s) does not match",username,secret);
				Control.log.info(info);
				connection.sendLoginFailedMsg(info);
				connection.closeCon();
				this.control.connectionClosed(connection);
			}
		} else {
			Control.log.info("Invalid login message received.");
			connection.sendInvalidMsg("Invalid login message");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		return true;
	}


}
