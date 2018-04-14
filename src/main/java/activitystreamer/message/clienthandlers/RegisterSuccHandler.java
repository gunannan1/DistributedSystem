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

public class RegisterSuccHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public RegisterSuccHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//register success, close connection
		clientSkeleton.disconnect();
		ClientSkeleton.log.info("Register successfully to server {}:{}", Settings.getRemoteHostname(),Settings.getRemotePort());
		ClientSkeleton.log.info("Close client");
		return false;// return false so that run loop will end
	}
}
