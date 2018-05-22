package activitystreamer.server.datalayer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Activity
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class Activity implements Comparable<Activity>, Serializable {

	private String originalJsonString;
	private String authenticated_user;
	private long sendTime;
	private boolean isDelivered;
	private long updateTime;

//	private Activity(JsonObject json, String username) {
//		]
//		sendTime = Calendar.getInstance().getTimeInMillis();
//		updateTime = Calendar.getInstance().getTimeInMillis();
//		isDelivered = false;
//	}

	private Activity(String activityJsonStr, String username,long sendTime,long updateTime,boolean isDelivered){
		this.originalJsonString = activityJsonStr;
		this.authenticated_user = username;
		this.sendTime = sendTime;
		this.isDelivered = isDelivered;
		this.updateTime = updateTime;
	}

	public Activity(Activity activity) {
		this.originalJsonString = activity.getOriginalJsonString();
		this.authenticated_user = activity.getAuthenticated_user();
		sendTime = activity.getSendTime();
		isDelivered = activity.isDelivered();
		updateTime = activity.getUpdateTime();
	}

	private Activity(JsonObject json) {
		JsonObject activityJson = json.get("activity").getAsJsonObject();
		this.originalJsonString = json.toString();
		this.authenticated_user = activityJson.get("authenticated_user").getAsString();
		this.sendTime = Long.parseLong(json.get("sendTime").getAsString());
		this.updateTime = Long.parseLong(json.get("updateTime").getAsString());
		this.isDelivered = Boolean.valueOf(json.get("isDelivered").getAsString());
	}

	public static Activity createActivityFromClientJson(JsonObject clientActivityJson, String username){
		clientActivityJson.remove("authenticated_user");
		clientActivityJson.addProperty("authenticated_user",username);

		long sendTime = Calendar.getInstance().getTimeInMillis();
		long updateTime = Calendar.getInstance().getTimeInMillis();
		return new Activity(clientActivityJson.toString(),username,sendTime,updateTime,false);
	}

	public static Activity createActivityFromServerJson(JsonObject json){

		return new Activity(json);
	}

	public Activity copy() {
		return new Activity(this);
	}


	public void setAuthenticated_user(String authenticated_user) {
		this.authenticated_user = authenticated_user;
	}

//	public JsonObject innerJson() {
//		originalJsonString.remove("authenticated_user");
//		originalJsonString.addProperty("authenticated_user", this.authenticated_user);
//		return originalJsonString;
//	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("id",this.hashCode());
		JsonParser parser = new JsonParser();
//		originalJsonString.remove("authenticated_user");
//		originalJsonString.addProperty("authenticated_user", authenticated_user);
		json.add("activity", parser.parse(originalJsonString));
		json.addProperty("updateTime", this.updateTime);
		json.addProperty("sendTime", this.sendTime);
		json.addProperty("isDelivered", this.isDelivered);
		return json;
	}


	public String getOriginalJsonString() {
		return originalJsonString;
	}

	public String getAuthenticated_user() {
		return authenticated_user;
	}

	public long getSendTime() {
		return sendTime;
	}

	public boolean isDelivered() {
		return isDelivered;
	}

	public void setDelivered(boolean delivered) {
		isDelivered = delivered;
		setUpdateTime();

	}

	public void setUpdateTime() {
		this.updateTime = Calendar.getInstance().getTimeInMillis();
	}

	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	public int compareTo(Activity o) {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Activity)) {
			return false;
		}
		Activity other = (Activity) obj;
		if (other.originalJsonString.equals(other.originalJsonString) && authenticated_user.equals(other.authenticated_user) && sendTime == other.sendTime) {
			return true;
		}
		return false;
	}

	public Activity update(Activity other) {
		if (other.getUpdateTime() > this.getUpdateTime()) {
			this.originalJsonString = other.originalJsonString;
			this.setAuthenticated_user(other.authenticated_user);
			this.sendTime = other.sendTime;
			this.isDelivered = other.isDelivered;
			this.updateTime = other.updateTime;
			return other;
		}
		return this;
	}
	//TODO overwirte hashcode

	@Override
	public int hashCode() {
		return originalJsonString.toString().hashCode();
	}

}