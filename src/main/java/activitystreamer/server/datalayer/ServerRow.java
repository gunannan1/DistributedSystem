package activitystreamer.server.datalayer;

import activitystreamer.message.MessageType;
import activitystreamer.server.networklayer.Connection;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * ServerRow
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ServerRow implements IRow {
	private String serverId;
	private int load;
	private String ip;
	private int port;
	private long updateTime;

	public ServerRow(String serverId, int load, String ip, int port) {
		this.serverId = serverId;
		this.load = load;
		this.ip = ip;
		this.port = port;
		this.updateTime = Calendar.getInstance().getTimeInMillis();
	}

	public String getServerId() {
		return serverId;
	}

	public int getLoad() {
		return load;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public long getUpdateTime() {
		return updateTime;
	}
	public String getUpdateTimeString() {
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
		return timeFormat.format(updateTime);
	}

	@Override
	public boolean lock() {
		return false;
	}

	@Override
	public boolean unlock() {
		return false;
	}

	@Override
	public ServerRow update(IRow server) {
		// TODO useless, only override existing one
		return null;
	}

	@Override
	public String getId() {
		return serverId;
	}

	public void notifyChange(){
		notifyChange(null);
	}

	public void notifyChange(Connection connection){
		String resultBroadcastStr = serverUpdateJsonString();
		NetworkLayer.getNetworkLayer().broadcastToServers(resultBroadcastStr, connection);
	}

	private String serverUpdateJsonString(){
		JsonObject json = this.toJson();
		json.addProperty("command", MessageType.SERVER_ANNOUNCE.name());
		return json.toString();
	}

	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("serverId",serverId);
		json.addProperty("load",load);
		json.addProperty("ip",ip);
		json.addProperty("port",port);
		return json;
	}
}