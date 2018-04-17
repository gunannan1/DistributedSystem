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

public class LoginFailedHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public LoginFailedHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {

		ClientSkeleton.log.info("Login failed to server {}:{}", Settings.getRemoteHostname(), Settings.getRemotePort());
		ClientSkeleton.log.info("Connection will be closed");
		return false;
	}
}
