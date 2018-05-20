package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.ServerRow;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static activitystreamer.message.datasynchandlers.ServerAnnounceHandler.AnnounceType.UPDATE;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerAnnounceHandler extends MessageHandler {
	public enum AnnounceType{
		UPDATE,
		DELETE
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.debug("Announcement received from {}" , connection.getSocket().getRemoteSocketAddress());
		try {
			AnnounceType announceType = AnnounceType.valueOf(json.get("action").getAsString());
			if(announceType == UPDATE) {
				String serverId = json.get("serverId").getAsString();
				int load = json.get("load").getAsInt();
				String host = json.get("ip").getAsString();
				int port = json.get("port").getAsInt();

				ServerRow receivedInfo = new ServerRow(serverId, load, host, port);
				DataLayer.getInstance().updateOrInsert(receivedInfo);
				receivedInfo.notifyChange(connection);
				return true;
			}else{
				String serverId = json.get("serverId").getAsString();
				DataLayer.getInstance().deleteServer(serverId);
			}
		}catch (Exception e){
			DataLayer.log.error("Invalid ServerAnnounce message:[{}]",json.getAsString());
			e.printStackTrace();
		}
		return true;
	}
}
