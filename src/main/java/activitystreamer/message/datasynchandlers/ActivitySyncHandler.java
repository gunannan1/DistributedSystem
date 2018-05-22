package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/**
 * ActivitySyncHandler
 * <p>
 * Author Ning Kang
 * Date 20/5/18
 */


public class ActivitySyncHandler extends MessageHandler {
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		//TODO pending
		return false;
	}
}
