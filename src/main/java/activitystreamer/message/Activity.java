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
	private String text;
	private String authenticated_user;

	public Activity(String text){
		this.text = text;
		authenticated_user="";
	}

	public void setAuthenticated_user(String authenticated_user) {
		this.authenticated_user = authenticated_user;
	}

	public JsonObject toJson()
	{
		JsonObject json = new JsonObject();
		json.addProperty("text",text);
		json.addProperty("authenticated_user",authenticated_user);
		return json;
	}

	@Override
	public String toString() {
		String jsonStr = "{\"text\":\"%s\",\"authenticated_user\":\"%s\"}";
		return String.format(jsonStr,text,authenticated_user);
	}
}
