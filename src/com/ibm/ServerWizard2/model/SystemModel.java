package com.ibm.ServerWizard2.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.ServerWizard2.ServerWizard2;
import com.ibm.ServerWizard2.view.ErrataViewer;

public class SystemModel {
	// public Target rootTarget;
	public Vector<Target> rootTargets = new Vector<Target>();
	private DocumentBuilder builder;

	// From target instances
	private HashMap<String, Target> targetInstances = new HashMap<String, Target>();
	private Vector<Target> targetUnitList = new Vector<Target>();
	private HashMap<String, Target> targetUnitModels = new HashMap<String,Target>();

	// From target types
	private TreeMap<String, Target> targetModels = new TreeMap<String, Target>();
	private HashMap<String, Vector<Target>> childTargetTypes = new HashMap<String, Vector<Target>>();

	// From attribute types
	public TreeMap<String, Enumerator> enumerations = new TreeMap<String, Enumerator>();
	private HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();
	private TreeMap<String, Vector<String>> attributeGroups = new TreeMap<String, Vector<String>>();
	private TreeMap<String, Errata> errata = new TreeMap<String, Errata>();

	// List of targets in current system
	private Vector<Target> targetList = new Vector<Target>();
	private HashMap<String, Target> targetLookup = new HashMap<String, Target>();
	private TreeMap<String, Errata> errataList = new TreeMap<String, Errata>();

	private TreeMap<String, Target> busTypesTree = new TreeMap<String, Target>();
	private Vector<Target> busTypes = new Vector<Target>();

	private TreeMap<String, TreeMap<String, Field>> globalSettings = new TreeMap<String, TreeMap<String, Field>>();

	public Boolean partsMode = false;
	public Boolean cleanupMode = false;
	public Boolean errataUpdated = false;
	

	public Vector<Target> getBusTypes() {
		return busTypes;
	}

	public Target getTarget(String t) {
		return targetLookup.get(t);
	}

	public HashMap<String, Target> getTargetLookup() {
		return this.targetLookup;
	}

	public Vector<Target> getTargetList() {
		return this.targetList;
	}

	public Collection<Target> getTargetInstances() {
		return targetInstances.values();
	}

	public Target getRootTarget() {
		return rootTargets.get(0);
	}
	
	public Vector<Target> getTopLevelTargets() {
		//TODO: need a better way to determine top level targets
		Vector<Target> topLevel = new Vector<Target>();
		for (Target target : targetModels.values()) {
			String type = target.getType();
			if (type.equals("chip") || type.startsWith("sys") || type.equals("targetoverride")) {
				topLevel.add(target);
			}
		}
		return topLevel;
	}
	
	public Vector<Target> getUnitTargets(Boolean override) {
		//TODO: need a better way to determine top level targets
		Vector<Target> topLevel = new Vector<Target>();
		for (Target target : targetModels.values()) {
			if (override == false) {
				if (target.isUnit()) { topLevel.add(target); }
			} else {
				if (target.isOverride()) {
					topLevel.add(target);
				}
			}
		}
		return topLevel;
	}
	
	public Vector<Target> getConnectionCapableTargets() {
		Vector<Target> cards = new Vector<Target>();
		for (Target target : targetList) {
			if (target.isCard() || target.isSystem() || target.isNode()) {
					cards.add(target);
			}
		}
		return cards;
	}

	public void initBusses(Target target) {
		target.initBusses(busTypes);
	}

	public Vector<Target> getChildTargetTypes(String targetType) {
		if (targetType.equals("")) {
			Vector<Target> a = new Vector<Target>();
			for (Target t : this.targetModels.values()) {
				a.add(t);
			}
			return a;
		}
		if (childTargetTypes.get(targetType) != null) {
			Collections.sort(childTargetTypes.get(targetType));
		}
		return childTargetTypes.get(targetType);
	}
	
	
	private void writeErrata(Writer out) throws Exception {
		out.write("<appliedErratas>\n");
		for (String e : errataList.keySet()) {
			out.write("<appliedErrata>");
			out.write("\t<id>" + e + "</id>");
			out.write("\t<is_applied>" + errataList.get(e).isApplied() + "</is_applied>");
			out.write("</appliedErrata>\n");
		}
		out.write("</appliedErratas>\n");
	}
	

