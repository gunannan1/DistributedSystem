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

public class UserLogoutHandler extends MessageHandler {

	private final Control control;

	public UserLogoutHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Logout request from {} is recieved.",connection.getSocket().getRemoteSocketAddress());
		connection.closeCon();
		control.connectionClosed(connection);
		return true;
	}
}
