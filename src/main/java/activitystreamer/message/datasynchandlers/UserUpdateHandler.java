package activitystreamer.message.datasynchandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.datalayer.DataLayer;
import activitystreamer.server.datalayer.UserRow;
import activitystreamer.server.networklayer.Connection;
import com.google.gson.JsonObject;

/**
 * UserUpdate
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class UserUpdateHandler extends MessageHandler {
	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		DataLayer.log.info("Update single user info");

		// Validate message
		try {
			UserRow userRow = new UserRow(json);
			DataLayer.getInstance().updateOrInsert(userRow);
			userRow.notifyChange(connection);

		} catch (Exception e) {
			String error = "USER_UPDATE message invalid";
			DataLayer.log.error(error);
			connection.sendInvalidMsg(error);
			return false;
		}

		return true;
	}
}
