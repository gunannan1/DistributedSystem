package activitystreamer.message;

import com.google.gson.Gson;

/**
 * Activity
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class Activity {

	public String toJsonString()
	{
		return new Gson().toJson(this);
	}
}
