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

public class LoginSuccMessageHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public LoginSuccMessageHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		clientSkeleton.log.info("Login successfully to server {} as user='{}' and secret='{}'", Settings.getRemoteHostname(), Settings.getUsername(), Settings.getSecret());
		this.clientSkeleton.startUI();
		this.clientSkeleton.sendLoginMsg();
		return true;
	}
}
