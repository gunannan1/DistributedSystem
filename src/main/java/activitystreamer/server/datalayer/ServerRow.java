package activitystreamer.server.datalayer;

import activitystreamer.message.MessageType;
import activitystreamer.message.datasynchandlers.ServerAnnounceHandler.AnnounceType;
import activitystreamer.server.networklayer.NetworkLayer;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * ServerRow
 * <p>
 * Author Ning Kang
 * Date 18/5/18
 */

public class ServerRow implements IRow , Serializable {
	private String serverId;
	private int load;
	private String ip;
	private int port;
	private long updateTime;
	private boolean online;

	public ServerRow(String serverId, int load, String ip, int port) {
		this.serverId = serverId;
		this.load = load;
		this.ip = ip;
		this.port = port;
		this.updateTime = Calendar.getInstance().getTimeInMillis();
		this.online = true;
	}
	public ServerRow(String serverId, boolean online) {
		this.serverId = serverId;
		this.online = online;
	}

	public ServerRow(ServerRow serverRow){
		this.serverId = serverRow.serverId;
		this.load = serverRow.load;
		this.ip = serverRow.ip;
		this.port = serverRow.port;
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

	public ServerRow copy(){
		return new ServerRow(this);
	}

	@Override
	public String getId() {
		return serverId;
	}

	public void notifyChange(){
		String resultBroadcastStr = serverUpdateJsonString();
		NetworkLayer.getNetworkLayer().broadcastToServers(resultBroadcastStr, null);
	}

	private String serverUpdateJsonString(){
		JsonObject json = this.toJson();
		json.addProperty("command", MessageType.SERVER_ANNOUNCE.name());
		return json.toString();
	}

	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		json.addProperty("serverId",serverId);
		if(online) {
			json.addProperty("load", load);
			json.addProperty("ip", ip);
			json.addProperty("port", port);
			json.addProperty("action",AnnounceType.UPDATE_OR_INSERT.name());
		}else{
			json.addProperty("load", 0);
			json.addProperty("ip", "");
			json.addProperty("port", 0);
			json.addProperty("action",AnnounceType.DELETE.name());
		}
		return json;
	}
}