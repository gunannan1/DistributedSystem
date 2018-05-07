package activitystreamer;

/**
 * BackupServerInfo
 * <p>
 * Author Ning Kang
 * Date 5/5/18
 */

public class BackupServerInfo {
	private String host;
	private int prot;

	public BackupServerInfo(String host, int prot) {
		this.host = host;
		this.prot = prot;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getProt() {
		return prot;
	}

	public void setProt(int prot) {
		this.prot = prot;
	}
}
