package com.ibm.ServerWizard2;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
public class ServerWizard2 {

	/**
	 * @param args
	 */
	public final static Logger LOGGER = Logger.getLogger(ServerWizard2.class.getName());
	public static void printUsage() {
		System.out.println("Usage:");
		System.out.println("   -i [xml filename]");
		System.out.println("   -v [update to version]");
		System.out.println("   -h = print this usage");
	}
	public static void main(String[] args) {
		String inputFilename="";
		String version="";
		for (int i=0;i<args.length;i++) {
			if (args[i].equals("-i")) {
				if (i==args.length-1) {
					printUsage();
					System.exit(3);
				}
				inputFilename=args[i+1];
			}
			if (args[i].equals("-v")) {
				if (i==args.length-1) {
					printUsage();
					System.exit(3);
				}
				version=args[i+1];
			}
			if (args[i].equals("-h")) {
				printUsage();
				System.exit(0);
			}
		}
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler logConsole = new ConsoleHandler();
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);
		/*
		try {
			FileHandler logFile = new FileHandler("serverwiz2.%u.%g.log",20000,2,true);
			LOGGER.addHandler(logFile);
			logFile.setFormatter(formatter);
			logFile.setLevel(Level.CONFIG);
		} catch (IOException e) {
			System.err.println("Unable to create logfile");
			System.exit(3);
		}
		*/
		LOGGER.config("======================================================================");
		LOGGER.config("ServerWiz2 Starting...");
		TargetWizardController tc = new TargetWizardController();
		SystemModel systemModel = new SystemModel();
	    MainDialog view = new MainDialog(null);
	    tc.setView(view);
	    tc.setModel(systemModel);
	    systemModel.addPropertyChangeListener(tc);
	    view.setController(tc);
		if (!inputFilename.isEmpty()) {
			view.mrwFilename=inputFilename;
		}
	    view.open();
	    //d.dispose();
	}
}