	private void writeGroups(Writer out) throws Exception {
		out.write("<attributeGroups>\n");
		for (String group : attributeGroups.keySet()) {
			out.write("<attributeGroup>\n");
			out.write("\t<id>" + group + "</id>\n");
			for (String attribute : attributeGroups.get(group)) {
				out.write("\t<attribute>" + attribute + "</attribute>\n");
			}
			out.write("</attributeGroup>\n");
		}
		out.write("</attributeGroups>\n");
	}
	private void writeEnumeration(Writer out) throws Exception {
		out.write("<enumerationTypes>\n");
		for (String enumeration : enumerations.keySet()) {
			Enumerator e = enumerations.get(enumeration);
			out.write("<enumerationType>\n");
			out.write("\t<id>" + enumeration + "</id>\n");
			for (Map.Entry<String, String> entry : e.enumValues.entrySet()) {
				out.write("\t\t<enumerator>\n");
				out.write("\t\t<name>" + entry.getKey() + "</name>\n");
				out.write("\t\t<value>" + entry.getValue() + "</value>\n");
				out.write("\t\t</enumerator>\n");
			}
			out.write("</enumerationType>\n");
		}
		out.write("</enumerationTypes>\n");
	}

	private NodeList isXMLValid(Document document, String tag) {
		NodeList n = document.getElementsByTagName(tag);
		if (n != null) { 
			if (n.item(0) != null) {
				String version = SystemModel.getElement((Element)n.item(0), "version");
				if (!version.isEmpty()) {
					ServerWizard2.LOGGER.info("XML Version found: "+version);
					if (Double.valueOf(version) >= ServerWizard2.VERSION_MAJOR) {
						return n;
					}
				}
			}
		}
		return null;
	}
	public void loadLibrary(String path) throws Exception {
		File xmlDir = new File(path);
		//Loads files in alphabetical order
		String[] filesStr = xmlDir.list();
		if (filesStr == null) {
			ServerWizard2.LOGGER.warning("No library loaded");
		} else {
			Arrays.sort(filesStr);
			
			for (String fstr : filesStr) {
				File file = new File(path+File.separator+fstr);
			    if (file.isFile() && file.getAbsolutePath().endsWith(".xml")) {
			    	if (file.getName().startsWith("attribute_types")) {
			    		this.loadAttributes(file.getPath());
			    	}
			    	if (file.getName().startsWith("target_types")) {
			    		this.loadTargetTypes(file.getPath());
			    	}
			    }
			}	
		}
		File partsDir = new File(path+File.separator+"parts"+File.separator);
		File filesList[] = partsDir.listFiles();
		if (filesList == null) {
			ServerWizard2.LOGGER.warning("No parts loaded");
		} else {		
			for (File file : filesList) {
			    if (file.isFile() && file.getAbsolutePath().endsWith(".xml")) {
			    	this.loadTargets(file.getPath());
			    }
			}
		}
	}
		
	// Reads a previously saved MRW
	public void readXML(String filename) throws Exception {
		ServerWizard2.LOGGER.info("Reading XML: "+filename);
		File f = new File(filename);
		this.loadLibrary(f.getParent());
		long startTime = System.currentTimeMillis();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// delete all existing instances
		this.deleteAllInstances();
		this.addUnitInstances();
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());
		Document document = builder.parse(filename);
		
		NodeList system = isXMLValid(document,"systemInstance");
		NodeList part = isXMLValid(document,"partInstance");
		if (system == null && part == null) {
			String msg = "ServerWiz cannot read this version of XML: "+filename;
			ServerWizard2.LOGGER.severe(msg);
			MessageDialog.openError(null, "XML Load Error", msg);
		}
		partsMode = false;
		String targetTag = "targetInstance";
		if (part != null) { 
			partsMode = true;
			targetTag = "targetPart";
			ServerWizard2.LOGGER.info("Setting Parts mode");
		}

		NodeList settingList = document.getElementsByTagName("globalSettings");
		Element t = (Element) settingList.item(0);
		if (t != null) {
			NodeList settingList2 = t.getElementsByTagName("globalSetting");
			for (int j = 0; j < settingList2.getLength(); ++j) {
				Element t2 = (Element) settingList2.item(j);
				this.readGlobalSettings(t2);
			}
		}
		NodeList errataList = document.getElementsByTagName("appliedErratas");
		Element te = (Element) errataList.item(0);
		if (te != null) {
			NodeList errataList2 = te.getElementsByTagName("appliedErrata");
			for (int j = 0; j < errataList2.getLength(); ++j) {
				Element t2 = (Element) errataList2.item(j);
				this.readErrata(t2);
			}
		}
		NodeList targetInstances = document.getElementsByTagName("targetInstances");
		t = (Element) targetInstances.item(0);
		NodeList targetInstanceList = t.getElementsByTagName(targetTag);

