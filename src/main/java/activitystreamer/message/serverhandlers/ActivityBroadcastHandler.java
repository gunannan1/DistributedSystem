package activitystreamer.message.serverhandlers;

import activitystreamer.message.Activity;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;

import java.util.Queue;

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
//		Control.log.info("Broadcast received activity message");


		String username=json.get("activity").getAsJsonObject().get("authenticated_user").getAsString();
		Queue<JsonObject> queue=this.control.getActivityQueue_Send().get(username);

		//for server,send the activity directly
		for(Connection c:this.control.getConnections()) {
			if (c.isAuthedServer() && c != connection) {
				c.sendActivityBroadcastMsg(json.toString());
			}
		}

		//for client, think about the activity order
		if(json.get("sequence").getAsInt()==this.control.getActivitySeq(username)+1){
			json.remove("sequence");
			for(Connection c:this.control.getConnections()) {
				if (c.isAuthedClient() && c != connection) {
					c.sendActivityBroadcastMsg(json.toString());
				}
			}
			this.control.updateActivitySeq(username);
			if(queue.size()!=0){
				for(int i=0;i<queue.size();i++){
					if(queue.peek().get("sequence").getAsInt()==this.control.getActivitySeq(username)+1){
						JsonObject jsonObject=queue.poll();
						jsonObject.remove("sequence");
						String message=jsonObject.toString();
						for(Connection c:this.control.getConnections()) {
							if (c.isAuthedClient() && c != connection) {
								c.sendActivityBroadcastMsg(message);
							}
						}
						this.control.updateActivitySeq(username);
					}
					else {
						break;
					}
				}
			}
		}
		else {
			this.control.getActivityQueue_Send().get(username).add(json);
		}

		return true;
	}
}
