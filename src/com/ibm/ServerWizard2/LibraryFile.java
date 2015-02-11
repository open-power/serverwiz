package com.ibm.ServerWizard2;

import java.io.File;
import java.net.URL;

public class LibraryFile {
	
	private URL remoteFile;
	private File localFile;
	private String filename;
	private FileTypes type;
	
	public enum FileTypes {
		ATTRIBUTE_TYPE_XML, TARGET_TYPE_XML, TARGET_INSTANCES_XML, SCRIPT
	}
	
	public void init(String url, String workingDir, FileTypes type) throws Exception {
		remoteFile = new URL(url);
		File fi = new File(getRemotePath());
		filename = fi.getName();
		this.type = type;
		String subdir = "xml";
		if (type==FileTypes.SCRIPT) { 
			subdir="scripts";
		}
		localFile=new File(workingDir+subdir+System.getProperty("file.separator")+filename);
	}
	
	public String getRemotePath() {
		return remoteFile.toString();
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
}
