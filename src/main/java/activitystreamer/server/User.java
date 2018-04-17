package activitystreamer.server;

import java.util.StringJoiner;

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

	@Override
	public String toString() {
		return String.format(" <tr>\n" +
				"      <th scope=\"row\">*</th>\n" +
				"      <td>%s</td>\n" +
				"      <td>%s</td>\n" +
//				"      <td>%s</td>\n" +
//				"      <td>%s</td>\n" +
				"    </tr>",username,secret);
	}

	public static String tableHeader(){
		return "    <tr>\n" +
				"      <th scope=\"col\">#</th>\n" +
				"      <th scope=\"col\">Username</th>\n" +
				"      <th scope=\"col\">Secret</th>\n" +
//				"      <th scope=\"col\">Is Register</th>\n" +
//				"      <th scope=\"col\">Is Connected</th>\n" +
				"    </tr>\n" ;
	}
}
