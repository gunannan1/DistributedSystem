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

public class UserRegisterMessageHandler extends MessageHandler {

	public static int lockRequestId = 0;
	private HashMap<String,LockResult> lockResultHashMap;
	private final Control control;

	public UserRegisterMessageHandler(Control control) {
		this.control = control;
		this.lockResultHashMap = new HashMap<>();
	}

	@Override
	public boolean processMessage(JsonObject json,Connection connection) {
		String username = json.get("username").getAsString();
		if(!this.control.checkUserExists(username)){ // if not exist locally
			if(!this.lockResultHashMap.containsKey(username)){ // if not in request list
				this.control.broadcast();//TODO broadcast lock_request, may need another method
			}else{ // is already in request list and waiting for other servers' response
				log.error("Username:{} is in registering process, waiting for other servers response.");
				return false;
			}
		}

		connection.setTerm(true);
		// return false to close related connection and thread
		return false;
	}

	class LockResult{
		public int lockRequestId ;
		private int enqueryServerCount;

		public LockResult(int lockRequestId, int enqueryServerCount) {
			this.lockRequestId = lockRequestId;
			this.enqueryServerCount = enqueryServerCount;
		}
	}
}
