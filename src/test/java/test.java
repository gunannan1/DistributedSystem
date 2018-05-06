import activitystreamer.client.ClientTextFrame;
import activitystreamer.message.Activity;
import activitystreamer.message.serverhandlers.BroadcastResult;
import activitystreamer.server.ServerTextFrame;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * test
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class test {

	public static void main(String[] argv) {

		String a = null;
		JsonObject json = new JsonObject();
		json.addProperty("a",a);
		System.out.println(a);
	}

}
