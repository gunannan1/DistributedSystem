package activitystreamer.message.serverhandlers;

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

public class ServerAnnounceHandler extends MessageHandler {

	private final Control control;

	public ServerAnnounceHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work

		Control.log.info("Announce recieved");
		String id=json.get("id").getAsString();
		int load=json.get("load").getAsInt();
		String host=json.get("host").getAsString();
		int port=json.get("load").getAsInt();
		System.out.println("hahhahaaaaaaahahahahahahahaha"+port);

		if(!connection.isAuthedServer()){
			connection.sendInvalidMsg("Received from an unauthenticated server");
			Control.log.info("Received from an unauthenticated server");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(id==null){
			connection.sendInvalidMsg("No id present");
			Control.log.info("No id present");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(host==null){
			connection.sendInvalidMsg("No host present");
			Control.log.info("No host present");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		this.control.maintainServerState(id,host,load,port);

		for(Connection c:this.control.getConnections()){
			if(c.isAuthedServer()&&c!=connection){
				c.sendAnnounceMsg(id,load,host,port);
			}
		}

		return true;
	}
}
