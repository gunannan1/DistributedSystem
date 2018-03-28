package activitystreamer.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.util.Settings;

public class ClientSkeleton extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSkeleton clientSolution;
	private TextFrame textFrame;
	

	
	public static ClientSkeleton getInstance(){
		if(clientSolution==null){
			clientSolution = new ClientSkeleton();
		}
		return clientSolution;
	}
	
	public ClientSkeleton(){
		// TODO initialise socket

		// new and open UI
		textFrame = new TextFrame();
		start();
	}
	
	
	// TODO estimate connection
	private Socket connectToServer(String host, int port, String user, String secret){
		//TODO what if receive a Re-direction command ?
		return new Socket();
	}


	private boolean register(String username, String secret, Socket socket){
		// TODO use existing socket to register
		return false;
	}

	private boolean authorise(String username, String secret, Socket socket){
		// TODO use existing socket to register
		return false;
	}
	
	
	
	@SuppressWarnings("unchecked")
	//TODO probability not need this method here
	public void sendActivityObject(JSONObject activityObj){
		
	}
	
	
	public void disconnect(){
		// TODO close socket, close TextFrame
	}
	
	
	public void run(){

	}

	
}
