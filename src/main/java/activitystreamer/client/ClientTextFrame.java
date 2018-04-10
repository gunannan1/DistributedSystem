package activitystreamer.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

import javax.swing.*;
import javax.swing.border.Border;

import activitystreamer.message.Activity;
import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SuppressWarnings("serial")
public class ClientTextFrame extends JFrame implements ActionListener {
	private static final Logger log = LogManager.getLogger("clientLogger");
	private JTextArea inputText;
	private JTextArea outputText;
	private JTextArea logText;
	private JButton sendButton;
	private JButton disconnectButton;
	private JSONParser parser = new JSONParser();

	//TODO need a socket to send/receive message, how to get this socket?
	private Socket socket;

	// TODO need a variable to hold threads created within this instance inreader order to close them when disconnect
	
	public ClientTextFrame(){
		setTitle("ActivityStreamer Text I/O");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1,3));

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),"JSON input, to send to server");
		inputPanel.setBorder(lineBorder);

		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new BorderLayout());
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),"JSON output, received from server");
		outputPanel.setBorder(lineBorder);
		outputPanel.setName("Text output");

		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),"Log Output");
		logPanel.setBorder(lineBorder);
		logPanel.setName("Log");

		
		inputText = new JTextArea();
		inputText.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(inputText);
		inputPanel.add(scrollPane,BorderLayout.CENTER);
		
		JPanel buttonGroup = new JPanel();
		sendButton = new JButton("Send");
		disconnectButton = new JButton("Disconnect");
		buttonGroup.add(sendButton);
		buttonGroup.add(disconnectButton);
		inputPanel.add(buttonGroup,BorderLayout.SOUTH);
		sendButton.addActionListener(this);
		disconnectButton.addActionListener(this);

		logText = new JTextArea();
		logText.setLineWrap(true);
		logText.setBackground(Color.gray);
		scrollPane = new JScrollPane(logText);
		logPanel.add(scrollPane,BorderLayout.CENTER);

		outputText = new JTextArea();
		outputText.setLineWrap(true);
		scrollPane = new JScrollPane(outputText);
		outputPanel.add(scrollPane,BorderLayout.CENTER);

		
		mainPanel.add(inputPanel);
		mainPanel.add(outputPanel);
		mainPanel.add(logPanel);
		add(mainPanel);
		
		setLocationRelativeTo(null); 
		setSize(1280,768);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

//		this.beginReceive();
	}

	public JTextArea getLogTextArea() {
		return logText;
	}

	// TODO maybe we can move this message handler into receive thread
	public void setOutputText(final JsonObject obj){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(obj.toString());
		String prettyJsonString = gson.toJson(je);
		outputText.append(prettyJsonString);
		outputText.revalidate();
		outputText.repaint();
//		outputText.append("Test step ");
	}

//	//TODO how to handel message from server
//	private void beginReceive(){
//		// TODO what if disconnect ?
//		// TODO what if lost connection ?
//		try {
//			ReceiveThread receiveThread = new ReceiveThread(this.socket, this.outputText);
//			receiveThread.run();
//		}catch (Exception e)
//		{
//			log.error(e.getMessage());
//		}
//
//	}
	//show error message
	public void showErrorMsg(String error)
	{
		JOptionPane.showMessageDialog(null,error,"Error",JOptionPane.INFORMATION_MESSAGE);
	}



	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==sendButton){
			String msg = inputText.getText().trim().replaceAll("\r"," ").replaceAll("\n"," ").replaceAll("\t", " ");
			try {
				Activity act = new Activity(msg);
				ClientSkeleton.getInstance().sendActivityObject(act);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} else if(e.getSource()==disconnectButton){
			// TODO need to take care of the receiving thread
			try {
				ClientSkeleton.getInstance().disconnect();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
