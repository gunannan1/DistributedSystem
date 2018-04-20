import activitystreamer.message.Activity;
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

		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		String ss = timeFormat.format(Calendar.getInstance().getTime());
		System.out.print(ss);

	}

}
