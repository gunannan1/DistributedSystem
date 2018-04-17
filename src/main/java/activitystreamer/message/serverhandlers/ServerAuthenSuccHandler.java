package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;

/**
 * ServerAuthenSuccHandler
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerAuthenSuccHandler extends MessageHandler {

	private final Control control;

	public ServerAuthenSuccHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		Control.log.info("Process message {}", json.get("command"));
		control.startListener();
		// return false to close related connection and thread
		return true;
	}
}
