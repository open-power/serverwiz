package com.ibm.ServerWizard2.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.TreeItem;

import com.ibm.ServerWizard2.ServerWizard2;
import com.ibm.ServerWizard2.model.Connection;
import com.ibm.ServerWizard2.model.Field;
import com.ibm.ServerWizard2.model.SystemModel;
import com.ibm.ServerWizard2.model.Target;
import com.ibm.ServerWizard2.view.LogViewerDialog;
import com.ibm.ServerWizard2.view.MainDialog;

public class TargetWizardController {
	private SystemModel model;
	private MainDialog view;
	private Boolean modelCreationMode = false;

	private String PROCESSING_SCRIPT = "scripts/gen_html.pl";

	public TargetWizardController() {

	}

	public void setModelCreationMode() {
		this.modelCreationMode = true;
	}
	public Boolean getModelCreationMode() {
		return this.modelCreationMode;
	}

	public void init() {
		try {
			model.loadLibrary("xml");
			this.initModel();
		} catch (Exception e) {
			ServerWizard2.LOGGER.severe(e.toString());
			MessageDialog.openError(null, "Error", e.toString());
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
	public Vector<Field> getAttributesAndGlobals(Target targetInstance, String path) {
		return model.getAttributesAndGlobals(targetInstance, path, !this.modelCreationMode);
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
		model.updateTargetPosition(targetInstance, parentTarget, -1);
		try {
			model.addTarget(parentTarget, targetInstance, modelCreationMode);
			view.updateInstanceTree(targetInstance, parentItem);
		} catch (Exception e) {
			MessageDialog.openError(null, "Add Target Error", e.getMessage());
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
			MessageDialog.openError(null, "Add Target Error", e.getMessage());
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
			MessageDialog.openError(null, "Error", exc.getMessage());
			exc.printStackTrace();
		}
	}

	public Boolean readXML(String filename) {
		Boolean rtn = false;
		try {
			model.readXML(filename);
			this.modelCreationMode = model.partsMode;
			rtn = model.errataUpdated;
		} catch (Exception e) {
			MessageDialog.openError(null, "Error", e.getMessage());
			e.printStackTrace();
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
	public String getWorkingDir() {
		File f = new File("").getAbsoluteFile();
		String workingDir = f.getAbsolutePath() + System.getProperty("file.separator");
		return workingDir;
	}
	public void loadLibrary(String path) {
		try {
			model.loadLibrary(path);
		} catch (Exception e) {
			ServerWizard2.LOGGER.severe("Unable to load library: "+path);
		}
	}
	public void runChecks(String xmlFile, String htmlFile) {

		String workingDir = this.getWorkingDir();
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