package activitystreamer.client;

import activitystreamer.message.Activity;

import activitystreamer.util.Settings;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Socket;


@SuppressWarnings("serial")
public class ClientTextFrame extends JFrame implements ActionListener {
	private static final Logger log = LogManager.getLogger("clientLogger");
	private JTextArea inputText;
	private JTextArea outputText;
	//	private JTextArea logText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JsonParser parser = new JsonParser();

	//TODO need a socket to send/receive message, how to get this socket?
	private Socket socket;

	// TODO need a variable to hold threads created within this instance inreader order to close them when disconnect

	public ClientTextFrame() {

		setTitle(String.format("Client-%s (%s)", ClientSkeleton.getInstance().getLocalAddress(), Settings.getUsername()));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1, 3));

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "JSON input, to send to server");
		inputPanel.setBorder(lineBorder);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), "JSON output, received from server");
		outputPanel.setBorder(lineBorder);
		outputPanel.setName("Text output");

		inputText = new JTextArea();
		inputText.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(inputText);
		inputPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonGroup = new JPanel();
		sendButton = new JButton("Send");
		disconnectButton = new JButton("Disconnect");
		buttonGroup.add(sendButton);
		buttonGroup.add(disconnectButton);
		inputPanel.add(buttonGroup, BorderLayout.SOUTH);
		sendButton.addActionListener(this);
		disconnectButton.addActionListener(this);

		outputText = new JTextArea();
		outputText.setLineWrap(true);
		scrollPane = new JScrollPane(outputText);
		outputPanel.add(scrollPane, BorderLayout.CENTER);


		mainPanel.add(inputPanel);
		mainPanel.add(outputPanel);
		add(mainPanel);

		setLocationRelativeTo(null);
		setSize(640, 384);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ClientSkeleton.getInstance().sendLogoutMsg();
			}
		});
	}


	// TODO maybe we can move this message handler into receive thread
	public void setOutputText(final JsonObject obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(obj.toString());
		String prettyJsonString = gson.toJson(je);
		setOutputText(prettyJsonString);
	}

	public void setOutputText(String info) {
		outputText.append(info);
		outputText.revalidate();
		outputText.repaint();
	}

	//show error message
	public void showErrorMsg(String error) {
		JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.INFORMATION_MESSAGE);
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sendButton) {
			String msg = inputText.getText().trim().replaceAll("\r", " ").replaceAll("\n", " ").replaceAll("\t", " ");
			JsonObject json;
			try {
//				Gson gson = new Gson();
				json = (JsonObject) parser.parse(msg);
				ClientSkeleton.getInstance().sendActivityObject(json);
			} catch (ClassCastException | JsonSyntaxException e1) {
				String error = String.format("Data not sent as the string you input is not a valid json string: %s",msg);
				log.error(error);
				showErrorMsg(error);
			}
			//Activity act = new Activity(msg);

			//ClientSkeleton.getInstance().sendActivityObject(act);

		} else if (e.getSource() == disconnectButton) {
			ClientSkeleton.getInstance().sendLogoutMsg();
			ClientSkeleton.getInstance().disconnect();
		}
	}

}
