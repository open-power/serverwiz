package com.ibm.ServerWizard2;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.ServerWizard2.controller.TargetWizardController;
import com.ibm.ServerWizard2.model.SystemModel;
import com.ibm.ServerWizard2.utility.MyLogFormatter;
import com.ibm.ServerWizard2.view.MainDialog;
public class ServerWizard2 {

	/**
	 * @param args
	 */
	public final static Logger LOGGER = Logger.getLogger(ServerWizard2.class.getName());
	public final static int VERSION_MAJOR = 2;
	public final static int VERSION_MINOR = 1;

	public static String getVersionString() {
		return VERSION_MAJOR+"."+VERSION_MINOR;
	}
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("   -i [xml filename]");
		System.out.println("   -v [update to version]");
		System.out.println("   -f = run xml clean up");
		System.out.println("   -h = print this usage");
	}
	public static void main(String[] args) {
		String inputFilename="";
		Boolean cleanupMode = false;
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
			}
			if (args[i].equals("-h")) {
				printUsage();
				System.exit(0);
			}
			if (args[i].equals("-f")) {
				cleanupMode = true;
			}
		}
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler logConsole = new ConsoleHandler();
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);

		LOGGER.config("======================================================================");
		LOGGER.config("ServerWiz2 Version "+getVersionString()+" Starting...");
		TargetWizardController tc = new TargetWizardController();
		SystemModel systemModel = new SystemModel();
	    MainDialog view = new MainDialog(null);
	    tc.setView(view);
	    tc.setModel(systemModel);
	    view.setController(tc);
	    systemModel.cleanupMode = cleanupMode;
		if (!inputFilename.isEmpty()) {
			view.mrwFilename=inputFilename;
		}
	    view.open();
	}
}
