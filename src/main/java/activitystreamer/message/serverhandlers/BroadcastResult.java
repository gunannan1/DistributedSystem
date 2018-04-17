package activitystreamer.message.serverhandlers;

import activitystreamer.server.Connection;
import activitystreamer.server.User;

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

	public BroadcastResult(Connection c, int enqieryServerCount,User u) {
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
		} else{
			return LOCK_STATUS.USER_NOT_FOUND;
		}

	}


	public Connection getFrom() {
		return from;
	}
}
