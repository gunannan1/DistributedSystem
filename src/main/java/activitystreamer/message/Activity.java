package activitystreamer.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;

/**
 * Activity
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class Activity {
	private JsonObject originalJson;
	private String authenticated_user;

	public Activity(String text,String username){
		JsonParser jsonParser = new JsonParser();
		this.originalJson = (JsonObject) jsonParser.parse(text);
		authenticated_user=username;
	}
	public Activity(JsonObject json, String username){
		originalJson = json;
		authenticated_user = username;
	}

	public void setAuthenticated_user(String authenticated_user) {
		this.authenticated_user = authenticated_user;
	}

	public JsonObject toJson()
	{
//		JsonObject json = new JsonObject();
//		json.addProperty("text",text);
//		json.addProperty("authenticated_user",authenticated_user);
		originalJson.remove("authenticated_user");
		originalJson.addProperty("authenticated_user",this.authenticated_user);
		return originalJson;
	}

//	@Override
//	public String toString() {
//		String jsonStr = "{\"text\":\"%s\",\"authenticated_user\":\"%s\"}";
//		return String.format(jsonStr,text,authenticated_user);
//	}
}
