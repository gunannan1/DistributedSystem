package activitystreamer.message;

import activitystreamer.server.Connection;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ClientHandler
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public abstract class MessageHandler {

	public abstract boolean processMessage(JsonObject json,Connection connection);

}
