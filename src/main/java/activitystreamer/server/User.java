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
//	private boolean registerApproved;
	private boolean con;

	public User(String username, String secret,Connection con) {
		this.username = username;
		this.secret = secret;
//		this.registerApproved = false;
		this.con = false;
	}
	public User(String username, String secret) {
		this.username = username;
		this.secret = secret;
//		this.registerApproved = false;
		this.con = false;
	}

	public String getUsername() {
		return username;
	}

	public String getSecret() {
		return secret;
	}

	public void setCon(boolean con) {
		this.con = con;
	}

//	public void setRegisterApproved(boolean isRegistered){
//		this.registerApproved = isRegistered;
//	}


	public String toHTML() {
//		return String.format("%-15s|%-15s|%-20s|%-15s",username,secret,registerApproved,this.con != null);
		return String.format(" <tr>\n" +
				"      <th scope=\"row\">*</th>\n" +
				"      <td>%s</td>\n" +
				"      <td>%s</td>\n" +
				"      <td>%s</td>\n" +
				"    </tr>",username,secret,this.con);
	}

	public static String tableHeader(){
//		StringJoiner sj = new StringJoiner("|");
//		return String.format("%-15s|%-15s|%-20s|%-15s","Username","secret","Is Register","Is Connected");

		return "    <tr>\n" +
				"      <th scope=\"col\">#</th>\n" +
				"      <th scope=\"col\">Username</th>\n" +
				"      <th scope=\"col\">Secret</th>\n" +
				"      <th scope=\"col\">Is Connected</th>\n" +
				"    </tr>\n" ;
	}
}
