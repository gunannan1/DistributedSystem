package activitystreamer.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.org.apache.regexp.internal.RE;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.File;
import java.net.Socket;
import java.util.logging.SocketHandler;

/**
 * ReceiveThread
 * <p>
 * Author Ning Kang
 * Date 28/3/18
 */

public class ReceiveThread implements Runnable{
	private Socket socket;
	private JTextArea printArea;
	private static int test = 0;

	public ReceiveThread(Socket socket, JTextArea printArea) {
		this.socket = socket;
		this.printArea = printArea;
	}

	@Override
	public void run() {
		// this is just for testing
		while(true) {
			printArea.append("Test #" + ReceiveThread.test++ + System.lineSeparator());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// TODO receive message from socket and show it in printArea
		// TODO what if lost connection ?
	}

	public void setOutputText(final JSONObject obj){
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(obj.toJSONString());
		String prettyJsonString = gson.toJson(je);
		printArea.setText(prettyJsonString);
		printArea.revalidate();
		printArea.repaint();
	}
}
