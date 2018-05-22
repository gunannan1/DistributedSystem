package activitystreamer.server.datalayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ServerTable
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ServerTable extends Table<ServerRow> implements Serializable {
	private HashMap<String,ServerRow> serverList;

	public ServerTable() {
		this.serverList = new HashMap<>();
	}

	public ServerRow getMinLoadServer(){
		int minload=Integer.MAX_VALUE;
		String minloadServerId=null;
		for(Map.Entry entry:serverList.entrySet()){
			ServerRow serverRow=(ServerRow) entry.getValue();
			if(serverRow.getLoad()<minload){
				minload=serverRow.getLoad();
				minloadServerId=serverRow.getId();
			}
		}

		return new ServerRow(serverList.get(minloadServerId).getServerId(), serverList.get(minloadServerId).getLoad(),
				serverList.get(minloadServerId).getIp(), serverList.get(minloadServerId).getPort());
	}

	@Override
	protected ServerRow updateOrInsert(ServerRow row) {
		serverList.put(row.getId(), row.copy());
		return row;
	}

	@Override
	protected ServerRow delete(String id) {
		ServerRow deletedRow = serverList.get(id);
		serverList.remove(id);
		return deletedRow;
	}


	@Override
	protected ServerRow selectById(String id) {
		return serverList.get(id);
	}

	@Override
	protected HashMap<String, ServerRow> getAll() {
		return serverList;
	}
}
