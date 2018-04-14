package activitystreamer;

import activitystreamer.client.UILogAppender;
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
		json.addProperty("A","Abc");
		String a = json.get("B").getAsString();
		System.out.print(a);

	}

}
