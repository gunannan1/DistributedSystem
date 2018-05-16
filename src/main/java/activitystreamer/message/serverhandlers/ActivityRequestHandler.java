package activitystreamer.message.serverhandlers;

import activitystreamer.message.Activity;
import activitystreamer.message.MessageGenerator;
import activitystreamer.message.MessageHandler;
import activitystreamer.message.MessageType;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonObject;

import java.util.Queue;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ActivityRequestHandler extends MessageHandler {

	private final Control control;

	public ActivityRequestHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		Control.log.info("Activity request received from {}", connection.getSocket().getRemoteSocketAddress());

		String username = json.get("username").getAsString();
		String secret = null;
		try {
			secret = json.get("secret").getAsString();
		} catch (UnsupportedOperationException e) {
			secret = null;
		}

		User conUser = connection.getUser();

		if (!connection.isAuthedClient()) {
			connection.sendAuthFailedMsg("The user has not logged in");
			Control.log.info("The user has not logged in");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (username == null) {
			connection.sendInvalidMsg("The message don't have username");
			Control.log.info("The message don't have username");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (!username.equals("anonymous") && secret == null) {
			connection.sendInvalidMsg("The message don't have secret and the user is not anonymous");
			Control.log.info("The message don't have secret and the user is not anonymous");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (json.get("activity") == null) {
			connection.sendInvalidMsg("The message does not have activity");
			Control.log.info("The message does not have activity");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (!username.equals("anonymous") &&
				(conUser == null || !conUser.getUsername().equals(username) || !conUser.getSecret().equals(secret))
				) {
			connection.sendAuthFailedMsg("The username and secret do not match the logged in the user");
			Control.log.info("The username and secret do not match the logged in the user");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		JsonObject actJson = json.get("activity").getAsJsonObject();
		Activity activity = new Activity(actJson, connection.getUser().getUsername());

		Control.log.info("Process activity from user {}", username);
		activity.setAuthenticated_user(username);

		Control.log.info("Broadcast activity from user {} to all clients/servers", username);

		//update the sequence num
		this.control.updateActivitySeq(username);
		int sequence=this.control.getActivitySeq(username);


		//create the new json for activity queue
//		JsonObject jsonObject=new JsonObject();
//		json.addProperty("command", MessageType.ACTIVITY_BROADCAST.name());
//		json.add("activity", activity.toJson());
//		json.addProperty("sequence",sequence);


		for(Connection c:this.control.getConnections()){
			if(c.isAuthedServer()){
				c.sendActivityBroadcastMsg(activity,sequence);
			}
			else if(c.isAuthedClient()){
				c.sendActivityBroadcastMsg(activity);
			}
//			else if(c.isAuthedClient()){
//				Queue<JsonObject> queue=this.control.getActivityQueue().get(c.getUser().getUsername());
//				queue.add(jsonObject);
//				for(int i=0;i<queue.size();i++){
//					JsonObject js=queue.poll();
//					try{
//						c.sendActivityBroadcastMsg(js.remove("sequence").toString());
//					}catch (Exception e){
//						Control.log.info("Fail to send activity");
//						queue.add(js);
//					}
//
//				}
//			}

		}


		return true;
	}
}
