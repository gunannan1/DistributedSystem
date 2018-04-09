package activitystreamer;

import Message_Vivian.JsonMessage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.util.Settings;

import java.io.IOException;
import java.net.InetAddress;

public class Client {
	
	private static final Logger log = LogManager.getLogger();
	
	private static void help(Options options){
		String header = "An ActivityStream Client for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Client", header, options, footer, true);
		System.exit(-1);
	}
	
	public static void main(String[] args) throws IOException {
		
		log.info("reading command line options");
		
		Options options = new Options();
		// TODO new option is required to separate "connect" and "register"

		options.addOption("u",true,"username");
		options.addOption("rp",true,"remote port number");
		options.addOption("rh",true,"remote hostname");
		options.addOption("s",true,"secret for username");
		options.addOption("r",false," user register, enter u,s then");
		options.addOption("c",false,"start connection, enter u, s, rh, rp then");
		options.addOption("a",false,"anonymous login");
		// build the parser
		CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			help(options);
		}
		// connect
		if (cmd.hasOption("c")) {
			if (cmd.hasOption("rh")) {
				String remoteHostname = cmd.getOptionValue("rh");
				int timeOut = 3000;
				boolean status = InetAddress.getByName(remoteHostname).isReachable(timeOut);
				if (status == true)
					Settings.setRemoteHostname(remoteHostname);
				else {
					log.error("The provided remote host name is not reachable, please try again");
					help(options);
				}
			}
			//TODO what if remote host is not provided--finished
			if (cmd.hasOption("rp")) {
				try {
					int port = Integer.parseInt(cmd.getOptionValue("rp"));
					Settings.setRemotePort(port);
				} catch (NumberFormatException e) {
					log.error("-rp requires a port number, parsed: " + cmd.getOptionValue("rp"));
					help(options);
				}
			}
			//TODO what if remote port is not provided--finished


			//TODO what if no user information is provided or only username is provided ?
			if (cmd.hasOption("s")) {
				Settings.setSecret(cmd.getOptionValue("s"));
			}

			if (cmd.hasOption("u")) {
				Settings.setUsername(cmd.getOptionValue("u"));
			}

			log.info("starting client");
			try {
				ClientSkeleton c = ClientSkeleton.getInstance();
				c.sendLoginMsg();

			} catch (IOException e) {
				log.error("client starts fail");
			}
		}

		//Register
        if(cmd.hasOption("r"))
		{
			if(cmd.hasOption("u"))
			{
              Settings.setUsername(cmd.getOptionValue("u"));
			}
			if(cmd.hasOption("s"))
			{
				Settings.setSecret(cmd.getOptionValue("s"));
			}
			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendRegisterMsg();

		}
		//anonymous login
		if(cmd.hasOption("a"))
		{
			Settings.setUsername((JsonMessage.ANONYMOUS_USERNAME));
			Settings.setSecret("");
            ClientSkeleton c=ClientSkeleton.getInstance();
            c.sendAnonymousLoginMsg();


		}
			
		
	}

	
}
