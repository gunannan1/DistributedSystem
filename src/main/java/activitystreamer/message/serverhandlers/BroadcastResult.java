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

	public static boolean processLock(LOCK_STATUS searchStatus,BroadcastResult lockRequest,User u) {
		String username = u.getUsername();
		String secret = u.getSecret();
		switch (searchStatus) {
			case USER_NOT_FOUND:
					Control.getInstance().addUser(new User(username, secret));
					Control.log.info("User [{}] registered successfully.", username);
					lockRequest.getFrom().sendRegisterSuccMsg(username);
					lockRequest.getUser().setRegistered(true);
					UserRegisterHandler.registerLockHashMap.remove(username);
					lockRequest.getFrom().closeCon();
					Control.getInstance().connectionClosed(lockRequest.getFrom());

				break;
			case USER_FOUND:
				// if it is a REGISTER request reply
					Control.log.info("User [{}] exists in this system, register failed.", username);
					lockRequest.getFrom().sendRegisterFailedMsg(username);
					UserRegisterHandler.registerLockHashMap.remove(username);
					lockRequest.getFrom().closeCon();
					Control.getInstance().connectionClosed(lockRequest.getFrom());
				break;
			default:
				Control.log.error("BroadResult handler should not run to here !!");
				break;
		}
		return true;
	}
}
