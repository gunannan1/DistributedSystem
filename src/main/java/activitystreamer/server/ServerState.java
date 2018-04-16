package activitystreamer.server;

public class ServerState {
    private int port;
    private int load;
    private String host;
    private String id;

    public ServerState(int port,int load,String host,String id){
        this.load=load;
        this.host=host;
        this.port=port;
        this.id=id;
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
}
