package activitystreamer.server.datalayer;

import java.util.Collection;
import java.util.HashMap;

/**
 * ServerTable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ActivityTable implements ITable<ActivityRow>{
	private HashMap<String,ActivityRow> activityMap;

	public ActivityTable() {
		this.activityMap = new HashMap<>();
	}

	@Override
	public boolean lockRow(ActivityRow row) {
		return false;
	}

	@Override
	public boolean unlockRow(ActivityRow row) {
		return false;
	}

	@Override
	public boolean insert(ActivityRow row) {
		return false;
	}

	@Override
	public ActivityRow updateOrInsert(ActivityRow newRow) {
		ActivityRow row = activityMap.get(newRow.getId());
		if(row != null){
			row.update(row);
		}else{
			activityMap.put(newRow.getId(),newRow);
		}
		return newRow;
	}

	public void insertActivity(Activity activity){
		Collection<UserRow> allUsers = DataLayer.getInstance().getAllUsers().values();
		for(UserRow user:allUsers){
			if(user.isOnline()){
				ActivityRow activityRow = activityMap.get(user.getUsername());
				if(activityRow == null){
					ActivityRow newActivityRow = new ActivityRow(user.getUsername());
					newActivityRow.updateOrInsert(activity.copy());
					activityMap.put(user.getUsername(),newActivityRow);
				}else{
					activityRow.updateOrInsert(activity.copy());
				}
			}
		}
	}
	@Override
	public boolean delete(ActivityRow row) {
		return false;
	}

	@Override
	public ActivityRow selectById(String id) {
		return activityMap.get(id);
	}

	@Override
	public HashMap<String, ActivityRow> getAll() {
		return activityMap;
	}
}
