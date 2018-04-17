package activitystreamer.server;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.util.Settings;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Socket;


@SuppressWarnings("serial")
public class ServerTextFrame extends JFrame implements ActionListener {
	private static final Logger log = Control.log;
	private JEditorPane registeredUserArea;
	private JEditorPane loginUserArea;
	private JEditorPane serverArea;
	private JTextArea logText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JSONParser parser = new JSONParser();

	//TODO need a socket to send/receive message, how to get this socket?
	private Socket socket;

	// TODO need a variable to hold threads created within this instance inreader order to close them when disconnect

	public ServerTextFrame() {
		setTitle(String.format("Server-%s:%d", Settings.getLocalHostname(),Settings.getLocalPort()));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 1));

		JPanel upPanel = new JPanel();
		upPanel.setLayout(new GridLayout(1,3));
		mainPanel.add(upPanel);

		registeredUserArea = addHtmlPanel(upPanel, "Users Registered at this server");

		loginUserArea = addHtmlPanel(upPanel, "Users Logged in this server");

		serverArea = addHtmlPanel(upPanel, "Servers connected to this server");

		logText = addLogPanel(mainPanel);

		add(mainPanel);

		setLocationRelativeTo(null);
		setSize(640, 384);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Control.getInstance().setTerm(true);
			}
		});
	}

	public JTextArea addLogPanel(JPanel mainPanel) {
		JTextArea textArea;
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),"Log Output");
		logPanel.setBorder(lineBorder);
		logPanel.setName("Log");

		textArea = new JTextArea();
		textArea.setLineWrap(false);
		textArea.setFont(new Font(Font.DIALOG,Font.PLAIN,10));
		JScrollPane scrollPane = new JScrollPane(textArea);
		logPanel.add(scrollPane,BorderLayout.CENTER);
		scrollPane.setAutoscrolls(true);
		mainPanel.add(logPanel);

		return textArea;

	}

	public JEditorPane addHtmlPanel(JPanel mainPanel, String title) {

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

	public void setLoginUserArea(String str) {
		loginUserArea.setText(str);
	}

	public void setRegisteredArea(String str) {
		if(registeredUserArea!=null) registeredUserArea.setText(str);
	}

	public void setServerArea(String str) {
		this.serverArea.setText(str);
	}

	//show error message
	public void showErrorMsg(String error) {
		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.INFORMATION_MESSAGE);
	}
	public JTextArea getLogArea(){
		return logText;
	}

	public void actionPerformed(ActionEvent e) {

	}


}
