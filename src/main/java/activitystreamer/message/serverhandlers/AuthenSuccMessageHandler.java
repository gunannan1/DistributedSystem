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

public class AuthenSuccMessageHandler extends MessageHandler {

	private final Control control;

	public AuthenSuccMessageHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		this.log.info("Process message {}", json.get("command"));
		this.control.startListener();
		// return false to close related connection and thread
		return true;
	}
}
