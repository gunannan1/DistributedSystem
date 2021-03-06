package activitystreamer.server;


import activitystreamer.BackupServerInfo;
import activitystreamer.message.Activity;
import activitystreamer.message.MessageGenerator;
import activitystreamer.util.Settings;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class Connection extends Thread {

	private BufferedReader inreader;
	private PrintWriter outwriter;
	private boolean open = false;
	private Socket socket;
	private boolean term = false;
	private boolean isAuthed = false;
	private User user;
	//	private boolean isMain = false; //TODO need to take care of this , maybe useless
	private String remoteServerHost;
	private int remoteServerPort;

	private ArrayList<BackupServerInfo> backupServers;
	//TODO reconnect to backup servers when target server crash
	//TODO when to remove a server from backServer list?

	private Connection(Socket socket, Boolean isServer) throws IOException {
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		inreader = new BufferedReader(new InputStreamReader(in));
		outwriter = new PrintWriter(out, true);
		this.socket = socket;
		open = true;
		isAuthed = false;
		user = null;
		start();
	}

	Connection(Socket socket) throws IOException {
		this(socket, false);
	}


	/*
		 * returns true if the message was written, otherwise false
		 */
	public void writeMsg(String msg) {
		if (open) {
			outwriter.println(msg);
			outwriter.flush();
		}
	}

	public void closeCon() {
		if (open) {
			Control.log.info("closing connection " + Settings.socketAddress(socket));
			try {
				term = true;
				inreader.close();
				outwriter.close();
//				if(isMain){
//					String info ="Connection to upstream server breaks, shutdown all services to clients and downstream servers";
//					Control.log.info(info);
//					Control.getInstance().setTerm(true);
//					Control.getInstance().refreshUI();
//					Control.getInstance().getServerTextFrame().showErrorMsg(info);
//				}
			} catch (IOException e) {
				// already closed?
				Control.log.error("received exception closing the connection " + Settings.socketAddress(socket) + ": " + e);
			}
		}
	}


	public void run() {
		try {
			String data;
			while (!term) {
				while (!term && (data = inreader.readLine()) != null) {
					Control.log.debug("receive data {}", data);
					term = !Control.getInstance().process(this, data);
				}
				Control.log.debug("connection closed to " + Settings.socketAddress(socket));

				if (isAuthedServer()) {
					Control.log.info("This server is authened, " +
							"need to connect to backup servers if there is any.");
					// Set this connect to false as the connection between servers is not authened
					isAuthed = false;
					if (reConnectBackupServer()) {
						String info = String.format("Reconnect to backup server %s:%s, sending AUTHENTICATE message",
								Settings.getRemoteHostname(), Settings.getRemotePort());
						Control.log.info(info);
						sendAuthMsg(Settings.getSecret());
					} else {
						Control.log.error("All backup servers are out of service, server terminates");
						this.closeCon();
						Control.getInstance().connectionClosed(this);
					}
				} else {
					term = true;
				}
			}

		} catch (IOException e) {
			Control.log.error("connection " + Settings.socketAddress(socket) + " closed with exception: " + e);
			Control.getInstance().connectionClosed(this);
		}
		open = false;
	}

	private boolean reConnectBackupServer() {
		if (backupServers != null && backupServers.size() > 0) {
			BackupServerInfo first = backupServers.get(0);
			if (!first.getHost().equals(Settings.getLocalHostname()) || first.getProt() != Settings.getLocalPort()) {
				for (BackupServerInfo sInfo : backupServers) {
					if (tryOneBackupServer(sInfo.getHost(), sInfo.getProt())) {
						Settings.setRemoteHostname(sInfo.getHost());
						Settings.setRemotePort(sInfo.getProt());
						return true;
					}
				}
			} else {
				Control.log.info("This server is the primary backup server, no need do reconnection.Wait for other servers.");
				return false;
			}
		}
		Control.log.info("no backup servers exist, server terminates");
		return false;
	}

	private boolean tryOneBackupServer(String host, int port) {
		Control.log.info("try to connect to backup server {}:{}", host, port);
		try {
			this.socket.close();
			Socket s = new Socket(host, port);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			inreader = new BufferedReader(new InputStreamReader(in));
			outwriter = new PrintWriter(out, true);
			return true;
		} catch (IOException e) {
			Control.log.info("re-connect backup server {}:{} failed.", host, port);
			return false;
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setAuthed(boolean isAuthed, String host, int port) {
		this.remoteServerHost = host;
		this.remoteServerPort = port;
		this.isAuthed = isAuthed;
	}

	public void setAuthed(boolean isAuthed) {
		this.isAuthed = isAuthed;
	}

	public String getRemoteServerHost() {
		return remoteServerHost;
	}

	public int getRemoteServerPort() {
		return remoteServerPort;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User u) {
		this.user = u;
	}

	public void setServer(boolean isServer) {
		if (isServer) {
			this.user = null;
		}
	}
//
//	public boolean isMain() {
//		return isMain;
//	}
//
//	public void setMain(boolean main) {
//		isMain = main;
//	}

	public boolean isAuthedClient() {
		return user != null && isAuthed;
	}

	public boolean isAuthedServer() {
		return user == null && isAuthed;
	}

	public void sendInvalidMsg(String info) {
		Control.log.debug("send invalid message to server with info={}", info);
		String invalidStr = MessageGenerator.invalid(info);
		this.writeMsg(invalidStr);
	}

	public void sendLoginSuccMsg(String info) {
		Control.log.debug("send login succ message to client with info=[{}]", info);
		String loginSuccStr = MessageGenerator.loginSucc(info);
		this.writeMsg(loginSuccStr);
	}

	public void sendLoginFailedMsg(String info) {
		Control.log.debug("send login failed message to client with info=[{}]", info);
		String loginFailedStr = MessageGenerator.loginFail(info);
		this.writeMsg(loginFailedStr);
	}

	public void sendRegisterSuccMsg(String username) {
		String registerSucc = MessageGenerator.registerSucc(username);
		this.writeMsg(registerSucc);
	}

	public void sendRegisterFailedMsg(String username) {
		String registerFail = MessageGenerator.registerFail(username);
		this.writeMsg(registerFail);
	}

	public void sendAuthMsg(String secret) {
		String authenticate = MessageGenerator.authen(Settings.getSecret());
		this.writeMsg(authenticate);
	}

	public void sendAuthFailedMsg(String info) {
		String authFail = MessageGenerator.authFail(info);
		this.writeMsg(authFail);
	}

	public void sendAnnounceMsg(String id, int load, String host, int port) {
		String announce = MessageGenerator.serverAnnounce(id, load, host, port);
		this.writeMsg(announce);
	}

	public void sendActivityBroadcastMsg(String msg) {
//		String activityBroadcast = MessageGenerator.actBroadcast(act);
		this.writeMsg(msg);
	}

	public void sendActivityBroadcastMsg(Activity act) {
		String activityBroadcast = MessageGenerator.actBroadcast(act);
		this.writeMsg(activityBroadcast);
	}

	public void sendActivityBroadcastMsg(Activity act,int sequence) {
		String activityBroadcast = MessageGenerator.actBroadcast(act,sequence);
		this.writeMsg(activityBroadcast);
	}

	// User register messages
	public void sendLockAllowedMsg(String username, String secret) {
		String message = MessageGenerator.lockAllowed(username, secret);
		this.writeMsg(message);
	}

	public void sendLockDeniedMsg(String username, String secret) {
		String message = MessageGenerator.lockDenied(username, secret);
		this.writeMsg(message);
	}

	public void sendRedirectMsg(String hostname, int port) {
		String message = MessageGenerator.redirect(hostname, port);
		this.writeMsg(message);
	}

	public void setBackupServers(ArrayList<BackupServerInfo> bsInfo) {
		this.backupServers = bsInfo;
	}
}
