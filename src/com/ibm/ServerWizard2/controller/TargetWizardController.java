package com.ibm.ServerWizard2.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.ServerWizard2.ServerWizard2;
import com.ibm.ServerWizard2.model.Connection;
import com.ibm.ServerWizard2.model.Field;
import com.ibm.ServerWizard2.model.SystemModel;
import com.ibm.ServerWizard2.model.Target;
import com.ibm.ServerWizard2.utility.Github;
import com.ibm.ServerWizard2.utility.GithubRepository;
import com.ibm.ServerWizard2.view.LogViewerDialog;
import com.ibm.ServerWizard2.view.MainDialog;

import com.ibm.ServerWizard2.utility.ServerwizMessageDialog;

public class TargetWizardController {
	private SystemModel model;
	private MainDialog view;
	private Boolean modelCreationMode = false;

	private String PROCESSING_SCRIPT = "scripts"+File.separator+"gen_html.pl";
	private final String LIBRARY_NAME = "common-mrw-xml";

	public TargetWizardController() {

	}

	public void setModelCreationMode() {
		this.modelCreationMode = true;
	}
	public Boolean getModelCreationMode() {
		return this.modelCreationMode;
	}

	public TreeMap<String,Boolean> getAttributeFilters() {
		return model.getAttributeFilters();
	}

	public void init() {
		try {
			String libraryLocation = ServerWizard2.GIT_LOCATION + File.separator + this.LIBRARY_NAME;
			File chk = new File(libraryLocation);
			if (!chk.exists()) {
				ServerWizard2.LOGGER.info("XML library does not exist so cloning: "+libraryLocation);
				StatusLogger.getLogger().setLevel(Level.FATAL);
				GithubRepository git = new GithubRepository(ServerWizard2.DEFAULT_REMOTE_URL, ServerWizard2.GIT_LOCATION, false);
				git.cloneRepository();
			}
			model.loadLibrary(libraryLocation);
			this.initModel();

			//Check if there are additional libraries to be cloned
			FileInputStream propFile = new FileInputStream(ServerWizard2.PROPERTIES_FILE);
			Properties p = new Properties();
			p.load(propFile);
			propFile.close();
			String repos = p.getProperty("repositories");
			String repo[] = repos.split(",");
			for (int i = 0; i < repo.length; i++) {
				if(repo[i].equals(ServerWizard2.DEFAULT_REMOTE_URL)) {
					//We do not need to clone default library here. This
					//would already be cloned above.
					continue;
				}

				String curRepo = repo[i].substring(repo[i].lastIndexOf('/') + 1);
				curRepo = ServerWizard2.GIT_LOCATION +  File.separator + curRepo;
				chk = new File(curRepo);
				if(!chk.exists()) {
					ServerWizard2.LOGGER.info("XML library does not exist so cloning: " + curRepo);
					GithubRepository git = new GithubRepository(
							repo[i], ServerWizard2.GIT_LOCATION, false);
					git.cloneRepository();
				}
			}
		} catch (Exception e) {
			ServerWizard2.LOGGER.severe(e.toString());
			ServerwizMessageDialog.openError(null, "Error", e.toString());
			e.printStackTrace();
			System.exit(4);
		}
	}

	public void initModel() throws Exception {
		this.modelCreationMode = false;
		model.deleteAllInstances();
		model.addUnitInstances();
	}

	public void deepCopyAttributes(Target t) {
		t.deepCopyAttributes(model.getUnitTargetModel(), model.getTargetLookup());
	}
	public void setView(MainDialog view) {
		this.view = view;
	}

	public void setModel(SystemModel model) {
		this.model = model;
	}

	public void deleteTarget(Target target) {
		model.deleteTarget(target);
	}
	public Vector<Field> getAttributesAndGlobals(Target targetInstance, String path, String filter) {
		return model.getAttributesAndGlobals(targetInstance, path, !this.modelCreationMode, filter);
	}

	public void addTargetInstance(Target targetModel, Target parentTarget,
			TreeItem parentItem, String nameOverride) {

		Target targetInstance;
		Target instanceCheck = model.getTargetInstance(targetModel.getType());
		if (instanceCheck != null) {
			// target instance found of this model type
			targetInstance = new Target(instanceCheck);
			targetInstance.copyChildren(instanceCheck);
		} else {
			targetInstance = new Target(targetModel);
		}
		targetInstance.setName(nameOverride);
		model.updateTargetPosition(targetInstance, parentTarget, -1, modelCreationMode);
		try {
			model.addTarget(parentTarget, targetInstance, modelCreationMode);
			view.updateInstanceTree(targetInstance, parentItem);
		} catch (Exception e) {
			ServerwizMessageDialog.openError(null, "Add Target Error", e.getMessage());
		}
	}

