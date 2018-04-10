package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ActivityBroadcastHandler extends MessageHandler {

	private final Control control;

	public ActivityBroadcastHandler(Control control) {
		this.control = control;
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		//TODO need future work
		Control.log.info("Activity broadcast received");

		return true;
	}
}
