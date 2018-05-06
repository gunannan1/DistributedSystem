package activitystreamer.server;

import activitystreamer.UIPanelCreator;
import activitystreamer.util.Settings;

import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

import javax.swing.text.Element;

import javax.swing.text.html.HTMLDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;


@SuppressWarnings("serial")
public class ServerTextFrame extends JFrame implements ActionListener {

	private DefaultTableModel registeredUserArea;
	private DefaultTableModel loginUserArea;
	private DefaultTableModel serverArea;
	private DefaultTableModel loadArea;
	private JTextArea logText;


	public ServerTextFrame() {
		setTitle(String.format("Server:(%s:%d)", Settings.getLocalHostname(), Settings.getLocalPort()));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 1));

		JPanel upPanel = new JPanel();
		upPanel.setLayout(new GridLayout(2, 2));
		mainPanel.add(upPanel);

		registeredUserArea = UIPanelCreator.addTablePanel(upPanel, "Users Registered at this server",new String[]{"Username","Secret","Load"});
		loginUserArea = UIPanelCreator.addTablePanel(upPanel, "Users Logged in this server",new String[]{"Username","Secret","Load"});
		serverArea = UIPanelCreator.addTablePanel(upPanel, "Servers directly-connected to this server",new String[]{"host","port"});
		loadArea = UIPanelCreator.addTablePanel(upPanel, "Server Loads",new String[]{"IP","Port","Load","Update Time"});

		logText = UIPanelCreator.addTextPanel(mainPanel, "Log");

		add(mainPanel);
		setLocationRelativeTo(null);
		setSize(640, 640);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Control.getInstance().closeAll();
			}
		});
	}



	public void setLoginUserArea(ArrayList<Connection> connections) {
		loginUserArea.setRowCount(0);
		for(Connection c:connections) {
			if(c.isAuthedClient()) {
				User u = c.getUser();
				loginUserArea.addRow(new String[]{u.getUsername(), u.getSecret()});
			}
		}
		//updateTableInfo(loginUserArea,str);
	}

	public void setRegisteredArea(Collection<User> registerUsers) {
		registeredUserArea.setRowCount(0);
		for(User u:registerUsers){
			registeredUserArea.addRow(new String[]{u.getUsername(),u.getSecret()});
		}
	}

	public void setServerArea(ArrayList<Connection> connections) {
		serverArea.setRowCount(0);
		for(Connection c:connections) {
			if(c.isAuthedServer()) {
				serverArea.addRow(new String[]{
						c.getRemoteServerHost(),
						Integer.toString(c.getRemoteServerPort())
				});
			}
		}
	}

	public void setLoadArea(Collection<ServerState> serverStates) {
		loadArea.setRowCount(0);
		for(ServerState ss:serverStates) {
			String host = ss.getHost();
			String port = Integer.toString(ss.getPort());
			String load = Integer.toString(ss.getLoad());
			String time = ss.getUpdateTime();
			loadArea.addRow(new String[]{host,port,load,time});
		}
//		updateTableInfo(loadArea,str);
	}



	//show error message
	public void showErrorMsg(String error) {
		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.INFORMATION_MESSAGE);
	}

	public JTextArea getLogArea() {
		return logText;
	}

	public void actionPerformed(ActionEvent e) {

	}
}
