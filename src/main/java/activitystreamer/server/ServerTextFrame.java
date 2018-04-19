package activitystreamer.server;

import activitystreamer.util.Settings;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;


@SuppressWarnings("serial")
public class ServerTextFrame extends JFrame implements ActionListener {
	private static final Logger log = Control.log;
	private JEditorPane registeredUserArea;
	private JEditorPane loginUserArea;
	private JEditorPane serverArea;
	private JEditorPane loadArea;
	private JTextArea logText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JSONParser parser = new JSONParser();

	//TODO need a socket to send/receive message, how to get this socket?
	private Socket socket;

	// TODO need a variable to hold threads created within this instance inreader order to close them when disconnect

	public ServerTextFrame() {
		setTitle(String.format("Server-%s:%d", Settings.getLocalHostname(), Settings.getLocalPort()));
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 1));

		JPanel upPanel = new JPanel();
		upPanel.setLayout(new GridLayout(2, 2));
		mainPanel.add(upPanel);

		registeredUserArea = addHtmlPanel(upPanel, "Users Registered at this server");
		loginUserArea = addHtmlPanel(upPanel, "Users Logged in this server");
		serverArea = addHtmlPanel(upPanel, "Servers connected to this server");
//		serverArea.setFont(new Font(Font.DIALOG,Font.PLAIN,10));
		loadArea = addHtmlPanel(upPanel, "Server Loads");
//		loadArea.setFont(new Font(Font.DIALOG,Font.PLAIN,10));

		logText = addTextPanel(mainPanel, "Log");

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

	public JTextArea addTextPanel(JPanel mainPanel, String title) {
		JTextArea textArea;
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), title);
		logPanel.setBorder(lineBorder);
		logPanel.setName("Log");

		textArea = new JTextArea();
		textArea.setLineWrap(false);
		textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		JScrollPane scrollPane = new JScrollPane(textArea);
		logPanel.add(scrollPane, BorderLayout.CENTER);
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
		try {
			kit.insertHTML((HTMLDocument) doc, 0, "<div id='table-div'></div>", 0, 0, HTML.Tag.DIV);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		updateTableInfo(loginUserArea,str);
	}

	public void setRegisteredArea(String str) {
		updateTableInfo(registeredUserArea,str);
	}

	public void setServerArea(String str) {
		updateTableInfo(serverArea,str);
	}

	public void setLoadArea(String str) {
		updateTableInfo(loadArea,str);
	}

	private void updateTableInfo(JEditorPane j, String str){
		HTMLDocument doc = (HTMLDocument) j.getDocument();
		try {
			Element body = doc.getElement("table-div");
			doc.setInnerHTML(body, str);
		} catch (Exception e) {
			Control.log.error("UI window updating fails.");
		}
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
