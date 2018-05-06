import activitystreamer.client.ClientTextFrame;
import activitystreamer.message.Activity;
import activitystreamer.message.serverhandlers.BroadcastResult;
import activitystreamer.server.ServerState;
import activitystreamer.server.ServerTextFrame;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

/**
 * test
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class test {

	public static void main(String[] argv) {

		ServerTextFrame tf = new ServerTextFrame();

		UIRefresher ur = new UIRefresher(tf);
		ur.start();
	}

	private static class UIRefresher extends Thread {
		private boolean isRun;
		ServerTextFrame tf;
		HashMap<String, ServerState> serverStateList;

		UIRefresher() {
			isRun = true;
		}

		public void setTerm() {
			isRun = false;
		}
		UIRefresher(ServerTextFrame tf){
			isRun = true;
			this.tf = tf;
			serverStateList = new HashMap<>();
		}

		@Override
		public void run() {
			while (isRun) {
				try {
					sleep(1000);
//					this.tf.setLoadArea(serverStateList.values());
					System.out.println("refresh");
				} catch (InterruptedException e) {
					System.out.println("n");
				}
			}

		}
	}
}
