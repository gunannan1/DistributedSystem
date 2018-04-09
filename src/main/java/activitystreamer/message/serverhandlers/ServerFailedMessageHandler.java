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

public class ServerFailedMessageHandler extends MessageHandler {

	private final Control control;

	public ServerFailedMessageHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		this.log.error(json.get("info"));
		connection.setTerm(true);
		// return false to close related connection and thread
		return false;
	}
}
