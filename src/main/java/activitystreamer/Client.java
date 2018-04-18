package activitystreamer;

import activitystreamer.client.ClientSkeleton;
import activitystreamer.util.Settings;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;

public class Client {

	private static final Logger log = LogManager.getLogger();

	private static void help(Options options) {
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
		options.addOption("r", false, " user register, enter u,s then");
		options.addOption("l", false, " user login");

		options.addOption("u", true, "username");
		options.addOption("s", true, "secret for username");

		options.addOption("rp", true, "remote port number");
		options.addOption("rh", true, "remote hostname");

		options.addOption("a", false, "anonymous login");
		// build the parser
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e1) {
			help(options);
		}

//		if (cmd.hasOption("c")) {
		if (cmd.hasOption("rh")) {
			String remoteHostname = cmd.getOptionValue("rh");
			int timeOut = 3000;
			boolean status = InetAddress.getByName(remoteHostname).isReachable(timeOut);
			if (status) {
				log.debug("Set remote host to {}", remoteHostname);
				Settings.setRemoteHostname(remoteHostname);
			} else {
				log.error("The provided remote host name is not reachable, please try again");
				help(options);
			}
		}
		//TODO what if remote host is not provided--finished
		if (cmd.hasOption("rp")) {
			try {
				int port = Integer.parseInt(cmd.getOptionValue("rp"));
				log.debug("Set remote port to {}", port);
				Settings.setRemotePort(port);
			} catch (NumberFormatException e) {
				log.error("-rp requires a port number, parsed: " + cmd.getOptionValue("rp"));
				help(options);
			}
		}

		//TODO what if no user information is provided or only username is provided ?
		if (cmd.hasOption("s")) {
			Settings.setSecret(cmd.getOptionValue("s"));
		}

		if (cmd.hasOption("u")) {
			Settings.setUsername(cmd.getOptionValue("u"));
		}

		log.info("starting client");
//		try {
//			ClientSkeleton c = ClientSkeleton.getInstance();
//			c.sendLoginMsg();
//
//		} catch (IOException e) {
//			log.error("client starts fail");
//		}
//		}

		// user info
		if (cmd.hasOption("u")) {
			Settings.setUsername(cmd.getOptionValue("u"));
		}
		if (cmd.hasOption("s")) {
			Settings.setSecret(cmd.getOptionValue("s"));
		}
//		else if(cmd.hasOption("r")){
//			log.error("no secret provided,please check your command");
//			System.exit(-1);
//		}



		//Register

		if (cmd.hasOption("r")) {
			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendRegisterMsg();
		}

		//anonymous login
		if (cmd.hasOption("a")) {
			Settings.setUsername("anonymous");
			Settings.setSecret("");
			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendAnonymousLoginMsg();
		}

		if(cmd.hasOption('l')){
			ClientSkeleton c = ClientSkeleton.getInstance();
			c.sendLoginMsg();
		}


	}


}
