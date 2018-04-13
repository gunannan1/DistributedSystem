package activitystreamer.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;


@SuppressWarnings("serial")
public class ServerTextFrame extends JFrame implements ActionListener {
	private static final Logger log = LogManager.getLogger("clientLogger");
	private JEditorPane clientListText;
	private JTextArea connectionsText;
	private JTextArea serversText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JSONParser parser = new JSONParser();

	//TODO need a socket to send/receive message, how to get this socket?
	private Socket socket;

	// TODO need a variable to hold threads created within this instance inreader order to close them when disconnect

	public ServerTextFrame() {
		setTitle("ActivityStreamer Text I/O");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 2));

		JPanel clientListPanel = new JPanel();
		clientListPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "Registered Clients");
		clientListPanel.setBorder(lineBorder);
		clientListText = new JEditorPane();
		clientListText.setEditable(false);

		HTMLEditorKit kit = new HTMLEditorKit();
		clientListText.setEditorKit(kit);

		Document doc = kit.createDefaultDocument();
		clientListText.setDocument(doc);
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule(".table { width: 100%; max-width: 100%; margin-bottom: 1rem; background-color: transparent;}");
		styleSheet.addRule(".table-bordered {border: 1; }");
		styleSheet.addRule(".table-bordered th, .table-bordered td { border: 1px solid #dee2e6 !important; }");
		styleSheet.addRule(".table thead th {vertical-align: bottom;  border-bottom: 2px solid #dee2e6;}");
		styleSheet.addRule(".table tbody + tbody { border-top: 2px solid #dee2e6;}");
		styleSheet.addRule(".table .table { background-color: #fff;}");


		JScrollPane scrollPane = new JScrollPane(clientListText);
		clientListPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel connectionsPanel = new JPanel();
		connectionsPanel.setLayout(new BorderLayout());
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "Unauthed Connections");
		connectionsPanel.setBorder(lineBorder);
		connectionsPanel.setName("Connectiong List");
		connectionsText = new JTextArea();
		connectionsText.setLineWrap(true);
		scrollPane = new JScrollPane(connectionsText);
		connectionsPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel serverListPanel = new JPanel();
		serverListPanel.setLayout(new BorderLayout());
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "Authened Servers");
		serverListPanel.setBorder(lineBorder);
		serverListPanel.setName("Server List");
		serversText = new JTextArea();
		serversText.setLineWrap(true);
		serversText.setBackground(Color.gray);
		scrollPane = new JScrollPane(serversText);
		serverListPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "Log Output");
		logPanel.setBorder(lineBorder);
		logPanel.setName("Log");
		serversText = new JTextArea();
		serversText.setLineWrap(true);
		serversText.setBackground(Color.gray);
		scrollPane = new JScrollPane(serversText);
		logPanel.add(scrollPane, BorderLayout.CENTER);


		mainPanel.add(clientListPanel);
		mainPanel.add(serverListPanel);
		mainPanel.add(connectionsPanel);
		mainPanel.add(logPanel);
		add(mainPanel);

		setLocationRelativeTo(null);
		setSize(1280, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

//		this.beginReceive();
	}

	public void setConnectionsText(String str) {
		connectionsText.append(str);
		connectionsText.revalidate();
		connectionsText.repaint();
	}

	//show error message
	public void showErrorMsg(String error) {
		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.INFORMATION_MESSAGE);
	}

	public void actionPerformed(ActionEvent e) {

	}

	public void setUserAreaText(String str) {
		clientListText.setText(str);
	}
}
