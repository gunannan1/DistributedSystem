package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.util.Settings;
import com.google.gson.JsonObject;

import java.util.Set;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class LoginSuccHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public LoginSuccHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {

		ClientSkeleton.log.info("Login successfully to server {}", Settings.getRemoteHostname());
		ClientSkeleton.log.info("Start User Interface");
		this.clientSkeleton.startUI(String.format("Login successfully to server as user [%s]\n", Settings.getUsername()));
		return true;
	}
}
