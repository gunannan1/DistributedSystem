package activitystreamer.message.serverhandlers;

import activitystreamer.client.ClientSkeleton;
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

public class ServerInvalidHandler extends MessageHandler {

	private final Control control;

	public ServerInvalidHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		// TODO need future work
		Control.log.info("Invalid message received from {}",connection.getSocket().getRemoteSocketAddress());
		Control.log.info(json.get("info"));
		Control.log.info("Close connection with {}",connection.getSocket().getRemoteSocketAddress());
		connection.closeCon();
		this.control.connectionClosed(connection);
		// return false to close related connection and thread
		return false;
	}
}
