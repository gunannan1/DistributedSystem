package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageGenerator;
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
	public boolean processMessage(JsonObject json, Connection connection) {
//		if(control.isOutOfService()){
//			connection.sendAuthFailedMsg("This server is temporary out-of-service, try again later");
//			connection.closeCon();
//			control.connectionClosed(connection);
//			return false;
//		}

		String secret;
		String remoteServiceHost;
		int remoteServicePort;
		try {
			secret = json.get("secret").getAsString();
			remoteServiceHost = json.get("host").getAsString();
			remoteServicePort = json.get("port").getAsInt();
			Control.log.info("process authentication for server{} with secret {}", connection.getSocket().getRemoteSocketAddress(), secret);
		} catch (NullPointerException | UnsupportedOperationException e) {
			String error = String.format("Invaid AUTHEN message received:%s. Need {secret,host,port}", json.toString());
			Control.log.info(error);
			return false;
		}
		if (connection.isAuthedServer()) {
			connection.sendInvalidMsg("Already authenticated");
			Control.log.info("Already authenticated");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (connection.isAuthedClient()) {
			connection.sendInvalidMsg("Authenticate message is for server, not client");
			Control.log.info("Authenticate message is for server, not client");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		} else if (secret == null) {
			connection.sendInvalidMsg("No secret present");
			Control.log.info("No secret present");
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}

		//check if the secret is correct
		else if (!secret.equals(Settings.getSecret())) {
			connection.sendAuthFailedMsg(String.format("The supplied secret is incorrect: %s", secret));
			Control.log.info(String.format("The supplied secret is incorrect: %s", secret));
			connection.closeCon();
			this.control.connectionClosed(connection);
			return false;
		}


		Control.log.info("Auth successfully, accept new server {}", connection.getSocket().getRemoteSocketAddress());
		connection.setAuthed(true, remoteServiceHost, remoteServicePort);
		connection.setServer(true);

		String userList = MessageGenerator.authenSucc(control.getUserList(),control.getActivitySeqMap());
		connection.writeMsg(userList);
		return true;
	}
}
