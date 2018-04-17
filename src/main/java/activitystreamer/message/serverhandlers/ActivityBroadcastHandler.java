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
		Control.log.info("Activity broadcast message received from {}", connection.getSocket().getRemoteSocketAddress());

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

		Control.log.info("Broadcast received activity message");
		for(Connection c:this.control.getConnections()){
			if((c.isAuthedServer()||c.isAuthedClient())&&c!=connection){
				c.sendActivityBroadcastMsg(json.toString());
			}
		}
		return true;
	}
}
