package activitystreamer.server;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import activitystreamer.message.MessageGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.util.Settings;


public class Connection extends Thread {
	private DataInputStream in;
	private DataOutputStream out;
	private BufferedReader inreader;
	private PrintWriter outwriter;
	private boolean open = false;
	private Socket socket;
	private boolean term=false;
	private boolean isAuthed = false;
	private boolean isServer;
	
	Connection(Socket socket, Boolean isServer) throws IOException{
		in = new DataInputStream(socket.getInputStream());
	    out = new DataOutputStream(socket.getOutputStream());
	    inreader = new BufferedReader( new InputStreamReader(in));
	    outwriter = new PrintWriter(out, true);
	    this.socket = socket;
	    open = true;
		isAuthed = false;
	    start();
	}
	Connection(Socket socket) throws IOException{
		this(socket,false);
	}

	public void setServer(boolean server) {
		isServer = server;
	}

	/*
		 * returns true if the message was written, otherwise false
		 */
	public boolean writeMsg(String msg) {
		if(open){
			outwriter.println(msg);
			outwriter.flush();
			return true;	
		}
		return false;
	}
	
	public void closeCon(){
		if(open){
			Control.log.info("closing connection "+Settings.socketAddress(socket));
			try {
				term=true;
				inreader.close();
				out.close();
			} catch (IOException e) {
				// already closed?
				Control.log.error("received exception closing the connection "+Settings.socketAddress(socket)+": "+e);
			}
		}
	}
	
	
	public void run(){
		try {
			String data;
			while(!term && (data = inreader.readLine())!=null){
				Control.log.debug("receive data {}",data);
				term = !Control.getInstance().process(this,data);
			}
			Control.log.debug("connection closed to "+Settings.socketAddress(socket));
			Control.getInstance().connectionClosed(this);
			in.close();
		} catch (IOException e) {
			Control.log.error("connection "+Settings.socketAddress(socket)+" closed with exception: "+e);
			Control.getInstance().connectionClosed(this);
		}
		open=false;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public boolean isOpen() {
		return open;
	}

	public void setTerm(boolean term) {
		this.term = term;
		if(term) interrupt();
	}
	public void setAuthed(boolean isAuthed){
		this.isAuthed = isAuthed;
	}

	public boolean isAuthedClient(){
		return !isServer && isAuthed;
	}

	public boolean isAuthedServer() {
		return isServer && isAuthed;
	}

	// TODO implement send methods for different types of messages
	public void sendInvalidMsg(String info){
		Control.log.debug("send invalid message to server with info={}",info);
		String invalidStr = MessageGenerator.generateInvalid(info);
		this.writeMsg(invalidStr);
	}

	//TODO for client
	public void sendLoginSuccMsg(String info){
		Control.log.debug("send login succ message to client with info='{}'",info);
		String loginSuccStr = MessageGenerator.generateLoginSucc(info);
		this.writeMsg(loginSuccStr);
	}
	public void sendLoginFailedMsg(String info){
		Control.log.debug("send login failed message to client with info='{}'",info);
		String loginFailedStr = MessageGenerator.generateLoginFail(info);
		this.writeMsg(loginFailedStr);
	}

	public void sendRegisterSuccMsg(String username){
		String registerSucc = MessageGenerator.generateRegisterSucc(username);
		this.writeMsg(registerSucc);
	}
	public void sendRegisterFailedMsg(String username){
		String registerSucc = MessageGenerator.generateRegisterFail(username);
		this.writeMsg(registerSucc);
	}
	public void sendAuthMsg(){}

	public void sendAuthFailedMsg(){}
	public void sendAnnounceMsg(){}
	public void sendActiveBroadcastMsg(){}
	public void sendLockRequestMsg(){}
	public void sendLockAllowedMsg(){}
	public void sendLockDeniedMsg(){}

}
