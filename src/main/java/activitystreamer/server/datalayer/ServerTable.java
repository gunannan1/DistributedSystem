package activitystreamer.server.datalayer;

import java.util.HashMap;

/**
 * ServerTable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ServerTable implements ITable<ServerRow>{
	private HashMap<String,ServerRow> serverList;

	public ServerTable() {
		this.serverList = new HashMap<>();
	}

	public ServerRow getMinLoadServer(){
		return new ServerRow("!",1,"!",1);
	}

	@Override
	public boolean lockRow(ServerRow row) {
		return false;
	}

	@Override
	public boolean unlockRow(ServerRow row) {
		return false;
	}

	@Override
	public boolean insert(ServerRow row) {
		return false;
	}

	@Override
	public ServerRow updateOrInsert(ServerRow row) {
		serverList.put(row.getId(),row);
		return row;
	}

	@Override
	public boolean delete(ServerRow row) {
		return false;
	}

	@Override
	public ServerRow selectById(String id) {
		return serverList.get(id);
	}

	@Override
	public HashMap<String, ServerRow> getAll() {
		return serverList;
	}
}
