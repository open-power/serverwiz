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

public class LibraryFile {
	private File localFile;
	private String filepath;
	private FileTypes type;
	private long localFileSize=0;

	public enum FileTypes {
		ATTRIBUTE_TYPE_XML, TARGET_TYPE_XML, TARGET_INSTANCES_XML, SCRIPT
	}

	public LibraryFile(String filepath,FileTypes type) {
		this.filepath=filepath;
		this.type=type;
		this.init();
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
		String workingDir=LibraryFile.getWorkingDir();
		this.localFile = new File(workingDir+filepath);
		if (this.localFile.exists()) {
			localFileSize = this.localFile.length();
		}
	}
	public String getPath() {
		return localFile.getPath();
	}
	public String getFilename() {
		return localFile.getName();
	}
	public FileTypes getType() {
		return type;
	}
}
