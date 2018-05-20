package activitystreamer.server.datalayer;

import activitystreamer.message.MessageGenerator;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * UserTable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class UserTable implements ITable<UserRow> {
	private HashMap<String,UserRow> userList;

	public UserTable() {
		userList = new HashMap<>();
	}

	@Override
	public boolean lockRow(UserRow row) {
		return false;
	}

	@Override
	public boolean unlockRow(UserRow row) {
		return false;
	}

	@Override
	public boolean insert(UserRow row) {
		return false;
	}

	@Override
	public UserRow updateOrInsert(UserRow row) {
		UserRow existRow = userList.get(row.getUsername());
		if(existRow == null){
			userList.put(row.getUsername(),row);
		}else{
			existRow.update(row);
		}
		return row;
	}

	@Override
	public boolean delete(String id) {
		userList.remove(id);
		return true;
	}

	@Override
	public UserRow selectById(String id) {
		return userList.get(id);
	}

	@Override
	public HashMap<String, UserRow> getAll() {
		return userList;
	}


	public void markUserOnline(String username,boolean online){
		userList.get(username).login(online);
	}

	public ArrayList<UserRow> connectedUserList(){
		ArrayList<UserRow> connectUsers = new ArrayList<>();
		for(UserRow user:userList.values()){
			if(user.isOnline()){
				connectUsers.add(user);
			}
		}
		return connectUsers;
	}
}
