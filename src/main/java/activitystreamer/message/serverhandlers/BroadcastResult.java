package activitystreamer.message.serverhandlers;

import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;

import static activitystreamer.message.serverhandlers.BroadcastResult.LOCK_STATUS.USER_NOT_FOUND;

/**
 * BroadcastResult
 * <p>
 * Author Ning Kang
 * Date 14/4/18
 */

class BroadcastResult {
	public enum LOCK_STATUS {
		PENDING,
		USER_FOUND,
		USER_NOT_FOUND
	}

	//	private String serverIdentifier;
	private int enquiryServerCount;
	private int allowedServerCount;
	private int deniedServerCount;
	private Connection from;
	private User user;

	public BroadcastResult(Connection c, int enqieryServerCount, User u) {
		this.from = c;
		this.enquiryServerCount = enqieryServerCount;
		this.allowedServerCount = 0;
		deniedServerCount = 0;
		user = u;

	}

	public User getUser() {
		return user;
	}

	public void addAllow() {
		this.allowedServerCount += 1;
//		return this.allowedServerCount == this.enquiryServerCount;
	}

	public void addDeny() {
		this.deniedServerCount += 1;
//		return this.deniedServerCount == this.enquiryServerCount;
	}

	public LOCK_STATUS getResult() {
		if (allowedServerCount + deniedServerCount != enquiryServerCount) {
			return LOCK_STATUS.PENDING;
		}
		if (deniedServerCount > 0) {
			return LOCK_STATUS.USER_FOUND;
		} else {
			return USER_NOT_FOUND;
		}

	}


	public Connection getFrom() {
		return from;
	}

	public static boolean processLock(LOCK_STATUS searchStatus,BroadcastResult loginRequest,BroadcastResult lockRequest,User u) {
		String username = u.getUsername();
		String secret = u.getSecret();
		switch (searchStatus) {
			case USER_NOT_FOUND:
				if (loginRequest != null) { // if it is a LOGIN request reply
					Control.log.info("User '{}' login failed, username does not exists.", username);
					loginRequest.getFrom().sendLoginFailedMsg(String.format("No user with username '%s' exists in this system", username));
					UserLoginHandler.enquiryRequestHashmap.remove(username);
					loginRequest.getFrom().closeCon();
					Control.getInstance().connectionClosed(loginRequest.getFrom());
				} else { // if it is a REGISTER request reply
					Control.getInstance().addUser(new User(username, secret));
					Control.log.info("User '{}' registered successfully.", username);
					lockRequest.getFrom().sendRegisterSuccMsg(username);
					UserRegisterHandler.registerLockHashMap.remove(username);
					lockRequest.getFrom().closeCon();
					Control.getInstance().connectionClosed(lockRequest.getFrom());
				}
				break;
			case USER_FOUND:
				if (loginRequest != null) { // if it is a LOGIN request reply
					Control.log.info("User {} login successfully.", username);
					loginRequest.getFrom().sendLoginSuccMsg(String.format("login successfully as user '%s'", username));
					loginRequest.getFrom().setAuthed(true);

					//check redirect
					if(Control.getInstance().findRedirectServer()!=null){
						String redirectServer = Control.getInstance().findRedirectServer();
						Control.log.info("Redirection is triggered, redirect user to server {}",redirectServer);
						Control.getInstance().doRedirect(loginRequest.getFrom(),redirectServer, username);
						return true;
					}

				} else { // if it is a REGISTER request reply
					Control.log.info("User '{}' exists in this system, register failed.", username);
					lockRequest.getFrom().sendRegisterFailedMsg(username);
					UserRegisterHandler.registerLockHashMap.remove(username);
					lockRequest.getFrom().closeCon();
					Control.getInstance().connectionClosed(lockRequest.getFrom());
				}
				break;
			default:
				Control.log.error("BroadResult handler should not run to here !!");
				break;
		}
		return true;
	}
}
