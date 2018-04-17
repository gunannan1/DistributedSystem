package activitystreamer.message.clienthandlers;

import activitystreamer.Client;
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
	public boolean processMessage(JsonObject json, Connection connection) {
		//TODO need future work
		try {
			ClientSkeleton.log.info("redirect message received from server {}:{}", Settings.getRemoteHostname(),Settings.getRemotePort());

			String newRemoteHost = json.get("hostname").getAsString();
			int newRemotePort = json.get("port").getAsInt();

			ClientSkeleton.log.info("redirect to server {}:{}", newRemoteHost,newRemotePort);

			Settings.setRemoteHostname(newRemoteHost);
			Settings.setRemotePort(newRemotePort);

			clientSkeleton.redirectToServer(newRemoteHost, newRemotePort);

			clientSkeleton.sendLoginMsg();

			return true;
		}catch (NullPointerException e){
			ClientSkeleton.log.info("Invalid redirection message received:{}",json.toString());
			connection.closeCon();
		}
		return false;
	}
}
