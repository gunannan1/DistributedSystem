package activitystreamer.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import Message.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import jdk.nashorn.internal.objects.NativeString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.json.simple.JSONObject;

import activitystreamer.util.Settings;

import javax.swing.*;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private TextFrame textFrame;
	private Socket s=null;
	DataInputStream in;
	DataOutputStream out;
	private String serverId;


	public static ClientSkeleton getInstance() throws IOException {
		if(clientSolution==null) {
			clientSolution = new ClientSkeleton();
		}
		return clientSolution;
	}
	
	public ClientSkeleton() throws IOException {
		// TODO initialise socket
         this.s=connectToServer(Settings.getRemoteHostname(),Settings.getRemotePort(),Settings.getUsername(),Settings.getSecret());
         this.in=new DataInputStream(this.s.getInputStream());
         this.out=new DataOutputStream((this.s.getOutputStream()));
		// new and open UI
		textFrame = new TextFrame();
		start();

	}
	
	
	// TODO estimate connection
	private Socket connectToServer(String host, int port, String user, String secret) throws IOException {
		//TODO what if receive a Re-direction command ?


			String remoteHost =host;
			int remotePort=port;
			String userName=user;
			String userSecret=secret;
			if (register(userName,userSecret,this.s)==true && login(userName,userSecret,this.s)==true)
			    return new Socket(remoteHost,remotePort);
			else
				return null;

	}


	private boolean register(String username, String secret, Socket socket){
		// TODO use existing socket to register
        //receive  REGISTER_SUCCESS-->return true

		return false;
	}

	private boolean authorise(String username, String secret, Socket socket){
		// TODO use existing socket to register

		return false;
	}
	private boolean login(String username, String secret, Socket socket)
    {
        //receive LOGIN_SUCCESS-->return true
        //receive AUTHENTICATION_FAIL-->return false
        return false;
    }
	
	
	
	@SuppressWarnings("unchecked")
	public void sendActivityObject(JSONObject activityObj){
<<<<<<< HEAD
		try {
		    ActivityMsg activityMsg=new ActivityMsg();
		    activityMsg.setUsername(Settings.getUsername());
		    activityMsg.setSecret(Settings.getSecret());
		    activityMsg.setObject(activityObj.toString());
		    activityMsg.setId(serverId);
		    String activityMessage = activityMsg.toJsonString();
			out.writeUTF(activityMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
=======
		// TODO send message via socket
>>>>>>> 31af276f860cfcc99772b2e972597fd44c0ee77f
	}
	
	
	public void disconnect(){
		// TODO close socket, close TextFrame
		try {
		    sendLogoutMsg();
			s.close();
			textFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//client receive thread
	public void run(){
<<<<<<< HEAD

        try {
            String data=this.in.readUTF();
            boolean term=false;
            while(!term && data !=null)
            {
                term=ClientSkeleton.getInstance().process(data);

            }
            log.debug("Connection closed in "+Settings.socketAddress(s));

        } catch (IOException e) {
            e.printStackTrace();
            log.error("Connection"+Settings.socketAddress(s)+" closed with exception "+e);
        }


    }
    public synchronized boolean process(String receivedJsonStr) {
        log.debug("Client received:" + receivedJsonStr);
        JsonObject receivedJson;
        try {
            receivedJson = new Gson().fromJson(receivedJsonStr, JsonObject.class);
        } catch (JsonSyntaxException e) {
            log.debug(("Client received msg failed. Not Json format:" + e.getMessage()));
            return true;
        }
        if (!containCommandField(receivedJson)) {
            return true;
        }
        String command = receivedJson.get("command").getAsString();
        switch (command)
        {
            case JsonMessage.ACTIVITY_BROADCAST:
                log.info("Activity broadcast received");
                textFrame.setOutputText(receivedJson);
                return false;

            case JsonMessage.REGISTER_SUCCESS:
                return processRegisterSuccessMsg(receivedJson);


            case JsonMessage.REGISTER_FAILED:
                return processRegisterFailedMsg(receivedJson);

            case JsonMessage.REDIRECT:
                return processRedirectMsg(receivedJson);


            case JsonMessage.AUTHENTICATION_FAIL:
                log.info("Server fails to authenticate");
                //close current connection
                disconnect();
                return true;


            case JsonMessage.LOGIN_SUCCESS:
                return processLoginSuccessMsg(receivedJson);

            case JsonMessage.LOGIN_FAILED:
                return processLoginFailedMsg(receivedJson);

            case JsonMessage.INVALID_MESSAGE:
                return processInvalidMsg(receivedJson);

            default:
                return processUnknownMsg(receivedJson);
        }
    }

    private boolean processUnknownMsg(JsonObject receivedJson) {
	    log.info("Unknown message received");
	    disconnect();
	    return true;
    }

    private boolean processInvalidMsg(JsonObject receivedJson) {
	    log.info("Client failed to send activity message to server");
	    String info=receivedJson.get("info").getAsString();
	    textFrame.showErrorMsg(info);
	    disconnect();
	    return true;
    }

    private boolean processLoginFailedMsg(JsonObject receivedJson) {
	    log.info("Login failed");
	    textFrame.setOutputText(receivedJson);
	    disconnect();
	    return true;
    }

    private boolean processLoginSuccessMsg(JsonObject receivedJson) {
	    log.info("Login success received");
	    log.debug("open GUI");
	    textFrame.setOutputText(receivedJson);

        return false;
    }

    private boolean processRedirectMsg(JsonObject receivedJson) {
	    log.info("Redirect");
	    //close current connection
        disconnect();

        //setup new host and port number
        serverId = receivedJson.get("id").getAsString();
        String newHost=receivedJson.get("hostname").getAsString();
        int newPort=receivedJson.get("port").getAsInt();
        Settings.setRemoteHostname(newHost);
        Settings.setRemotePort(newPort);

        //connect
        return true;
    }

    private boolean processRegisterFailedMsg(JsonObject receivedJson) {
	    log.info("Register failed");
	    textFrame.setOutputText(receivedJson);
	    disconnect();
	    return true;

    }

    private boolean processRegisterSuccessMsg(JsonObject receivedJson) {
	    log.info("Register success received");
	    String info=receivedJson.get("info").getAsString();
	    textFrame.setOutputText(receivedJson);
	    disconnect();
	    return true;

    }

    private boolean containCommandField (JsonObject receivedJsonObj) {
            if (!receivedJsonObj.has("command")) {
                InvalidMsg invalidMsg = new InvalidMsg();
                invalidMsg.setInfo("Message must contain field command");
                try {
                    this.out.writeUTF(invalidMsg.toJsonString());
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;

        }

        public void sendLoginMsg () {
            LoginMsg loginMsg = new LoginMsg();
            loginMsg.setUsername(Settings.getUsername());
            loginMsg.setSecrect(Settings.getSecret());
            String loginMessage = loginMsg.toJsonString();
            try {
                this.out.writeUTF(loginMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendRegisterMsg () {
            RegisterMsg registerMsg = new RegisterMsg();
            registerMsg.setUsername(Settings.getUsername());
            registerMsg.setSecret(Settings.getSecret());
            String registerMessage = registerMsg.toJsonString();
            try {
                this.out.writeUTF(registerMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void sendAnonymousLoginMsg () {
            AnonymousLoginMsg anonymousLoginMsg = new AnonymousLoginMsg();
            String anonymousMsg = anonymousLoginMsg.toJsonString();
            try {
                this.out.writeUTF(anonymousMsg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void sendLogoutMsg ()
        {
            LogoutMsg logoutMsg = new LogoutMsg();
            logoutMsg.setUsername(Settings.getUsername());
            logoutMsg.setSecret(Settings.getSecret());
            try {
                this.out.writeUTF(logoutMsg.toJsonString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
=======
		while(true) {
			// TODO receive message
			textFrame.setOutputText(new JSONObject());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
>>>>>>> 31af276f860cfcc99772b2e972597fd44c0ee77f

}
