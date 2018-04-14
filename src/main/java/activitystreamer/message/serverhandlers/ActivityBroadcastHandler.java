package activitystreamer.message.serverhandlers;

import activitystreamer.message.Activity;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ActivityBroadcastHandler extends MessageHandler {

	private final Control control;

	public ActivityBroadcastHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("Activity broadcastToAll received");

		if(!connection.isAuthedServer()){
			connection.sendInvalidMsg("Received from an unauthenticated server");
			Control.log.info("Received from an unauthenticated server");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(json.get("activity")==null){
			connection.sendInvalidMsg("The message don't have activity");
			Control.log.info("The message don't have activity");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		Activity activity=new Activity(json.get("activity").getAsString());

		for(Connection c:this.control.getConnections()){
			if(c.isAuthedClient()){
				c.sendActivityBroadcastMsg(activity);
			}
			else if(c.isAuthedServer()){
				if(c!=connection){
					c.sendActivityBroadcastMsg(activity);
				}

			}
		}
		return true;
	}
}
