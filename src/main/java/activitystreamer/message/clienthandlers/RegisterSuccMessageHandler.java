package activitystreamer.message.clienthandlers;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class RegisterSuccMessageHandler extends MessageHandler {

	private final ClientSkeleton clientSkeleton;

	public RegisterSuccMessageHandler(ClientSkeleton clientSkeleton) {
		this.clientSkeleton = clientSkeleton;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		log.info("Register successfully to server {}",connection.getSocket().getRemoteSocketAddress());
		this.clientSkeleton.sendLoginMsg();
		return true;
	}
}
