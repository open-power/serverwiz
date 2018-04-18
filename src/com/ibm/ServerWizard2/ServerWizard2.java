package com.ibm.ServerWizard2;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

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
	public final static int VERSION_MINOR = 10;

	public final static String PROPERTIES_FILE = "serverwiz.preferences";
	public static String GIT_LOCATION = "";
	public final static String DEFAULT_REMOTE_URL = "https://github.com/open-power/common-mrw-xml.git";

	public static Boolean updateOnlyMode = false;

	public static String getVersionString() {
		return VERSION_MAJOR+"."+VERSION_MINOR;
	}
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("   -i [xml filename]");
		System.out.println("   -o [xml filename]");
		System.out.println("   -v [update to version]");
		System.out.println("   -f = run xml clean up");
		System.out.println("   -u = update the XML with attributes and targets."
				+ " When using this option it is mandatory to pass input XML "
				+ "file name and ouput XML filename. Serverwiz looks for "
				+ "common-mrw-xml directory in the current directory. If it is "
				+ "not found, then clones it from github.");
		System.out.println("   -h = print this usage");
	}
	public static void main(String[] args) {
		String inputFilename="";
		String outputFilename="";
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
			if (args[i].equals("-o")) {
				if (i==args.length-1) {
					printUsage();
					System.exit(3);
				}
				outputFilename=args[i+1];
			}
			if (args[i].equals("-u")) {
				updateOnlyMode = true;
			}
		}

		if(updateOnlyMode)
		{
			//In "update only" mode, we only open the input File, update the
			//attributes and save the output file. So make sure we have input
			//and output files names.
			if((inputFilename.length() == 0) || (outputFilename.length() == 0))
			{
				printUsage();
				System.exit(3);
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

		if(updateOnlyMode) {
			LOGGER.config("Running in update only mode. GUI would not be loaded.");
			getPreferencesForUpdateMode();
		} else {
			getPreferences();
		}
	
		TargetWizardController tc = new TargetWizardController();
		SystemModel systemModel = new SystemModel();
		MainDialog view = new MainDialog(null);
		tc.setView(view);
		tc.setModel(systemModel);
		view.setController(tc);
		tc.init();
		systemModel.cleanupMode = cleanupMode;
		if (!inputFilename.isEmpty()) {
			view.mrwFilename=inputFilename;
		}
		if(updateOnlyMode) {
			try {
				tc.readXML(inputFilename);
			} catch(Exception e) {
				LOGGER.severe(e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
			tc.writeXML(outputFilename);
		} else {
			view.open();
		}
	}

	// Set the GIT location to current working directory
	private static void getPreferencesForUpdateMode() {
		try {
		ServerWizard2.GIT_LOCATION = new java.io.File( "." ).getCanonicalPath();
		} catch (Exception e) {
			LOGGER.severe("Unable to obtain current working directory");
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	// Load preferences file.
	// Contains git repository location and repositories to manage.
	private static void getPreferences() {
		Display display = new Display();
		Shell shell = new Shell(display);
		try {
			Properties p = new Properties();
			File f = new File(ServerWizard2.PROPERTIES_FILE);
			if (!f.exists()) {
				//File doesn't exist, so create; prompt user for git location
				ServerWizard2.LOGGER.info("Preferences file doesn't exist, creating...");
				DirectoryDialog fdlg = new DirectoryDialog(shell, SWT.OPEN);
				fdlg.setMessage("Select location of GIT repositories:");
				fdlg.setFilterPath(ServerWizard2.getWorkingDir());
				String libPath = fdlg.open();
				if (libPath == null || libPath.isEmpty()) {
					ServerWizard2.LOGGER.warning("No directory selected; exiting...");
					System.exit(0);
				}
				p.setProperty("git_location", libPath);
				p.setProperty("repositories", ServerWizard2.DEFAULT_REMOTE_URL);
				p.setProperty("needs_password", "false");

				FileOutputStream out = new FileOutputStream(ServerWizard2.PROPERTIES_FILE);
				p.store(out, "");
				out.close();
			}
			FileInputStream propFile = new FileInputStream(ServerWizard2.PROPERTIES_FILE);
			p.load(propFile);
			propFile.close();
			String loc = p.getProperty("git_location");
			if (loc !=null && !loc.isEmpty()) {
				ServerWizard2.GIT_LOCATION = loc;
			} else {
				ServerWizard2.LOGGER.severe(ServerWizard2.PROPERTIES_FILE+" does not contain a repository location.\nPlease correct or delete.");
				System.exit(0);
			}


		} catch (Exception e) {
			e.printStackTrace();
			ServerWizard2.LOGGER.severe(e.getMessage());
			System.exit(0);
		}
	}
	public static String getWorkingDir() {
		File f = new File("").getAbsoluteFile();
		String workingDir = f.getAbsolutePath() + System.getProperty("file.separator");
		return workingDir;
	}
}
