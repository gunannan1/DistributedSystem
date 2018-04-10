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

public class ServerAuthenRequestHandler extends MessageHandler {

	private final Control control;

	public ServerAuthenRequestHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		String secret = json.get("secret").getAsString();
		Control.log.debug("process authentication for server with secret {}", secret);
		connection.setAuthed(true);
		connection.setServer(true);
		return true;
	}
}
