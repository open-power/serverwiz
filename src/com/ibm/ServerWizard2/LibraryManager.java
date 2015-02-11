package com.ibm.ServerWizard2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.ProgressMonitorInputStream;

import org.eclipse.jface.dialogs.MessageDialog;

public class LibraryManager {
	public String xmlDirectory = "";
	public String scriptDirectory = "";
	private Vector<LibraryFile> files = new Vector<LibraryFile>();

	public enum UpdateStatus {
		FILE_NOT_LOCAL, REMOTE_FILE_NEWER, NO_UPDATE_NEEDED, UNABLE_TO_DOWNLOAD, UPDATE_REQUIRED, UPDATE_RECOMMENDED
	}

	public static String getWorkingDir() {
		// gets working directory whether running as jar or from eclipse
		File f = new File("").getAbsoluteFile();
		String workingDir = f.getAbsolutePath() + System.getProperty("file.separator");
		return workingDir;
	}

	public void loadModel(SystemModel model) throws Exception {
		for (LibraryFile libFile : files) {
			if (libFile.getType() == LibraryFile.FileTypes.ATTRIBUTE_TYPE_XML) {
				model.loadAttributes(new XmlHandler(), libFile.getLocalPath());
			}
			if (libFile.getType() == LibraryFile.FileTypes.TARGET_TYPE_XML) {
				model.loadTargetTypes(new XmlHandler(), libFile.getLocalPath());
			}
			if (libFile.getType() == LibraryFile.FileTypes.TARGET_INSTANCES_XML) {
				model.loadTargetInstances(libFile.getLocalPath());
			}
		}
	}

