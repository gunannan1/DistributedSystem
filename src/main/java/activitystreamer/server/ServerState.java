package activitystreamer.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ServerState {
    private int port;
    private int load;
    private String host;
    private String id;
    private String updateTime;

    public ServerState(int port,int load,String host,String id){
        this.load=load;
        this.host=host;
        this.port=port;
        this.id=id;
        this.setUpdateTime();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
        this.setUpdateTime();

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        this.updateTime = timeFormat.format(Calendar.getInstance().getTime());
    }
}