		for (int i = 0; i < targetInstanceList.getLength(); ++i) {
			t = (Element) targetInstanceList.item(i);
			String type = SystemModel.getElement(t, "type");
			if (type.length() > 0) {
				Target targetModel = this.getTargetModel(type);
				if (targetModel == null) {
					ServerWizard2.LOGGER.severe("Invalid target type: " + type);
					throw new Exception("Invalid target type: " + type);
				} else {
					Target target = new Target(targetModel);
					target.initBusses(busTypes);
					target.readInstanceXML(t, targetModels);
					this.targetLookup.put(target.getName(), target);
					this.targetList.add(target);

					if (target.isRoot()) {
						this.rootTargets.add(target);
					}
					///////
					// Check to see if new children defined in model
					Target targetInst = this.targetInstances.get(target.getType());
					if (targetInst != null) {
						HashMap <String,Boolean> childTest = new HashMap<String,Boolean>();
						for (String child : target.getAllChildren()) {
							childTest.put(child, true);
						}
						for (String child : targetInst.getChildren()) {
							if (childTest.get(child)==null) {
								target.addChild(child, false);
								if (!this.targetLookup.containsKey(child)) {
									this.targetLookup.put(child, target);
									this.targetList.add(target);
								}
								childTest.put(child, true);
							}
						}
						for (String child : targetInst.getHiddenChildren()) {
							if (childTest.get(child)==null) {
								target.addChild(child, true);
								if (!this.targetLookup.containsKey(child)) {
									this.targetLookup.put(child,target);
									this.targetList.add(target);
								}
								childTest.put(child, true);
							}
						}
					}
				}
			} else {
				throw new Exception("Empty Target Type");
			}
		}
		if (cleanupMode) { this.xmlCleanup(); }
		long endTime = System.currentTimeMillis();
		ServerWizard2.LOGGER.info("Loaded XML in " + (endTime - startTime) + " milliseconds");
		