	public void init() {
		String workingDir = LibraryManager.getWorkingDir();
		try {
			//String hbUrl = "https://raw.githubusercontent.com/open-power/hostboot/master/src/usr/targeting/common/xmltohb/";
			String swUrl = "https://raw.githubusercontent.com/open-power/serverwiz/master/";

			LibraryFile f1 = new LibraryFile();
			f1.init(swUrl + "xml/attribute_types.xml", workingDir,
					LibraryFile.FileTypes.ATTRIBUTE_TYPE_XML);
			files.add(f1);

			LibraryFile f2 = new LibraryFile();
			f2.init(swUrl + "xml/attribute_types_hb.xml", workingDir,
					LibraryFile.FileTypes.ATTRIBUTE_TYPE_XML);
			files.add(f2);

			LibraryFile f3 = new LibraryFile();
			f3.init(swUrl + "xml/attribute_types_mrw.xml", workingDir,
					LibraryFile.FileTypes.ATTRIBUTE_TYPE_XML);
			files.add(f3);

			LibraryFile f4 = new LibraryFile();
			f4.init(swUrl + "xml/target_types_mrw.xml", workingDir,
					LibraryFile.FileTypes.TARGET_TYPE_XML);
			files.add(f4);

			LibraryFile f5 = new LibraryFile();
			f5.init(swUrl + "xml/target_instances_v3.xml", workingDir,
					LibraryFile.FileTypes.TARGET_INSTANCES_XML);
			files.add(f5);

			LibraryFile f7 = new LibraryFile();
			f7.init(swUrl + "scripts/processMrw.pl", workingDir, LibraryFile.FileTypes.SCRIPT);
			files.add(f7);

			LibraryFile f8 = new LibraryFile();
			f8.init(swUrl + "scripts/Targets.pm", workingDir, LibraryFile.FileTypes.SCRIPT);
			files.add(f8);

		} catch (Exception e) {
			ServerWizard2.LOGGER.severe(e.getMessage());
			e.printStackTrace();
			System.exit(3);
		}
		// create xml subdir if doesn't exist
		File dir = new File(workingDir + "xml");
		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (SecurityException se) {
				System.err.println("Unable to create directory: " + workingDir + "xml");
			}
		}
		// create scripts subdir if doesn't exist
		File sdir = new File(workingDir + "scripts");
		if (!sdir.exists()) {
			try {
				sdir.mkdir();
			} catch (SecurityException se) {
				System.err.println("Unable to create directory: " + workingDir + "scripts");
			}
		}

	}

	public boolean doUpdateCheck() {
		String workingDir = LibraryManager.getWorkingDir();
		String updateFilename = workingDir + "serverwiz2.update";
		File updateFile = new File(updateFilename);

		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Boolean doUpdate = true;

		if (updateFile.exists()) {
			// check last update
			// update check done once per week
			Long updateTime = (long) 0;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(updateFile));
				String t = reader.readLine();
				updateTime = Long.parseLong(t);
			} catch (Exception e) {
				ServerWizard2.LOGGER.severe("Error reading " + updateFilename + "\n"
						+ e.getMessage());
			} finally {
				try {
					// Close the writer regardless of what happens...
					reader.close();
				} catch (Exception e) {
				}
			}
			ServerWizard2.LOGGER.info("Current Time: " + String.valueOf(currentTime)
					+ "; Update Time: " + String.valueOf(updateTime));

			long msPerDay = 86400*1000;
			if ((currentTime - updateTime) < msPerDay*3) {
				ServerWizard2.LOGGER.info("Update Check Not Needed");
				doUpdate = false;
			}
		}
		if (doUpdate) {
			ServerWizard2.LOGGER.info("Update Check Needed");
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(updateFile));
				writer.write(String.valueOf(currentTime));
			} catch (Exception e) {
				ServerWizard2.LOGGER.severe("Error writing " + updateFilename + "\n"
						+ e.getMessage());
			} finally {
				try {
					// Close the writer regardless of what happens...
					writer.close();
				} catch (Exception e) {
				}
			}
		}
		return doUpdate;
	}

	/*
	 * check if files exist yes- files exist, check if github is newer if yes,
	 * then ask user if wishes to update if no or can't download, then continue
	 * no- files don't exist; download if can't download, then exit
	 */
	public void update() {
		// init();
		Boolean fileNotLocal = false;
		Boolean remoteFileNewer = false;
		Boolean doDownload = false;
		for (LibraryFile libFile : files) {
			UpdateStatus status = getUpdateStatus(libFile);
			if (status == UpdateStatus.FILE_NOT_LOCAL) {
				fileNotLocal = true;
			}
			if (status == UpdateStatus.REMOTE_FILE_NEWER) {
				remoteFileNewer = true;
			}
		}
		if (remoteFileNewer) {
			doDownload = MessageDialog.openConfirm(null, "Confirm update",
					"Library out of date.  Do you wish to download latest library");
		}
		// do download
		if (fileNotLocal || doDownload) {
			int fileNum = 0;
			for (LibraryFile libFile : files) {
				downloadXML(libFile, fileNum);
				fileNum++;
			}
		}
	}

	public UpdateStatus getUpdateStatus(LibraryFile f) {
		ServerWizard2.LOGGER.info("Checking status: " + f.getRemotePath());

		// Check local file size
		File file = new File(f.getLocalPath());
		double localFileSize = 0;
		if (file.exists()) {
			localFileSize = file.length();
		} else {
			return UpdateStatus.FILE_NOT_LOCAL;
		}
		UpdateStatus r = UpdateStatus.NO_UPDATE_NEEDED;
		try {
			URL u;
			u = new URL(f.getRemotePath());
			URLConnection connection = u.openConnection();
			connection.setConnectTimeout(5000);
			connection.connect();
			int remoteFileSize = connection.getContentLength();

			if ((double) remoteFileSize == localFileSize) {
				// no update needed
				ServerWizard2.LOGGER.info("No update needed for " + f.getLocalPath() + ".");
				r = UpdateStatus.NO_UPDATE_NEEDED;
			} else {
				// update needed
				ServerWizard2.LOGGER.info("Update needed for " + f.getLocalPath() + ".");
				r = UpdateStatus.REMOTE_FILE_NEWER;
			}
			connection.getInputStream().close();
		} catch (Exception e) {
			// unable to download
			ServerWizard2.LOGGER.info("Unable to connect to " + f.getFilename()
					+ ".  Using local version.");
			r = UpdateStatus.UNABLE_TO_DOWNLOAD;
		}
		return r;
	}

	public void downloadXML(LibraryFile f, int fileNum) {
		// Check local file size
		File file = new File(f.getLocalPath());
		double localFileSize = 0;
		if (file.exists()) {
			localFileSize = file.length();
		}
		ProgressMonitorInputStream pim = null;
		try {
			URL u;
			u = new URL(f.getRemotePath());
			URLConnection connection = u.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(15000);
			connection.connect();
			int remoteFileSize = connection.getContentLength();
			if ((double) remoteFileSize == localFileSize) {
				ServerWizard2.LOGGER.info(f.getFilename()
						+ " - Not downloading, files are same size");
			} else {
				ServerWizard2.LOGGER.info("Downloading: " + f.getRemotePath() + " ("
						+ remoteFileSize + ")");
				pim = new ProgressMonitorInputStream(null, "Downloading " + (fileNum + 1) + " of "
						+ files.size() + " library files: " + f.getFilename(),
						connection.getInputStream());
				pim.getProgressMonitor().setMaximum(remoteFileSize);
				pim.getProgressMonitor().setMillisToDecideToPopup(500);
				Writer out = new BufferedWriter(new FileWriter(f.getLocalPath()));
				int j;
				while ((j = pim.read()) != -1) {
					out.write((char) j);
				}
				out.close();
			}
			connection.getInputStream().close();
		} catch (Exception e) {
			if (pim != null) {
				try {
					pim.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (localFileSize == 0) {
				ServerWizard2.LOGGER.severe("Unable to download " + f.getFilename()
						+ " and local version doesn't exist.  Exiting...");
				MessageDialog.openError(null, "Unable to Download",
						"Unable to download " + f.getFilename()
								+ " and local version doesn't exist.  Exiting...");
				System.exit(3);
			} else {
				ServerWizard2.LOGGER.info("Unable to download " + f.getFilename()
						+ ".  Using local version.");
			}
		}
	}
}
