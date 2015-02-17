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

	public static final String VERSION = "2.1.0";
	public final static Logger LOGGER = Logger.getLogger(ServerWizard2.class.getName());

	public static void main(String[] args) {
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler logConsole = new ConsoleHandler();
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);

		try {
			FileHandler logFile = new FileHandler("serverwiz2.%u.%g.log",20000,2,true);
			LOGGER.addHandler(logFile);
			logFile.setFormatter(formatter);
			logFile.setLevel(Level.CONFIG);
		} catch (IOException e) {
			System.err.println("Unable to create logfile");
			System.exit(3);
		}
		LOGGER.config("======================================================================");
		LOGGER.config("ServerWiz2 Starting.  VERSION: "+VERSION);
		TargetWizardController tc = new TargetWizardController();
		SystemModel systemModel = new SystemModel();
	    MainDialog view = new MainDialog(null);
	    tc.setView(view);
	    tc.setModel(systemModel);
	    systemModel.addPropertyChangeListener(tc);
	    view.setController(tc);
		if (args.length>0) {
			view.mrwFilename=args[0];
		}
	    view.open();
	    //d.dispose();
	}
}
