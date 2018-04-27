package activitystreamer;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import activitystreamer.server.Control;
import activitystreamer.util.Settings;

public class Server {
	private static final Logger log = LogManager.getLogger();
	
	private static void help(Options options){
		String header = "An ActivityStream Server for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Server", header, options, footer, true);
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		
		log.info("reading command line options");
		
		Options options = new Options();
		//TODO option for command type

		options.addOption("lh",true,"local hostname");
		options.addOption("lp",true,"local port number");

		options.addOption("rp",true,"remote port number");
		options.addOption("rh",true,"remote hostname");

		options.addOption("a",true,"activity interval in milliseconds");
		options.addOption("s",true,"secret for the server to use");

		options.addOption("ui",false,"Provide this argv to show UI");
		
		
		// build the parser
		CommandLineParser parser = new DefaultParser();
		
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			help(options);
		}

		// TODO what if arguments are not suitable, for example no remote infomation provided when register?
		
		if(cmd.hasOption("lp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("lp"));
				Settings.setLocalPort(port);
			} catch (NumberFormatException e){
				log.info("-lp requires a port number, parsed: "+cmd.getOptionValue("lp"));
				help(options);
			}
		}
		
		if(cmd.hasOption("rh")){
			Settings.setRemoteHostname(cmd.getOptionValue("rh"));
		}
		else if(!cmd.hasOption("s")){
			Settings.setSecret(Settings.nextSecret());
			log.info("Secret for the system: "+Settings.getSecret());
		}
		
		if(cmd.hasOption("rp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("rp"));
				Settings.setRemotePort(port);
			} catch (NumberFormatException e){
				log.error("-rp requires a port number, parsed: "+cmd.getOptionValue("rp"));
				help(options);
			}
		}
		
		if(cmd.hasOption("a")){
			try{
				int a = Integer.parseInt(cmd.getOptionValue("a"));
				Settings.setActivityInterval(a);
			} catch (NumberFormatException e){
				log.error("-a requires a number in milliseconds, parsed: "+cmd.getOptionValue("a"));
				help(options);
			}
		}
		
		try {
			Settings.setLocalHostname(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			log.warn("failed to get localhost IP address");
		}
		
		if(cmd.hasOption("lh")){
			Settings.setLocalHostname(cmd.getOptionValue("lh"));
		}

		setSecret(cmd);
		
		log.info("starting server");

		Settings.setServerId(Settings.nextSecret());
		
		final Control c = Control.getInstance();


		// the following shutdown hook doesn't really work, it doesn't give us enough time to
		// cleanup all of our connections before the jvm is terminated.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {  
				c.setTerm(true);
				c.interrupt();
		    }
		 });
	}

	private static void setSecret(CommandLine cmd){
		if(cmd.hasOption("s") && !cmd.hasOption("rh") && !cmd.hasOption("rp")){
			Settings.setSecret(cmd.getOptionValue("s"));
			log.info("Secret for the system: "+Settings.getSecret());
			return;
		}

		if(!cmd.hasOption("s") && !cmd.hasOption("rh") && !cmd.hasOption("rp")){
			log.info("secret for the first server is not provided, generate one automatically");
			Settings.setSecret(Settings.nextSecret());
			log.info("Generated secret: [{}]: ",Settings.getSecret());
			return;
		}

		if(!cmd.hasOption("s") && cmd.hasOption("rh") && cmd.hasOption("rp")){
			log.error("Secret must be provided to join to an existing server");
			System.exit(-1);
		}else{
			String secret = cmd.getOptionValue("s");
			log.info("Set secret to [{}]",secret);
			Settings.setSecret(secret);
		}
	}

}
