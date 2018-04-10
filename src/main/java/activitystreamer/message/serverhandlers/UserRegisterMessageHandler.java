package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageGenerator;
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

public class UserRegisterMessageHandler extends MessageHandler {

	public static int lockRequestId = 0;
	private HashMap<String, LockResult> lockResultHashMap;
	private final Control control;

	public UserRegisterMessageHandler(Control control) {
		this.control = control;
		this.lockResultHashMap = new HashMap<>();
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		User newuser = null;
		String username = json.get("username").getAsString();
		String secret = json.get("secret").getAsString();
		if (username != null && secret != null) {
			newuser = new User(
					username,
					secret,
					connection.getSocket().getRemoteSocketAddress().toString(),
					connection.getSocket().getPort()
			);
			Control.log.debug("process register for user {}", username);
		} else {
			Control.log.error("User register command missing information username='{}' secret='{}'", username, secret);
			return false;
		}

		if (!this.control.checkUserExists(newuser.getUsername())) { // if not exist locally
			if (!this.lockResultHashMap.containsKey(newuser.getUsername())) { // if not in request list
				if (this.control.getServerLoads() > 0) {
					Control.log.debug("broadcast to enquiry username existance:{}", username);
					this.control.broadcast();//TODO broadcast lock_request, may need another method

					return true;
				} else {
					Control.log.debug("No additional server connected, register successfully for user:{} ", username);
					String registerSucc = MessageGenerator.generateRegisterSucc(username);
					this.control.addUser(newuser);
					connection.writeMsg(registerSucc);
					return true;
				}
			} else { // is already in request list and waiting for other servers' response
				Control.log.error("Username:{} is in registering process, waiting for other servers response.");
				return false;
			}
		}else{
			Control.log.debug("User with username '{}' already exists, reject register and close connection", username);
		}

		// return false to close related connection and thread
		return false;
	}

	class LockResult {
		public int lockRequestId;
		private int enqueryServerCount;

		public LockResult(int lockRequestId, int enqueryServerCount) {
			this.lockRequestId = lockRequestId;
			this.enqueryServerCount = enqueryServerCount;
		}
	}
}
