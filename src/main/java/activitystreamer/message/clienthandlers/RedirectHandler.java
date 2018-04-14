package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class RedirectHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public RedirectHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		ClientSkeleton.log.info("redirect message from server {}", Settings.getRemoteHostname());
		String newRemoteHost = json.get("hostname").getAsString();
		int newRemotePort = json.get("port").getAsInt();
		clientSkeleton.redirectToServer(newRemoteHost,newRemotePort);
		return true;
	}
}
