package activitystreamer.server.datalayer;

import java.io.Serializable;
import java.util.HashMap;

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

	ServerRow getMinLoadServer(){
		return new ServerRow("!",1,"!",1);
	}

	@Override
	protected ServerRow updateOrInsert(ServerRow row) {
		serverList.put(row.getId(), row);
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
