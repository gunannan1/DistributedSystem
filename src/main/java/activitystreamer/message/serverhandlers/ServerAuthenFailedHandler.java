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

public class ServerAuthenFailedHandler extends MessageHandler {

	private final Control control;

	public ServerAuthenFailedHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("Process message {}", json.get("command"));
		Control.log.info(json.get("info").getAsString());
		connection.closeCon();
		this.control.connectionClosed(connection);
		// return false to close related connection and thread
		return false;
	}
}
