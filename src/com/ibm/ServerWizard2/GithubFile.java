package com.ibm.ServerWizard2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.ProgressMonitorInputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.json.simple.parser.JSONParser;

public class GithubFile {
	private final String API_URL="https://api.github.com/repos/";
	private static String UPDATE_FILE="serverwiz2.update";
	private final boolean DEBUG=false;

	private File localFile;
	private String filename;
	private FileTypes type;
	private String localDirectory;
	private boolean inRelease=false;

	private String downloadUrl="";
	private String downloadUrlTag="download_url";
	private String apiUrl="";
	private String version="";
	private String repository="";
	private long remoteFileSize=0;
	private long localFileSize=0;
	private Logger logger;
	private boolean downloadNeeded=false;
	private static long MILLISECONDS_PER_DAY = 86400*1000;

	public enum FileTypes {
		ATTRIBUTE_TYPE_XML, TARGET_TYPE_XML, TARGET_INSTANCES_XML, SCRIPT, JAR
	}

	public GithubFile(String repository, String version, String filename, FileTypes type,Logger logger) {
		this.repository=repository;
		this.version=version;
		this.filename=filename;
		this.type=type;
		this.logger=logger;
		this.init();
	}
	public String getVersion() {
		return version;
	}
	public static String getWorkingDir() {
		// gets working directory whether running as jar or from eclipse
		File f = new File("").getAbsoluteFile();
		String workingDir = f.getAbsolutePath() + System.getProperty("file.separator");
		return workingDir;
	}
	public boolean localFileExists() {
		return this.localFileSize > 0;
	}
	public void init() {
		String workingDir=GithubFile.getWorkingDir();
		String subDir="";
		String urlSubDir = "";
		if (type==FileTypes.SCRIPT) {
			subDir="scripts"+System.getProperty("file.separator");
			urlSubDir="scripts/";
		} else if (type==FileTypes.JAR) {
			subDir="jars"+System.getProperty("file.separator");
			this.inRelease=true;
		} else {
			subDir = "xml"+System.getProperty("file.separator");
			urlSubDir="xml/";
		}
		if (this.inRelease) {
			if (version.equals("latest")) {
				apiUrl=API_URL+repository+"releases/latest";
			} else {
				apiUrl=API_URL+repository+"releases/tags/"+version;
			}
			downloadUrlTag="browser_download_url";
		} else {
			apiUrl=API_URL+repository+"contents/"+urlSubDir+"?ref="+version;
		}
		localDirectory=workingDir + subDir;
		this.localFile = new File(workingDir+subDir+filename);
		if (this.localFile.exists()) {
			localFileSize = this.localFile.length();
		}
	}
	public void mkdir() throws Exception {
		//create local directory
		File sdir = new File(localDirectory);
		if (!sdir.exists()) {
			try {
				sdir.mkdir();
			} catch (SecurityException se) {
				throw new Exception("Unable to create dir: "+localDirectory);
			}
		}
	}
	public boolean update() throws Exception {
		downloadNeeded=false;
		logger.info("Updating: "+this.getLocalPath());
		logger.info("Querying: "+apiUrl);
		try {
			URL url = new URL(apiUrl);
			InputStream is = url.openStream();
			BufferedReader bis = new BufferedReader(new InputStreamReader(is));
			JSONParser parser=new JSONParser();
			List fileList;
			if (this.inRelease) {
				Map fileMap = (Map)parser.parse(bis);
				version = (String)fileMap.get("tag_name");
				fileList = (List)fileMap.get("assets");
			} else {
				fileList = (List)parser.parse(bis);
			}
			boolean found=false;
			for (Object mapObj : fileList) {
				Map files = (Map) mapObj;
				String fileName = (String)files.get("name");
				if (fileName.equals(this.filename)) {
					downloadUrl = (String)files.get(this.downloadUrlTag);
					remoteFileSize = (Long)files.get("size");
					logger.info("Remote File: "+downloadUrl);
					found=true;
				}
			}
			if (!found) {
				throw new Exception("Unable to find "+this.filename+" at "+apiUrl);
			}
			is.close();
		} catch (Exception e) {
			//can't download file, check if local file exists
			logger.warning(e.toString());
			if (this.localFileExists()) {
				logger.warning("Unable to download");
				removeUpdateFile(false);
				return false;
			} else {
				throw new Exception("Unable to connect to "+this.apiUrl+ " and "+this.filename+" does not exist locally.");
			}
		}
		//if (localFileSize==remoteFileSize && localFileSize>0) {
		//	logger.info("No Update Required; Files are same.");
		//	return false;
		//}
		downloadNeeded=true;
		return true;
	}

