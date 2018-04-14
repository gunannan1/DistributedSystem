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

public class UserRegisterHandler extends MessageHandler {

	private int lockRequestId;
	private HashMap<String, LockResult> lockResultHashMap;
	private final Control control;

	public UserRegisterHandler(Control control) {
		this.control = control;
		this.lockResultHashMap = new HashMap<>();
		this.lockRequestId = 0;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		User newuser = null;
		String username = json.get("username").getAsString();
		String secret = json.get("secret").getAsString();
		if (username != null && secret != null) {
			newuser = new User(
					username,
					secret
			);
			Control.log.debug("process register for user {}", username);
		} else {
			String error = String.format("User register command missing information username='{%s}' secret='{%s}'", username, secret);
			Control.log.info(error);
			connection.sendInvalidMsg(error);
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		if (!this.control.checkUserExists(newuser.getUsername())) { // if not exist locally
			if (!this.lockResultHashMap.containsKey(newuser.getUsername())) { // if not in request list
				if (this.control.getServerLoads() > 0) {
					Control.log.debug("broadcast to enquiry username existance:{}", username);
					LockResult newLockResult = new LockResult(lockRequestId++,control.getServerLoads());
					lockResultHashMap.put(newuser.getUsername(),newLockResult);
					this.control.broadcast();
					//TODO broadcast lock_request, may need another method

					return true;
				} else {
					Control.log.debug("No additional server connected, register successfully for user:{} ", username);
					connection.sendRegisterSuccMsg(username);
					this.control.addUser(newuser);
					return true;
				}
			} else { // is already in request list and waiting for other servers' response
				Control.log.info("Username:{} is in registering process, waiting for other servers response.");
				connection.sendRegisterFailedMsg(username + "( another register under processing )");
				connection.closeCon();
				this.control.connectionClosed(connection);
				return false;
			}
		}else{
			Control.log.info("User with username '{}' already exists, reject register and close connection", username);
			connection.sendRegisterFailedMsg(username);
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		// return false to close related connection and thread
	}

	class LockResult {
		public int lockRequestId;
		private int enqueryServerCount;
		private int allowedServerCount;

		public LockResult(int lockRequestId, int enqueryServerCount) {
			this.lockRequestId = lockRequestId;
			this.enqueryServerCount = enqueryServerCount;
			this.allowedServerCount = 0 ;
		}

		public boolean getLockAllow(){
			this.allowedServerCount += 1;
			return this.allowedServerCount == this.enqueryServerCount;
		}
	}
}
