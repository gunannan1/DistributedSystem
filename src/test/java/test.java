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

		ServerTextFrame s = new ServerTextFrame();
		s.setLoadArea("<table class='table table-bordered'> <thead>%s</thead><tbody>%S</tbody></table><p>Update Time:%s</p>");

	}

}
