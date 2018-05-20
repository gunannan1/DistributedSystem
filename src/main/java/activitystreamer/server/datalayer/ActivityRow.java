package activitystreamer.server.datalayer;

import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageType;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * ServerRow
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ActivityRow implements IRow {
	private String owner;
	private ArrayList<Activity> activityList;

	public ActivityRow(String owner){
		this.owner = owner;
		activityList = new ArrayList<>();
	}


	public ActivityRow(JsonObject actJson){
		this.owner = actJson.get("owner").getAsString();
		JsonArray actArray = actJson.get("activity_list").getAsJsonArray();
		for(JsonElement je:actArray){
			Activity act = new Activity(je.getAsJsonObject());
			activityList.add(act);
		}
		Collections.sort(this.activityList);
	}

	public ArrayList<Activity> getActivityList() {
		return activityList;
	}



	@Override
	public long getUpdateTime() {
		return 0;
	}

	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("owner",owner);
		JsonArray actListJson = new JsonArray();
		for(Activity act:activityList){
			actListJson.add(act.toJson());
		}
		json.add("activity_list",actListJson);
		return json;
	}

	public ActivityRow updateOrInsert(Activity activity){
		int index = activityList.indexOf(activity);
		int changedCount = 0 ;
		if(index == -1) {
			activityList.add(activity);
			changedCount = 1;
		}else{
			changedCount += activityList.get(index).update(activity);
		}

		if(changedCount > 0) {
			return this;
		}
		return null;
	}

	@Override
	public boolean lock() {
		return false;
	}

	@Override
	public boolean unlock() {
		return false;
	}

	@Override
	public ActivityRow update(IRow newRow) {
		int changedCount = 0 ;
		for(Activity newOne:((ActivityRow)newRow).getActivityList()){
			int index = activityList.indexOf(newOne);
			if(index >=0){
				changedCount += activityList.get(index).update(newOne);
			}
		}
		if(changedCount > 0){
			return (ActivityRow)newRow;
		}
		return null;
	}

	@Override
	public String getId() {
		return owner;
	}

	public void notifyChange(){
		notifyChange(null);
	}

	@Override
	public void notifyChange(Connection connection) {
		String notifytStr = notifyJsonString();
		NetworkLayer.getNetworkLayer().broadcastToServers(notifytStr, connection);
	}

	private String notifyJsonString(){
		JsonObject json = this.toJson();
		json.addProperty("command", MessageType.ACTIVITY_UPDATE.name());
		return json.toString();
	}


}
