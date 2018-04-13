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

import java.net.Socket;


@SuppressWarnings("serial")
public class ServerTextFrame extends JFrame implements ActionListener {
	private static final Logger log = LogManager.getLogger("clientLogger");
	private JEditorPane clientListText;
	private JEditorPane connectionsText;
	private JEditorPane serversText;
	private JEditorPane logText;
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

		clientListText = addHtmlPanel(mainPanel,"User Info");

		connectionsText = addHtmlPanel(mainPanel,"Connections");

		serversText = addHtmlPanel(mainPanel,"Servers");

		logText = addHtmlPanel(mainPanel,"Logging");

		add(mainPanel);

		setLocationRelativeTo(null);
		setSize(1280, 768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public JEditorPane addHtmlPanel(JPanel mainPanel,String title) {

		JPanel jp = new JPanel();
		jp.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), title);
		jp.setBorder(lineBorder);

		JEditorPane htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		htmlPane.setEditorKit(kit);
		Document doc = kit.createDefaultDocument();
		htmlPane.setDocument(doc);
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule(".table { width: 100%; max-width: 100%; margin-bottom: 1rem; background-color: transparent;}");
		styleSheet.addRule(".table-bordered {border: 1; }");
		styleSheet.addRule(".table-bordered th, .table-bordered td { border: 1px solid #dee2e6 !important; }");
		styleSheet.addRule(".table thead th {vertical-align: bottom;  border-bottom: 2px solid #dee2e6;}");
		styleSheet.addRule(".table tbody + tbody { border-top: 2px solid #dee2e6;}");
		styleSheet.addRule(".table .table { background-color: #fff;}");
		JScrollPane scrollPane = new JScrollPane(htmlPane);
		jp.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(jp);
		return htmlPane;
	}

	public void setConnectionsText(String str) {
		connectionsText.setText(str);
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
