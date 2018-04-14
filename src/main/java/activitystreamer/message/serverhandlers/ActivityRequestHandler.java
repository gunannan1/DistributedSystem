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

public class ActivityRequestHandler extends MessageHandler {

	private final Control control;

	public ActivityRequestHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("Lock request recieved");

		String username = json.get("username").getAsString();
		String secret = json.get("secret").getAsString();

		if(!connection.isAuthedClient()){
			connection.sendAuthFailedMsg("The user has not logged in");
			Control.log.info("The user has not logged in");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(username==null){
			connection.sendInvalidMsg("The message don't have username");
			Control.log.info("The message don't have username");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(!username.equals("anonymous")&&secret==null){
			connection.sendInvalidMsg("The message don't have secret and the user is not anonymous");
			Control.log.info("The message don't have secret and the user is not anonymous");
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

		else if(!username.equals("anonymous")&&!this.control.checkSecret(username,secret)){
			connection.sendAuthFailedMsg("The username and secret do not match the logged in the user");
			Control.log.info("The username and secret do not match the logged in the user");
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
