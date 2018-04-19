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
	public boolean processMessage(JsonObject json, Connection connection) {

		Control.log.debug("Announcement received from {}" , connection.getSocket().getRemoteSocketAddress());
		String id = json.get("id").getAsString();
		int load = json.get("load").getAsInt();
		String host = json.get("hostname").getAsString();
		int port = json.get("port").getAsInt();

		if (!connection.isAuthedServer()) {
			connection.sendInvalidMsg("Received announce from an unauthenticated server");
			Control.log.info("Received announce from an unauthenticated server");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (id == null) {
			connection.sendInvalidMsg("No id present");
			Control.log.info("No id present");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (host == null) {
			connection.sendInvalidMsg("No host present");
			Control.log.info("No host present");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		this.control.maintainServerState(id, host, load, port);

		for (Connection c : this.control.getConnections()) {
			if (c.isAuthedServer() && c != connection) {
				Control.log.debug("Send announce to " + c.getSocket().getRemoteSocketAddress());
				c.sendAnnounceMsg(id, load, host, port);
			}
		}
		return true;
	}
}
