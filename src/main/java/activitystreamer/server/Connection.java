package activitystreamer.server;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import activitystreamer.BackupServerInfo;
import activitystreamer.message.Activity;
import activitystreamer.message.MessageGenerator;

import activitystreamer.util.Settings;


public class Connection extends Thread {

	private BufferedReader inreader;
	private PrintWriter outwriter;
	private boolean open = false;
	private Socket socket;
	private boolean term=false;
	private boolean isAuthed = false;
	private User user;
	private boolean isMain = false; //TODO need to take care of this , maybe useless
	private String remoteServerHost;
	private int remoteServerPort;

	private ArrayList<BackupServerInfo> backupServers;
	//TODO reconnect to backup servers when target server crash
	//TODO when to remove a server from backServer list?

	Connection(Socket socket, Boolean isServer) throws IOException{
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	    inreader = new BufferedReader( new InputStreamReader(in));
	    outwriter = new PrintWriter(out, true);
	    this.socket = socket;
	    open = true;
		isAuthed = false;
		user = null;
	    start();
	}
	Connection(Socket socket) throws IOException{
		this(socket,false);
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
				outwriter.close();
				if(isMain){
					String info ="Connection to upstream server breaks, shutdown all services to clients and downstream servers";
					Control.log.info(info);
					Control.getInstance().setTerm(true);
					Control.getInstance().refreshUI();
					Control.getInstance().getServerTextFrame().showErrorMsg(info);
				}
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
			//TODO crash reconnection
			Control.getInstance().connectionClosed(this);
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
	public void setAuthed(boolean isAuthed,String host, int port){
		this.remoteServerHost = host;
		this.remoteServerPort = port;
		this.isAuthed = isAuthed;
	}

	public void setAuthed(boolean isAuthed){
		this.isAuthed = isAuthed;
	}

	public String getRemoteServerHost() {
		return remoteServerHost;
	}

	public int getRemoteServerPort() {
		return remoteServerPort;
	}

	public User getUser(){
		return user;
	}
	public void setUser(User u){
		this.user = u;
	}

	public void setServer(boolean isServer){
		if(isServer){
			this.user = null;
		}
	}

	public boolean isMain() {
		return isMain;
	}

	public void setMain(boolean main) {
		isMain = main;
	}

	public boolean isAuthedClient(){
		return user != null && isAuthed;
	}

	public boolean isAuthedServer() {
		return user == null && isAuthed;
	}

	public void sendInvalidMsg(String info){
		Control.log.debug("send invalid message to server with info={}",info);
		String invalidStr = MessageGenerator.invalid(info);
		this.writeMsg(invalidStr);
	}

	public void sendLoginSuccMsg(String info){
		Control.log.debug("send login succ message to client with info=[{}]",info);
		String loginSuccStr = MessageGenerator.loginSucc(info);
		this.writeMsg(loginSuccStr);
	}
	public void sendLoginFailedMsg(String info){
		Control.log.debug("send login failed message to client with info=[{}]",info);
		String loginFailedStr = MessageGenerator.loginFail(info);
		this.writeMsg(loginFailedStr);
	}

	public void sendRegisterSuccMsg(String username){
		String registerSucc = MessageGenerator.registerSucc(username);
		this.writeMsg(registerSucc);
	}
	public void sendRegisterFailedMsg(String username){
		String registerFail = MessageGenerator.registerFail(username);
		this.writeMsg(registerFail);
	}
	public void sendAuthMsg(String secret){
		String authenticate=MessageGenerator.authen(Settings.getSecret());
		this.writeMsg(authenticate);
	}

	public void sendAuthFailedMsg(String info){
		String authFail = MessageGenerator.authFail(info);
		this.writeMsg(authFail);
	}

	public void sendAnnounceMsg(String id, int load, String host, int port){
		String announce=MessageGenerator.serverAnnounce(id,load,host,port);
		this.writeMsg(announce);
	}

	public void sendActivityBroadcastMsg(String msg){
//		String activityBroadcast = MessageGenerator.actBroadcast(act);
		this.writeMsg(msg);
	}
	public void sendActivityBroadcastMsg(Activity act){
		String activityBroadcast = MessageGenerator.actBroadcast(act);
		this.writeMsg(activityBroadcast);
	}

	// User register messages
	public void sendLockAllowedMsg(String username,String secret) {
		String message = MessageGenerator.lockAllowed(username, secret);
		this.writeMsg(message);
	}
	public void sendLockDeniedMsg(String username,String secret){
		String message = MessageGenerator.lockDenied(username,secret);
		this.writeMsg(message);
	}

	public void sendRedirectMsg(String hostname, int port){
		String message=MessageGenerator.redirect(hostname,port);
		this.writeMsg(message);
	}

	public void setBackupServers(ArrayList<BackupServerInfo> bsInfo) {
		this.backupServers = bsInfo;
	}
}