	public String getLocalDirectory() {
		return this.localDirectory;
	}
	public String getLocalPath() {
		return localFile.getPath();
	}
	public String getFilename() {
		return localFile.getName();
	}
	public FileTypes getType() {
		return type;
	}
	public void download() throws Exception {
		if (!downloadNeeded) { return; }
		logger.info("Downloading: "+this.downloadUrl);
		if (!DEBUG) {
			this.mkdir();
		    URL url = new URL(this.downloadUrl);
		    ProgressMonitorInputStream pim = new ProgressMonitorInputStream(null, "Downloading "+ downloadUrl,
					url.openStream());

		    pim.getProgressMonitor().setMaximum((int) this.remoteFileSize);
		    pim.getProgressMonitor().setMillisToDecideToPopup(500);

		    Files.copy(pim, localFile.toPath(),
		            StandardCopyOption.REPLACE_EXISTING);
		}
	}
	public static String isTimeForUpdateCheck(Logger logger) {
		String workingDir = GithubFile.getWorkingDir();
		String updateFilename = workingDir + UPDATE_FILE;
		File updateFile = new File(updateFilename);
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		String version="NONE";
		if (updateFile.exists()) {
			// check last update
			// update check done once per week
			Long updateTime = (long) 0;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(updateFile));
				String t = reader.readLine();
				String c[] = t.split(",");
				updateTime = Long.parseLong(c[0]);
				if (c.length>1) {
					version=c[1];
				}
			} catch (Exception e) {
				logger.severe("Error reading " + updateFilename);
				logger.severe(e.getMessage());
			} finally {
				try {
					// Close the writer regardless of what happens...
					reader.close();
				} catch (Exception e) {
				}
			}
			//logger.info("Current Time: " + String.valueOf(currentTime));
			//logger.info("Update Time: " + String.valueOf(updateTime));

			if ((currentTime - updateTime) < MILLISECONDS_PER_DAY*3) {
				logger.info("Not checking for updates");
				version="";
			}
		}
		return version;
	}
	public static void updateSuccess(Logger logger,String version) {
		logger.info("Writing update file; Version = "+version);
		String workingDir = GithubFile.getWorkingDir();
		String updateFilename = workingDir + UPDATE_FILE;
		File updateFile = new File(updateFilename);
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(updateFile));
			writer.write(String.valueOf(currentTime)+","+version);
		} catch (Exception e) {
			logger.severe("Error writing " + updateFilename);
			logger.severe(e.getMessage());
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}
	public static void removeUpdateFile(boolean showDialog) {
		String workingDir = GithubFile.getWorkingDir();
		String updateFilename = workingDir + UPDATE_FILE;
		File updateFile = new File(updateFilename);
		if (updateFile.exists()) {
			if (updateFile.delete()) {
				if (showDialog) {
					MessageDialog.openInformation(null, "Force Update",
							"Update will occur upon restart...");
					ServerWizard2.LOGGER.info("Removing update file to force update upon restart.");
				}
			} else {
				MessageDialog.openError(null, "Error",
						"Unable to delete serverwiz2.update.  Try deleting manually.");
				ServerWizard2.LOGGER.severe("Unable to delete serverwiz2.update.");
			}
		}
	}
}
