package activitystreamer.server;

/**
 * User
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class User {
	private String username;
	private String secret;
	private String host;
	private int port;
//	private String identify;
	private boolean isRegistered;

	public User(String username, String secret, String host, int port) {
		this.username = username;
		this.secret = secret;
		this.host = host;
		this.port = port;
//		this.identify = String.format("%s%d",host.replaceAll(".","_"),port);
		this.isRegistered = false;
	}

	public String getUsername() {
		return username;
	}

	public void setRegistered(boolean isRegistered){
		this.isRegistered = isRegistered;
	}
}
