package com.ibm.ServerWizard2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Launcher {

	public static String RELEASE_URL = "https://github.com/open-power/serverwiz/releases/download/";
	public static String JAR_NAME = "serverwiz2";
	public final static Logger LOGGER = Logger.getLogger(Launcher.class.getName());

	public Launcher() {

	}
	public static void main(String[] args) {
		String version = "latest";
		boolean forceUpdate=false;
		boolean forceLocal=false;
		for (int i=0;i<args.length;i++) {
			if (args[i].equals("-v")) {
				if (i==args.length-1) {
					System.out.println("Must provide a version if -v option is used");
					System.exit(3);
				}
				version=args[i+1];
				System.out.println("FORCE UPDATE:"+version);
				forceUpdate=true;
			}
			if (args[i].equals("-d")) {
				forceLocal=true;
			}
		}
		//Setup logger
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		ConsoleHandler logConsole = new ConsoleHandler();
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);

		try {
			FileHandler logFile = new FileHandler("serverwiz2_launcher.%u.%g.log",20000,2,true);
			LOGGER.addHandler(logFile);
			logFile.setFormatter(formatter);
			logFile.setLevel(Level.CONFIG);
		} catch (IOException e) {
			System.err.println("Unable to create logfile");
			System.exit(3);
		}
		LOGGER.config("======================================================================");
		LOGGER.config("Retreiving ServerWiz...");

		String jarName = getArchFilename("serverwiz2");
		LOGGER.info("JarName = "+jarName);
		GithubFile jar =    new GithubFile("open-power/serverwiz/",version,jarName,GithubFile.FileTypes.JAR,LOGGER);

		String versionCurrent="NONE";

		if (!jar.localFileExists()) {
			if (forceLocal) {
				JOptionPane.showMessageDialog(null, "Download is disabled and\n"+jar.getLocalPath()+" doesn't exist",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
			forceUpdate=true;
		}
		if (!forceUpdate && !forceLocal) {
			//returns empty string if no update required, otherwise returns current version
			versionCurrent=GithubFile.isTimeForUpdateCheck(LOGGER);
		}
		if (forceLocal) {
			LOGGER.info("Downloading is disabled");
			versionCurrent="";
		}
		boolean updated=false;
		if (!versionCurrent.isEmpty()) {
			LOGGER.info("Current jar version: "+versionCurrent);
			try {
				if (jar.update()) {
					version=jar.getVersion();
					LOGGER.info("Latest version: "+version);
					if (!version.equals(versionCurrent)) {
						boolean doUpdate=true;
						if (!versionCurrent.equals("NONE")) {
							int selection=JOptionPane.showConfirmDialog(null,
									 "There is a newer verison of ServerWiz2 ("+version+").  Would like like to download?",
									 "Confirm Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (selection==JOptionPane.NO_OPTION) {
								doUpdate=false;
								GithubFile.updateSuccess(LOGGER, versionCurrent);
							}
						}
						if (doUpdate) {
							jar.download();
							updated=true;
							GithubFile.updateSuccess(LOGGER, version);
						}
					} else {
						//versions are same
						GithubFile.updateSuccess(LOGGER, versionCurrent);
					}
				}
			} catch(Exception e) {
				GithubFile.removeUpdateFile(false);
				LOGGER.severe(e.getMessage());
				JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(3);
			}
		}
		Vector<String> commandLine = new Vector<String>();
		commandLine.add("java");
		commandLine.add("-jar");
		commandLine.add(jar.getLocalPath());

		for (String arg : args) {
			commandLine.add(arg);
		}
		if (updated) {
			commandLine.add("-v");
			commandLine.add(version);
		}
		run(commandLine);
		LOGGER.info("Exiting...");
	}
	public static void run(Vector<String> commandLine) {
		String commandLineStr="";
		for (String c : commandLine) {
			commandLineStr=commandLineStr+c+" ";
		}
		LOGGER.info("Running: "+commandLineStr);

		try {
			final ProcessBuilder builder = new ProcessBuilder(commandLine).redirectErrorStream(true);
			final Process process = builder.start();

			new Thread(new Runnable() {
			public void run() {
				InputStreamReader in = new InputStreamReader(process.getInputStream());
				BufferedReader inb = new BufferedReader(in);
				String thisLine;
				try {
			        while ((thisLine = inb.readLine()) != null) {
			            LOGGER.info(thisLine);
			         }
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			}).start();

			//final int exitValue = process.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getWorkingDir() {
		// gets working directory whether running as jar or from eclipse
		File f = new File("").getAbsoluteFile();
		String workingDir = f.getAbsolutePath() + System.getProperty("file.separator");
		return workingDir;
	}
	public static String getArchFilename(String prefix) {
	   return prefix + "_" + getOSName() + getArchName() + ".jar";
	}

	private static String getOSName() {
	   String osNameProperty = System.getProperty("os.name");
	   if (osNameProperty == null) {
	       throw new RuntimeException("os.name property is not set");
	   } else {
	       osNameProperty = osNameProperty.toLowerCase();
	   }
	   if (osNameProperty.contains("win")) {
	       return "win";
	   } else if (osNameProperty.contains("linux") || osNameProperty.contains("nix"))
	   {
	       return "linux";
	   } else {
	       throw new RuntimeException("Unknown OS name: " + osNameProperty);
	   }
	}

	private static String getArchName() {
	   String osArch = System.getProperty("os.arch");
	   if (osArch != null && osArch.contains("64")) {
	       return "64";
	   } else {
	       return "32";
	   }
	}
	}
