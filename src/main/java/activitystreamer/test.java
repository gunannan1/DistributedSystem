package activitystreamer;

import activitystreamer.message.clienthandlers.ClientFailedMessageHandler;

/**
 * test
 * <p>
 * Author Ning Kang
 * Date 9/4/18
 */

public class test {

	public static void main(String[] argv){
		ClientFailedMessageHandler rh = new ClientFailedMessageHandler("ningk1","abc");
		System.out.println(rh.toJsonString());
	
	}

}
