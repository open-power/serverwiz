package com.ibm.ServerWizard2;

import org.eclipse.jface.dialogs.MessageDialog;

public class LibraryManager {
	private String processScript = "";
	private String processDirectory = "";
	public static String REPO = "open-power/serverwiz/";
	GithubFile files[] = new GithubFile[7];

	public void loadModel(SystemModel model) throws Exception {
		for (GithubFile libFile : files) {
			if (libFile.getType() == GithubFile.FileTypes.ATTRIBUTE_TYPE_XML) {
				model.loadAttributes(new XmlHandler(), libFile.getLocalPath());
			}
			if (libFile.getType() == GithubFile.FileTypes.TARGET_TYPE_XML) {
				model.loadTargetTypes(new XmlHandler(), libFile.getLocalPath());
			}
			if (libFile.getType() == GithubFile.FileTypes.TARGET_INSTANCES_XML) {
				model.loadTargetInstances(libFile.getLocalPath());
			}
		}
	}

	public void init(String version) {
		files[0] = new GithubFile(REPO,version,
				                  "attribute_types.xml",GithubFile.FileTypes.ATTRIBUTE_TYPE_XML,ServerWizard2.LOGGER);

		files[1] = new GithubFile(REPO,version,
                                  "attribute_types_hb.xml",GithubFile.FileTypes.ATTRIBUTE_TYPE_XML,ServerWizard2.LOGGER);

		files[2] = new GithubFile(REPO,version,
                                  "attribute_types_mrw.xml",GithubFile.FileTypes.ATTRIBUTE_TYPE_XML,ServerWizard2.LOGGER);

		files[3] = new GithubFile(REPO,version,
                                  "target_types_mrw.xml",GithubFile.FileTypes.TARGET_TYPE_XML,ServerWizard2.LOGGER);

		files[4] = new GithubFile(REPO,version,
                                  "target_instances_v3.xml",GithubFile.FileTypes.TARGET_INSTANCES_XML,ServerWizard2.LOGGER);

		files[5] = new GithubFile(REPO,version,
                                  "processMrw.pl",GithubFile.FileTypes.SCRIPT,ServerWizard2.LOGGER);

		files[6] = new GithubFile(REPO,version,
                                  "Targets.pm",GithubFile.FileTypes.SCRIPT,ServerWizard2.LOGGER);

		processScript = files[5].getLocalPath();
		processDirectory = files[5].getLocalDirectory();
	}
	public String getProcessingScript() {
		return this.processScript;
	}
	public String getProcessingDirectory() {
		return this.processDirectory;
	}


	/*
	 * check if files exist yes- files exist, check if github is newer if yes,
	 * then ask user if wishes to update if no or can't download, then continue
	 * no- files don't exist; download if can't download, then exit
	 */
	public void update(String version) throws Exception {
		if (!version.isEmpty()) {
			ServerWizard2.LOGGER.info("Updating XML lib to version: "+version+"\n");
			for (GithubFile libFile : files) {
				libFile.update();
				libFile.download();
			}
		}
	}
}
