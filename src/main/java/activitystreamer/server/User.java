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

	private boolean isRegistered;

	public User(String username, String secret) {
		this.username = username;
		this.secret = secret;
		this.isRegistered = false;
	}

	public String getUsername() {
		return username;
	}

	public String getSecret() {
		return secret;
	}

	public void setRegistered(boolean isRegistered){
		this.isRegistered = isRegistered;
	}
}
