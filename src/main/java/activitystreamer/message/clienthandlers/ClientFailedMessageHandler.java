package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONObject;

import java.util.logging.Logger;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ClientFailedMessageHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public ClientFailedMessageHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		this.clientSkeleton.log.error(json.get("info"));
		this.clientSkeleton.disconnect();
		return false;

	}
}
