package activitystreamer.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Activity
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class Activity {
	private String activity;

	public Activity(String activity){
		this.activity = activity;
	}


	public String toJsonString()
	{
		return new Gson().toJson(this, JsonObject.class);
	}

	@Override
	public String toString() {
		return this.activity;
	}
}
