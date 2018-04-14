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

public class UserRegisterHandler extends MessageHandler {
	// HashMap<username, LockResult>
	public static HashMap<String, LockResult> registerLockHashMap = new HashMap<>();
	private final Control control;

	public UserRegisterHandler(Control control) {
		this.control = control;
	}

	/**
	 * Check user exists and register
	 * 1. Validate message
	 * 2. Check if user exists locally
	 * 		YES: send register failed msg and return false
	 * 		NO: 2.1 check if username under register process( in the register list but not approved)
	 * 			YES: send register failed msg and return false
	 * 			NO:  2.1.1 check if any remote servers exists
	 * 				YES: broadcastToAll
	 * 				NO: register succ
	 *
	 * @param json       message
	 * @param connection connection that received this message
	 * @return true if succ
	 */
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Register message is received ");
		User newuser = null;
		String username = null;
		String secret = null;

		// 1. Validate message
		try{
		 username = json.get("username").getAsString();
		 secret = json.get("secret").getAsString();

		}catch (NullPointerException e){
			String error = String.format("User register command missing information username='%s' secret='%s'", username, secret);
			connection.sendInvalidMsg(error);
			failHandler(error, connection);
			return false;
		}

		Control.log.info("process register for user {}", username);
		newuser = new User(username, secret);

		//2. Check if user exists locally
		if (this.control.checkUserExists(newuser.getUsername())) {
			String error = String.format("User '%s' exists in this server'", username);
			connection.sendRegisterFailedMsg(error);
			failHandler(error, connection);

			return false;
		}

		// 2.1 check if username under register process( in the register list but not approved)
		if (UserRegisterHandler.registerLockHashMap.containsKey(newuser.getUsername())) {
			String error = String.format("User '%s' is under register processing'", username);
			connection.sendRegisterFailedMsg(error);
			failHandler(error, connection);
			return false;
		}

		// 2.1.1 check if any remote servers exists
		if (this.control.getServerLoads(null) > 0) {
			Control.log.info("Remote servers exist, need to get confirmation from remote servers for user register '{}' ", username);
			LockResult lockResult = new LockResult(control.getIdentifier(),connection,control.getServerLoads(null));
			UserRegisterHandler.registerLockHashMap.put(newuser.getUsername(), lockResult);
			//TODO need testing
			// broadcastToAll lock request and then waiting for lock_allow & lock_denied, this register process will be handled by LockAllowedHandler & LockDeniedHandler
			control.broadcastLockRequest(newuser,connection);
			return true;
		}

		// register successfully if no above condision
		Control.log.info("No additional server connected, register successfully for user:{} ", username);
		connection.sendRegisterSuccMsg(username);
		this.control.addUser(newuser);
		connection.setUser(newuser);

		return true;

	}

	private void failHandler(String error, Connection connection) {
		Control.log.info(error);
		connection.closeCon();
		this.control.connectionClosed(connection);
	}

	class LockResult {
		private String serverIdentifier;
		private int enqueryServerCount;
		private int allowedServerCount;
		private Connection from;

		public LockResult(String serverIdentifier,Connection c, int enqueryServerCount) {
			this.from = c;
			this.serverIdentifier = serverIdentifier;
			this.enqueryServerCount = enqueryServerCount;
			this.allowedServerCount = 0;
		}

		public boolean addLockAllow() {
			this.allowedServerCount += 1;
			return this.allowedServerCount == this.enqueryServerCount;
		}

		public Connection getFrom() {
			return from;
		}
	}
}
