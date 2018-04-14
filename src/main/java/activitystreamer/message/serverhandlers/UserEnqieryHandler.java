package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import activitystreamer.server.User;
import com.google.gson.JsonObject;

import java.util.HashMap;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class UserEnqieryHandler extends MessageHandler {
	private final Control control;

	public UserEnqieryHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {


		Control.log.info("login message is received");

		String username = json.get("username").getAsString();
		String secret = json.get("secret").getAsString();



		return true;
	}


}
