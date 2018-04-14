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
	private Connection con;

	public User(String username, String secret,Connection con) {
		this.username = username;
		this.secret = secret;
		this.isRegistered = false;
		this.con = con;
	}
	public User(String username, String secret) {
		this.username = username;
		this.secret = secret;
		this.isRegistered = false;
		this.con = null;
	}

	public String getUsername() {
		return username;
	}

	public String getSecret() {
		return secret;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public void setRegistered(boolean isRegistered){
		this.isRegistered = isRegistered;
	}

	@Override
	public String toString() {
//		return String.format("%-15s|%-15s|%-20s|%-15s",username,secret,isRegistered,this.con != null);
		return String.format(" <tr>\n" +
				"      <th scope=\"row\">*</th>\n" +
				"      <td>%s</td>\n" +
				"      <td>%s</td>\n" +
				"      <td>%s</td>\n" +
				"      <td>%s</td>\n" +
				"    </tr>",username,secret,isRegistered,this.con.isOpen());
	}

	public static String tableHeader(){
//		StringJoiner sj = new StringJoiner("|");
//		return String.format("%-15s|%-15s|%-20s|%-15s","Username","secret","Is Register","Is Connected");

		return "    <tr>\n" +
				"      <th scope=\"col\">#</th>\n" +
				"      <th scope=\"col\">Username</th>\n" +
				"      <th scope=\"col\">Secret</th>\n" +
				"      <th scope=\"col\">Is Register</th>\n" +
				"      <th scope=\"col\">Is Connected</th>\n" +
				"    </tr>\n" ;
	}
}
