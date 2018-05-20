package activitystreamer.message.DataSyncHandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.Activity;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.application.Control;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ActivityBroadcastHandler extends MessageHandler {

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Activity broadcast message received from [{}]", connection.connectionFrom());

		if(!connection.isAuthedServer()){
			connection.sendInvalidMsg("Received from an unauthenticated server");
			Control.log.info("Received from an unauthenticated server");
			connection.closeCon();
			NetworkLayer.getNetworkLayer().connectionClosed(connection);
			return false;
		}

		else if(json.get("activity")==null){
			connection.sendInvalidMsg("The message does not have activity");
			Control.log.info("The message does not have activity");
			connection.closeCon();
			NetworkLayer.getNetworkLayer().connectionClosed(connection);
			return false;
		}

		Control.log.info("Broadcast received activity message:{}",json);
		Activity activity = new Activity(json);
		DataLayer.getInstance().insertActivity(activity,connection);
//		NetworkLayer.getNetworkLayer().broadcastToAll(json.toString(),connection);

		return true;
	}
}
