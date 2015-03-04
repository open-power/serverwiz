package com.ibm.ServerWizard2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

public class Launcher {
	public final static String JAR_NAME = "serverwiz2";
	public final static String ZIP_NAME = "serverwiz2_lib.zip";
	public final static String REPOSITORY= "open-power/serverwiz/";
	public final static Logger LOGGER = Logger.getLogger(Launcher.class.getName());

	public Launcher() {

	}
	public static void main(String[] args) {
		MessagePopup dm = new MessagePopup("ServerWiz2 Launcher", 5);
		dm.open();
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
				forceUpdate=true;
			}
			if (args[i].equals("-d")) {
				forceLocal=true;
			}
		}
		//Setup logger
		LOGGER.setLevel(Level.CONFIG);
		LOGGER.setUseParentHandlers(false);
		DialogHandler logConsole = new DialogHandler(dm.text);
		logConsole.setLevel(Level.CONFIG);
		LOGGER.addHandler(logConsole);
		MyLogFormatter formatter = new MyLogFormatter();
		logConsole.setFormatter(formatter);

		try {
			//FileHandler logFile = new FileHandler("serverwiz2_launcher.%u.%g.log",20000,2,true);
			FileHandler logFile = new FileHandler("serverwiz2.%u.%g.log",200000,2,true);
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
		GithubFile jar = new GithubFile(REPOSITORY,version,jarName,"jars",true,LOGGER);
		GithubFile zip = new GithubFile(REPOSITORY,version,ZIP_NAME,"jars",true,LOGGER);

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
									 "There is a newer version of ServerWiz2 ("+version+").  Would like like to download?",
									 "Confirm Update", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (selection==JOptionPane.NO_OPTION) {
								doUpdate=false;
								GithubFile.updateSuccess(LOGGER, versionCurrent);
							}
						}
						if (doUpdate) {
							jar.download();
							zip.update();
							zip.download();
							unzip(zip.getLocalPath(),getWorkingDir());
							updated=true;
							GithubFile.updateSuccess(LOGGER, version);
						}
					} else {
						//versions are same
						GithubFile.updateSuccess(LOGGER, versionCurrent);
					}
				}
			} catch(Exception e) {
				//unknown state so force update next time.
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
		try {
		Thread.sleep(1500);
		} catch (Exception e) {

		}
		Thread t=run(commandLine);
		try {
			t.join();
		} catch (Exception e) {

		}
		dm.close();
	}
	public static Thread run(Vector<String> commandLine) {
		String commandLineStr="";
		for (String c : commandLine) {
			commandLineStr=commandLineStr+c+" ";
		}
		LOGGER.info("Running: "+commandLineStr);
		try {
			final ProcessBuilder builder = new ProcessBuilder(commandLine).redirectErrorStream(true);
			final Process process = builder.start();
			Thread thread = new Thread(new Runnable() {
			public void run() {
				InputStreamReader in = new InputStreamReader(process.getInputStream());
				BufferedReader inb = new BufferedReader(in);
				String thisLine;
				try {
			        while ((thisLine = inb.readLine()) != null) {
			            LOGGER.info(thisLine);
			         }
				} catch (IOException e) {
					LOGGER.info(e.toString());
					e.printStackTrace();
				}
			}
			});
			thread.start();
			return thread;
			//final int exitValue = process.waitFor();
		} catch (Exception e) {
			LOGGER.info(e.toString());
			e.printStackTrace();
		}
		return null;
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
	public static void unzip(String file, String outputDir) throws Exception {
		ZipFile zipFile = new ZipFile(file);
	    Enumeration<? extends ZipEntry> entries = zipFile.entries();
	    while (entries.hasMoreElements()) {
	        ZipEntry entry = entries.nextElement();
	        File entryDestination = new File(outputDir,  entry.getName());
	        entryDestination.getParentFile().mkdirs();
	        if (entry.isDirectory()) {
	            entryDestination.mkdirs();
	    	} else {
	    		LOGGER.info("Unzipping: "+entryDestination.getPath());
	            Files.copy(zipFile.getInputStream(entry), entryDestination.toPath(),StandardCopyOption.REPLACE_EXISTING);
	        }
	    }
	    zipFile.close();
	}
}
