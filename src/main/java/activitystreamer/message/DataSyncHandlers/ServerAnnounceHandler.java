package activitystreamer.message.DataSyncHandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.ServerRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerAnnounceHandler extends MessageHandler {


	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.debug("Announcement received from {}" , connection.getSocket().getRemoteSocketAddress());
		try {
			String serverId = json.get("serverId").getAsString();
			int load = json.get("load").getAsInt();
			String host = json.get("ip").getAsString();
			int port = json.get("port").getAsInt();

			ServerRow receivedInfo = new ServerRow(serverId,load,host,port);
			DataLayer.getInstance().updateOrInsert(receivedInfo);
			receivedInfo.notifyChange(connection);
			return true;
		}catch (Exception e){
			DataLayer.log.error("Invalid ServerAnnounce message:[{}]",json.getAsString());
			e.printStackTrace();
		}
		return true;
	}
}
