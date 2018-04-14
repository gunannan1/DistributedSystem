package activitystreamer.message.serverhandlers;

import activitystreamer.server.Connection;

/**
 * BroadcastResult
 * <p>
 * Author Ning Kang
 * Date 14/4/18
 */

class BroadcastResult {
//	private String serverIdentifier;
	private int enquiryServerCount;
	private int allowedServerCount;
	private Connection from;

	public BroadcastResult(Connection c, int enqieryServerCount) {
		this.from = c;
//		this.serverIdentifier = serverIdentifier;
		this.enquiryServerCount = enqieryServerCount;
		this.allowedServerCount = 0;
	}

	public boolean addLockAllow() {
		this.allowedServerCount += 1;
		return this.allowedServerCount == this.enquiryServerCount;
	}

	public Connection getFrom() {
		return from;
	}
}
