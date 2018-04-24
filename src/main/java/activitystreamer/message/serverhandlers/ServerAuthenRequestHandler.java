package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.util.Settings;
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
		String secret = null;
		try {
			secret = json.get("secret").getAsString();
		}catch(UnsupportedOperationException e){
			// Invalid message
			connection.sendInvalidMsg("secret is not provided");
			Control.log.info("secret is not provided");
			connection.closeCon();
			control.connectionClosed(connection);
			return false;

		}
		Control.log.info("process authentication for server{} with secret {}", connection.getSocket().getRemoteSocketAddress(), secret);

		if(connection.isAuthedServer()){
			connection.sendInvalidMsg("Already authenticated");
			Control.log.info("Already authenticated");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(connection.isAuthedClient()){
			connection.sendInvalidMsg("Authenticate is for server, not client");
			Control.log.info("Authenticate is for server, not client");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		else if(secret==null){
			connection.sendInvalidMsg("No secret present");
			Control.log.info("No secret present");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		//check if the secret is correct
		else if(!secret.equals(Settings.getSecret())){
			connection.sendAuthFailedMsg(String.format("The supplied secret is incorrect: %s",secret));
			Control.log.info(String.format("The supplied secret is incorrect: %s",secret));
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		Control.log.info("Auth successfully, accept new server {}",connection.getSocket().getRemoteSocketAddress());
		connection.setAuthed(true);
		connection.setServer(true);
		return true;
	}
}