		checkErrata();
	}
	
	public void checkErrata() {
		Vector<Errata> errataNew = new Vector<Errata>();
		
		//Determine errata that has not been acknowledged
		for (String errata_id : errata.keySet()) {
			if (!errataList.containsKey(errata_id)) {
				Errata e = new Errata(errata.get(errata_id));
				errataNew.add(e);
			}
		}
		
		HashMap<String,Errata> errataCheck = new HashMap<String,Errata>();
		for (Target tchk : this.targetLookup.values()) {
			for (Errata e : errataNew) {
				if (e.getTargetType().equals(tchk.getType())) {
					Boolean found = false;
					String cmpSummary = e.getDesc()+"\n===========================================\n";
					for (Attribute errataAttr : e.getAttributes()) {
						if (tchk.attributeExists(errataAttr.name)) {
							Attribute currentAttr = tchk.getAttributes().get(errataAttr.name);
							String cmp = errataAttr.compare(currentAttr);
							if (!cmp.isEmpty()) {
								cmpSummary = cmpSummary + cmp +"\n";
								errataCheck.put(e.getId(), e);
								e.setDetail(cmpSummary);
								found = true;
							}
						}
					}
					if (found) { e.addTarget(tchk); }
				}
			}
		}
		Vector<Errata> errataV = new Vector<Errata>();
		for (Errata e : errataCheck.values()) {
			errataV.add(e);
		}
		if (errataCheck.size() > 0) {
			ErrataViewer dlg = new ErrataViewer(null,errataV);
			int rtn = dlg.open();
			if (rtn == Dialog.OK) {
				for (Errata e : errataV) {
					if (e.isApplied()) {
						e.updateAttributes();
					}
					errataUpdated = true;
					errataList.put(e.getId(), e);
				}
			}
		}
	}
	
	///////////////////////////////////////////////
	// global settings
	
	public Field setGlobalSetting(String path, String attribute, String value) {
		TreeMap<String, Field> s = globalSettings.get(path);
		if (s == null) {
			s = new TreeMap<String, Field>();
			globalSettings.put(path, s);
		}
		Field f = s.get(attribute);
		if (f == null) {
			f = new Field();
			f.attributeName = attribute;
			s.put(attribute, f);
		}
		f.value = value;
		return f;
	}

	public Boolean isGlobalSetting(String path, String attribute) {
		TreeMap<String, Field> s = globalSettings.get(path);
		if (s == null) {
			return false;
		}
		Field f=s.get(attribute);
		if (f==null) {
			return false;
		}
		return true;
	}

	public Field getGlobalSetting(String path, String attribute) {
		TreeMap<String, Field> s = globalSettings.get(path);
		if (s == null) {
			if (s == null) {
				s = new TreeMap<String, Field>();
				globalSettings.put(path, s);
			}			
			return null;
		}
		Field f=s.get(attribute);
		return f;
	}

	public TreeMap<String, Field> getGlobalSettings(String path) {
		TreeMap<String, Field> s = globalSettings.get(path);
		return s;
	}

	private void writeGlobalSettings(Writer out) throws Exception {
		out.write("<globalSettings>\n");
		for (Map.Entry<String, TreeMap<String, Field>> entry : this.globalSettings.entrySet()) {
			out.write("<globalSetting>\n");
			out.write("\t<id>" + entry.getKey() + "</id>\n");
			for (Map.Entry<String, Field> setting : entry.getValue().entrySet()) {
				out.write("\t<property>\n");
				out.write("\t<id>" + setting.getKey() + "</id>\n");
				out.write("\t<value>" + setting.getValue().value + "</value>\n");
				out.write("\t</property>\n");
			}
			out.write("</globalSetting>\n");
		}
		out.write("</globalSettings>\n");
	}

	private void readGlobalSettings(Element setting) {
		String targetId = SystemModel.getElement(setting, "id");
		NodeList propertyList = setting.getElementsByTagName("property");
		for (int i = 0; i < propertyList.getLength(); ++i) {
			Element t = (Element) propertyList.item(i);
			String property = SystemModel.getElement(t, "id");
			String value = SystemModel.getElement(t, "value");
			this.setGlobalSetting(targetId, property, value);
		}
	}
	private void readErrata(Element setting) {
		String errata_id = SystemModel.getElement(setting, "id");
		if (this.errata.containsKey(errata_id)) {
			Errata e = new Errata(this.errata.get(errata_id));
			errataList.put(errata_id, e);
		}
	}
	
	/////////////////////////////////////////////////
	// Writes MRW to file
	public void writeXML(String filename, Boolean partsMode) throws Exception {
		Writer out = new BufferedWriter(new FileWriter(filename));
		String topTag = "systemInstance";
		if (partsMode) { topTag = "partInstance"; }
		
		out.write("<"+topTag+">\n");
		out.write("<version>"+ServerWizard2.getVersionString()+"</version>\n");
		if (!partsMode) {
			this.writeEnumeration(out);
			this.writeGlobalSettings(out);
			this.writeGroups(out);
			this.writeErrata(out);
		}
		out.write("<targetInstances>\n");
		HashMap<String, Boolean> targetWritten = new HashMap<String, Boolean>();
		for (Target target : targetList) {
			if (partsMode) {
				target.writeTargetXML(out, targetLookup, targetWritten);
			} else {
				target.writeInstanceXML(out, targetLookup, targetWritten);
			}
		}
		out.write("</targetInstances>\n");
		out.write("</"+topTag+">\n");
		out.close();
	}

	public void addTarget(Target parentTarget, Target newTarget,Boolean pathMode) throws Exception {
		if (parentTarget == null) {
			this.rootTargets.add(newTarget);
			newTarget.setRoot();
			if (pathMode) {
				newTarget.parent = newTarget.getType();
				newTarget.setType(newTarget.parent+"-"+newTarget.getRawName());
			}
		} else {
			newTarget.clearRoot();
			if (pathMode) {
				String name = newTarget.getRawName();
				if (name.isEmpty()) { name = newTarget.getIdPrefix(); }
				newTarget.setName(parentTarget.getName()+"."+name);
			}
			parentTarget.addChild(newTarget.getName(), false);
		}
		this.updateTargetList(newTarget);
		initBusses(newTarget);
	}
	public void loadTargetTypes(String fileName) throws SAXException,
			IOException, ParserConfigurationException {
		ServerWizard2.LOGGER.info("Loading Target Types: " + fileName);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());

		Document document = builder.parse(fileName);
		NodeList targetList = document.getElementsByTagName("targetType");
		for (int i = 0; i < targetList.getLength(); ++i) {
			Element t = (Element) targetList.item(i);
			Target target = new Target();
			target.readModelXML(t, attributes);
			targetModels.put(target.getType(), target);
		}
		for (Map.Entry<String, Target> entry : targetModels.entrySet()) {
			Target target = entry.getValue();

			// add inherited attributes
			addParentAttributes(target, target);
			if (target.getAttribute("CLASS").equals("BUS")) {
				busTypesTree.put(entry.getKey(),target);
			}
		}
		busTypes.removeAllElements();
		for (Target t : busTypesTree.values()) {
			busTypes.add(t);
		}
	}

	public void loadAttributes(String fileName) throws SAXException,
			IOException, ParserConfigurationException {
		ServerWizard2.LOGGER.info("Loading Attributes: " + fileName);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());

		Document document = builder.parse(fileName);
		NodeList enumList = document.getElementsByTagName("enumerationType");
		for (int i = 0; i < enumList.getLength(); ++i) {
			Element t = (Element) enumList.item(i);
			Enumerator en = new Enumerator();
			en.readXML(t);
			enumerations.put(en.id, en);
		}
		NodeList attrList = document.getElementsByTagName("attribute");
		for (int i = 0; i < attrList.getLength(); ++i) {
			Element t = (Element) attrList.item(i);
			Attribute a = new Attribute();
			a.readModelXML(t);
			attributes.put(a.name, a);
			
			if (!a.group.isEmpty()) {
				Vector<String> grp = attributeGroups.get(a.group);
				if (grp == null) {
					grp = new Vector<String>();
					attributeGroups.put(a.group, grp);
				}
				grp.add(a.name);
			}

			if (a.getValue().getType().equals("enumeration")) {
				a.getValue().setEnumerator(enumerations.get(a.value.getFields().get(0).name));
			}
		}
	}

	public void loadTargets(String filename) throws Exception {
		ServerWizard2.LOGGER.info("Loading Part: " + filename);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());
		Document document = builder.parse(filename);
		NodeList targetInstanceList = document.getElementsByTagName("targetPart");
		for (int i = 0; i < targetInstanceList.getLength(); ++i) {
			Element t = (Element) targetInstanceList.item(i);
			String type = SystemModel.getElement(t, "type");
			Target tmodel = targetModels.get(type);
			Target target = null;
			if (tmodel == null) {
				target = new Target();
			} else {
				target = new Target(tmodel);
			}
			target.readTargetXML(t, targetModels, attributes);
			addParentAttributes(target, target);
			if (target.isRoot()) {
				target.clearRoot();
				targetModels.put(target.getType(), target);
				targetInstances.put(target.getType(), target);
				Vector<String> parentTypes = target.getParentType();
				for (int j = 0; j < parentTypes.size(); j++) {
					String parentType = parentTypes.get(j);
					
					Vector<Target> childTypes = childTargetTypes.get(parentType);
					if (childTypes == null) {
						childTypes = new Vector<Target>();
						childTargetTypes.put(parentType, childTypes);
					}
					childTypes.add(target);
				}				
			} else {
				if (!targetUnitModels.containsKey(target.getName())) {
					this.targetUnitList.add(target);
					this.targetUnitModels.put(target.getName(), target);
				}
				if (!targetModels.containsKey(target.getType())) {
					targetModels.put(target.getType(), target);
				}
			}
		}
	}
	public void loadErrata(String filename) throws Exception {
		ServerWizard2.LOGGER.info("Loading Errata: " + filename);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());
		Document document = builder.parse(filename);
		NodeList list = document.getElementsByTagName("errata");
		for (int i = 0; i < list.getLength(); ++i) {
			Element t = (Element) list.item(i);
			Errata e = new Errata();
			e.read(t, attributes);
			errata.put(e.getId(), e);
		}	
	}
	
	////////////////////////////////////////////////////////////////////////
	// Target manipulation
	public Target getTargetInstance(String type) {
		return targetInstances.get(type);
	}
	
	public HashMap<String,Target> getUnitTargetModel() {
		return this.targetUnitModels;
	}
	// Add target to target database.
	// only contains targets that user has added
	// not library targets
	private void updateTargetList(Target target) throws Exception {
		String id = target.getName();
		if (!this.targetLookup.containsKey(id)) {
			this.targetLookup.put(id, target);
			this.targetList.add(target);
		} else {
			String msg="Duplicate Target: "+target.getName();
			ServerWizard2.LOGGER.warning(msg);
			throw new Exception(msg);
		}
	}

	private void addParentAttributes(Target childTarget, Target t) {
		if (t == null) {
			return;
		}
		if (t.parent == null || t.parent.isEmpty()) {
			return;
		}
		Target parent = targetModels.get(t.parent);
		if (parent == null) {
			MessageDialog.openError(null, "Error", "Invalid parent target: "+t.parent );
		}
		childTarget.copyAttributesFromParent(parent);
		addParentAttributes(childTarget, parent);
	}
	
	public void addUnitInstances() {
		for (Target target : this.targetUnitList) {
			this.targetLookup.put(target.getName(), target);
		}
	}

	public void updateTargetPosition(Target target, Target parentTarget, int position) {
		if (position > 0) {
			target.setPosition(position);
			return;
		}
		if (parentTarget == null) {
			target.setName(target.getIdPrefix());
			target.setPosition(-1);
			return;
		}
		int p = -1;
		// set target position to +1 of any target found of same type
		for (Target t : targetList) {
			if (t.getType().equals(target.getType())) {
				if (t.getPosition() >= p) {
					p = t.getPosition();
				}
			}
		}
		target.setPosition(p + 1);
		target.setSpecialAttributes();
	}
	public TreeMap<String, Target> getTargetModels() {
		return targetModels;
	}

	public Target getTargetModel(String t) {
		return targetModels.get(t);
	}

	public void deleteAllInstances() {
		errataUpdated = false;
		this.targetList.clear();
		this.targetLookup.clear();
		this.rootTargets.clear();
		this.globalSettings.clear();
		this.errataList.clear();
	}

	public void deleteTarget(Target deleteTarget) {
		//if (deleteTarget == null) {
		//	return;
		//}
		targetList.remove(deleteTarget);
		//Vector<String> children = deleteTarget.getAllChildren();
		//for (String s : children) {
		//	Target d = targetLookup.get(s);
		//	deleteTarget(d);
		//}
		
		for (Target t : targetList) {
			t.removeChildren(deleteTarget.getName());
		}
		this.targetLookup.remove(deleteTarget.getName());
	}
	
	/////////////////////////////////////////////////////////////////
	// Utility static methods
	public static String getElement(Element a, String e) {
		Node n = a.getElementsByTagName(e).item(0);
		if (n != null) {
			Node cn = n.getChildNodes().item(0);
			if (cn == null) {
				return "";
			}
			return cn.getNodeValue();
		}
		return "";
	}

	public static Boolean isElementDefined(Element a, String e) {
		Node n = a.getElementsByTagName(e).item(0);
		if (n != null) {
			Node cn = n.getChildNodes().item(0);
			if (cn == null) {
				return true;
			}
			return true;
		}
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////
	// Special method to cleanup past bugs
	private void targetWalk(Target target, String path, HashMap<String,Target> targets) {
		targets.put(path, target);
		for (String child : target.getChildren()) {
			Target childTarget = getTarget(child);
			targetWalk(childTarget, path + "/" + child, targets);
		}
	}
	
	private void xmlCleanup() {
		String path = "/"+this.getRootTarget().getName();
		HashMap<String,Target> targets = new HashMap<String,Target>();
		targetWalk(this.getRootTarget(),path,targets);
		
		ServerWizard2.LOGGER.info("Running XML cleanup...");
		
		// IO_CONFIG_SELECT bug
		TreeMap<String, TreeMap<String,Field>> tmpSettings = new TreeMap<String, TreeMap<String,Field>>(globalSettings);
		
		for (Map.Entry<String, TreeMap<String,Field>> settings : tmpSettings.entrySet()) {
			TreeMap<String,Field> tmpFields = new TreeMap<String,Field>(settings.getValue());
			Target t = targets.get(settings.getKey());
			if (t == null) {
				ServerWizard2.LOGGER.warning("Target not found, removing: "+settings.getKey());
				globalSettings.remove(settings.getKey());
			} else {
				for (Field f : tmpFields.values()) {
					Attribute a = t.getAttributes().get(f.attributeName);
					if (a == null) {
						ServerWizard2.LOGGER.info("Not an attribute in target, removing: "+f.attributeName);
						globalSettings.get(settings.getKey()).remove(f.attributeName);
					} else {
						if (!a.isGlobal()) {
							globalSettings.get(settings.getKey()).remove(f.attributeName);
							ServerWizard2.LOGGER.info("Removing global property: "+f.attributeName);
						}
					}
				}
			}
			TreeMap<String,Field> tmpSettings2 = globalSettings.get(settings.getKey());
			
			if (tmpSettings2 != null) {
				if (tmpSettings2.isEmpty()) {
					ServerWizard2.LOGGER.info("Removing global target: "+settings.getKey());
					globalSettings.remove(settings.getKey());
				}
			}
		}
		// End IO_CONFIG_SELECT bug
	}
	
}
