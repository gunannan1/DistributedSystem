import activitystreamer.client.UILogAppender;
import activitystreamer.message.Activity;
import activitystreamer.server.ServerTextFrame;
import com.google.gson.JsonObject;

/**
 * test
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class test {

	public static void main(String[] argv) {

		JsonObject json = new JsonObject();
		json.addProperty("command","ACTIVITY_MESSAGE");
		Activity a = new Activity("hello");
		json.add("activity",a.toJson());
		System.out.print(json.toString());

	}

}
