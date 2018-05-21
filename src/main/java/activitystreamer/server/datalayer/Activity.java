package activitystreamer.server.datalayer;

import com.google.gson.JsonObject;

import java.util.Calendar;

/**
 * Activity
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class Activity implements Comparable<Activity> {
	//TODO an ID should be speficed
	private JsonObject originalJson;
	private String authenticated_user;
	private long sendTime;
	private boolean isDelivered;
	private long updateTime;

	public Activity(JsonObject json, String username) {
		originalJson = json;
		authenticated_user = username;
		sendTime = Calendar.getInstance().getTimeInMillis();
		updateTime = Calendar.getInstance().getTimeInMillis();
		isDelivered = false;
	}

	public Activity(JsonObject json) {
		this.originalJson = json.get("activity").getAsJsonObject();
		this.authenticated_user = originalJson.get("authenticated_user").getAsString();
		this.sendTime = Long.parseLong(json.get("sendTime").getAsString());
		this.updateTime = Long.parseLong(json.get("updateTime").getAsString());
		this.isDelivered = Boolean.valueOf(json.get("isDelivered").getAsString());
	}

	public Activity(Activity activity) {
		this.originalJson = activity.getOriginalJson();
		this.authenticated_user = activity.getAuthenticated_user();
		sendTime = activity.getSendTime();
		isDelivered = activity.isDelivered();
		updateTime = activity.getUpdateTime();
	}

	public Activity copy() {
		return new Activity(this);
	}

	public void setAuthenticated_user(String authenticated_user) {
		this.authenticated_user = authenticated_user;
	}

	public JsonObject innerJson() {
		originalJson.remove("authenticated_user");
		originalJson.addProperty("authenticated_user", this.authenticated_user);
		return originalJson;
	}

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		originalJson.remove("authenticated_user");
		originalJson.addProperty("authenticated_user", authenticated_user);
		json.add("activity", originalJson);
		json.addProperty("updateTime", this.updateTime);
		json.addProperty("sendTime", this.sendTime);
		json.addProperty("isDelivered", this.isDelivered);
		return json;
	}

//	public String toJsonString()
//	{
//		return toJson().toString();
//	}

	public JsonObject getOriginalJson() {
		return originalJson;
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
		if (other.originalJson.equals(other.originalJson) && authenticated_user.equals(other.authenticated_user) && sendTime == other.sendTime) {
			return true;
		}
		return false;
	}

	public int update(Activity other) {
		if (other.getUpdateTime() > this.getUpdateTime()) {
			this.originalJson = other.originalJson;
			this.setAuthenticated_user(other.authenticated_user);
			this.sendTime = other.sendTime;
			this.isDelivered = other.isDelivered;
			this.updateTime = other.updateTime;
			return 1;
		}
		return 0;
	}
}