	public Target copyTargetInstance(Target target, Target parentTarget,
			Boolean incrementPosition) {
		Target newTarget = new Target(target);
		if (incrementPosition) {
			newTarget.setPosition(newTarget.getPosition() + 1);
			newTarget.setSpecialAttributes();
		}
		try {
			model.addTarget(parentTarget, newTarget, false);
			newTarget.copyChildren(target);
		} catch (Exception e) {
			ServerwizMessageDialog.openError(null, "Add Target Error", e.getMessage());
			newTarget = null;
		}
		return newTarget;
	}

	public void deleteConnection(Target target, Target busTarget,
			Connection conn) {
		target.deleteConnection(busTarget, conn);
	}

	public Vector<Target> getRootTargets() {
		return model.rootTargets;
	}

	public void writeXML(String filename) {
		try {
			String tmpFilename = filename + ".tmp";
			model.writeXML(tmpFilename, this.modelCreationMode);
			File from = new File(tmpFilename);
			File to = new File(filename);
			Files.copy(from.toPath(), to.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			Files.delete(from.toPath());
			ServerWizard2.LOGGER.info(filename + " Saved");
		} catch (Exception exc) {
			ServerwizMessageDialog.openError(null, "Error", exc.getMessage());
			exc.printStackTrace();
		}
	}

	public Boolean readXML(String filename) throws Exception {
		Boolean rtn = false;
		try {
			model.readXML(filename);
			this.modelCreationMode = model.partsMode;
			rtn = model.errataUpdated;
		} catch (Exception e) {
			ServerwizMessageDialog.openError(null, "Error", e.getMessage());
			if(ServerWizard2.updateOnlyMode) {
				throw e;
			} else {
				e.printStackTrace();
			}
		}
		return rtn;
	}

	public Vector<Target> getConnectionCapableTargets() {
		return model.getConnectionCapableTargets();
	}

	public void setGlobalSetting(String path, String attribute, String value) {
		model.setGlobalSetting(path, attribute, value);
	}

	public Field getGlobalSetting(String path, String attribute) {
		return model.getGlobalSetting(path, attribute);
	}

	public Boolean isGlobalSettings(String path, String attribute) {
		return model.isGlobalSetting(path, attribute);
	}
	public Vector<Target> getChildTargets(Target target) {

		if (target == null) {
			return model.getTopLevelTargets();
		}
		if (this.modelCreationMode) {
			Boolean override = false;
			if (target.getType().startsWith("targetoverride")) { override = true; }
			return model.getUnitTargets(override);
		}
		return model.getChildTargetTypes(target.getType());
	}

	public void hideBusses(Target target) {
		target.hideBusses(model.getTargetLookup());
	}

	public Vector<Target> getVisibleChildren(Target target, boolean showHidden) {
		Vector<Target> children = new Vector<Target>();
		for (String c : target.getChildren()) {
			Target t = model.getTarget(c);
			if (t == null) {
				String msg = "Invalid Child target id: " + c;
				ServerWizard2.LOGGER.severe(msg);
			}
			children.add(t);
		}
		if (showHidden) {
			for (String c : target.getHiddenChildren()) {
				Target t = model.getTarget(c);
				if (t == null) {
					String msg = "Invalid Child target id: " + c;
					ServerWizard2.LOGGER.severe(msg);
				}
				children.add(t);
			}
		}
		return children;
	}

	public Vector<Target> getBusTypes() {
		return model.getBusTypes();
	}
	public void loadLibrary(String path) {
		try {
			model.loadLibrary(path);
		} catch (Exception e) {
			ServerWizard2.LOGGER.severe("Unable to load library: "+path);
		}
	}
	public void runChecks(String xmlFile, String htmlFile) {

		String workingDir = ServerWizard2.getWorkingDir();
		String commandLine[] = { "perl", "-I", workingDir + "/scripts",
				workingDir + PROCESSING_SCRIPT, "-x",
				xmlFile, "-f", "-o", htmlFile };
		String commandLineStr = "";
		for (int i = 0; i < commandLine.length; i++) {
			commandLineStr = commandLineStr + commandLine[i] + " ";
		}
		ServerWizard2.LOGGER.info("Running: " + commandLineStr);
		try {
			final ProcessBuilder builder = new ProcessBuilder(commandLine)
					.redirectErrorStream(true);

			final Process process = builder.start();
			final StringWriter writer = new StringWriter();

			new Thread(new Runnable() {
				public void run() {
					char[] buffer = new char[1024];
					int len;
					InputStreamReader in = new InputStreamReader(
							process.getInputStream());

					try {
						while ((len = in.read(buffer)) != -1) {
							writer.write(buffer, 0, len);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			final int exitValue = process.waitFor();
			final String processOutput = writer.toString();
			ServerWizard2.LOGGER.info(processOutput);
			ServerWizard2.LOGGER.info("Return Code: " + exitValue);
			LogViewerDialog dlg = new LogViewerDialog(null);
			dlg.setData(processOutput);
			dlg.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}