package activitystreamer.message.serverhandlers;

import activitystreamer.message.MessageHandler;
import activitystreamer.server.Connection;
import activitystreamer.server.Control;
import com.google.gson.JsonObject;

import java.util.HashMap;

/**
 * RegisterMessage
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class ServerRegisterMessageHandler extends MessageHandler {

	public static int lockRequestId = 0;
	private HashMap<String, LockResult> lockResultHashMap;
	private final Control control;

	public ServerRegisterMessageHandler(Control control) {
		this.control = control;
		this.lockResultHashMap = new HashMap<>();
	}

	@Override
	public boolean processMessage(JsonObject json, Connection connection) {
		String secret = json.get("secret").getAsString();
		Control.log.debug("process authentication for server with secret {}", secret);
		connection.setAuthed(true);
		connection.setServer(true);
		this.control.changeServerLoads(1);
		return true;
	}

	class LockResult {
		public int lockRequestId;
		private int enqueryServerCount;

		public LockResult(int lockRequestId, int enqueryServerCount) {
			this.lockRequestId = lockRequestId;
			this.enqueryServerCount = enqueryServerCount;
		}
	}
}
