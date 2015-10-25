package com.ibm.ServerWizard2;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SystemModel {
	// public Target rootTarget;
	public Vector<Target> rootTargets = new Vector<Target>();
	private DocumentBuilder builder;

	// From target instances
	private HashMap<String, Target> targetInstances = new HashMap<String, Target>();
	private Vector<Target> targetUnitList = new Vector<Target>();

	// From target types
	private TreeMap<String, Target> targetModels = new TreeMap<String, Target>();
	public HashMap<String, Vector<Target>> childTargetTypes = new HashMap<String, Vector<Target>>();

	// From attribute types
	public TreeMap<String, Enumerator> enumerations = new TreeMap<String, Enumerator>();
	public HashMap<String, Attribute> attributes = new HashMap<String, Attribute>();

	// List of targets in current system
	private Vector<Target> targetList = new Vector<Target>();
	private HashMap<String, Target> targetLookup = new HashMap<String, Target>();

	private Vector<Target> busTypes = new Vector<Target>();
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private TreeMap<String, TreeMap<String, Field>> globalSettings = new TreeMap<String, TreeMap<String, Field>>();

	public String logData;

	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

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

	public Vector<Target> getConnectionCapableTargets() {
		Vector<Target> cards = new Vector<Target>();
		for (Target target : targetList) {
			if (target.isCard() || target.isSystem() || target.isNode()) {
				//if (!target.isLibraryTarget) {
					cards.add(target);
				//}
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

	public Integer getEnumValue(String enumerator, String value) {
		Enumerator e = enumerations.get(enumerator);
		return e.getEnumInt(value);
	}

	public String getEnumValueStr(String enumerator, String value) {
		Enumerator e = enumerations.get(enumerator);
		return e.getEnumStr(value);
	}

	public static String getElement(Element a, String e) {
		Node n = a.getElementsByTagName(e).item(0);
		if (n != null) {
			Node cn = n.getChildNodes().item(0);
			if (cn == null) {
				return "";
			}
			return n.getChildNodes().item(0).getNodeValue();
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

	public TreeMap<String, Target> getTargetModels() {
		return targetModels;
	}

	public Target getTargetModel(String t) {
		return targetModels.get(t);
	}

	public void deleteAllInstances() {
		this.targetList.clear();
		this.targetLookup.clear();
		this.rootTargets.clear();
		this.globalSettings.clear();
	}

	public void deleteTarget(Target deleteTarget) {
		targetList.remove(deleteTarget);
		for (Target t : targetList) {
			t.removeChildren(deleteTarget.getName());
		}
		this.targetLookup.remove(deleteTarget.getName());
		changes.firePropertyChange("DELETE_TARGET", "", "");
	}

	// Reads a previously saved MRW
	public void readXML(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// delete all existing instances
		this.deleteAllInstances();
		this.addUnitInstances();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());
		Document document = builder.parse(filename);

		NodeList settingList = document.getElementsByTagName("globalSetting");
		for (int i = 0; i < settingList.getLength(); ++i) {
			Element t = (Element) settingList.item(i);
			this.readGlobalSettings(t);
		}

		NodeList targetInstanceList = document.getElementsByTagName("targetInstance");
		for (int i = 0; i < targetInstanceList.getLength(); ++i) {
			Element t = (Element) targetInstanceList.item(i);
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
					//if (this.targetLookup.containsKey(target.getName())) {
						// ServerWizard2.LOGGER.warning("Duplicate Target: "+target.getName());
					//} else {
						this.targetLookup.put(target.getName(), target);
						this.targetList.add(target);
					//}
					if (target.getAttribute("CLASS").equals("SYS")) {
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
	}

	public void writeEnumeration(Writer out) throws Exception {
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
	}

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
			Field f=this.setGlobalSetting(path, attribute, "");
			return f;
		}
		Field f=s.get(attribute);
		if (f==null) {
			f=this.setGlobalSetting(path, attribute, "");
		}
		return f;
	}

	public TreeMap<String, Field> getGlobalSettings(String path) {
		TreeMap<String, Field> s = globalSettings.get(path);
		return s;
	}

	public void writeGlobalSettings(Writer out) throws Exception {
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
	}

	public void readGlobalSettings(Element setting) {
		String targetId = SystemModel.getElement(setting, "id");
		NodeList propertyList = setting.getElementsByTagName("property");
		for (int i = 0; i < propertyList.getLength(); ++i) {
			Element t = (Element) propertyList.item(i);
			String property = SystemModel.getElement(t, "id");
			String value = SystemModel.getElement(t, "value");
			this.setGlobalSetting(targetId, property, value);
		}
	}

	// Writes MRW to file
	public void writeXML(String filename) throws Exception {
		Writer out = new BufferedWriter(new FileWriter(filename));
		out.write("<targetInstances>\n");
		this.writeEnumeration(out);
		this.writeGlobalSettings(out);
		HashMap<String, Boolean> targetWritten = new HashMap<String, Boolean>();
		for (Target target : targetList) {
			target.writeInstanceXML(out, targetLookup, targetWritten);
		}
		out.write("</targetInstances>\n");
		out.close();
	}

	public void addTarget(Target parentTarget, Target newTarget) throws Exception {
		if (parentTarget == null) {
			this.rootTargets.add(newTarget);
		} else {
			parentTarget.addChild(newTarget.getName(), false);
		}
		this.updateTargetList(newTarget);
		initBusses(newTarget);
		changes.firePropertyChange("ADD_TARGET", "", "");
	}

	public Target getTargetInstance(String type) {
		return targetInstances.get(type);
	}

	// Add target to target database.
	// only contains targets that user has added
	// not library targets
	public void updateTargetList(Target target) throws Exception {
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

	public void addParentAttributes(Target childTarget, Target t) {
		if (t == null) {
			return;
		}
		Target parent = targetModels.get(t.parent);
		if (parent == null) {
			return;
		}
		childTarget.copyAttributesFromParent(parent);
		addParentAttributes(childTarget, parent);
	}

	public void loadTargetTypes(DefaultHandler errorHandler, String fileName) throws SAXException,
			IOException, ParserConfigurationException {
		ServerWizard2.LOGGER.info("Loading Target Types: " + fileName);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		builder.setErrorHandler(errorHandler);

		Document document = builder.parse(fileName);
		NodeList targetList = document.getElementsByTagName("targetType");
		for (int i = 0; i < targetList.getLength(); ++i) {
			Element t = (Element) targetList.item(i);
			Target target = new Target();
			target.readModelXML(t, attributes);
			targetModels.put(target.getType(), target);
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
		}
		for (Map.Entry<String, Target> entry : targetModels.entrySet()) {
			Target target = entry.getValue();

			// add inherited attributes
			addParentAttributes(target, target);
			if (target.getAttribute("CLASS").equals("BUS")) {
				busTypes.add(target);
			}
		}
	}

	public void loadAttributes(DefaultHandler errorHandler, String fileName) throws SAXException,
			IOException, ParserConfigurationException {
		ServerWizard2.LOGGER.info("Loading Attributes: " + fileName);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		builder.setErrorHandler(errorHandler);

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

			if (a.getValue().getType().equals("enumeration")) {
				a.getValue().setEnumerator(enumerations.get(a.name));
			}
		}
	}

	public void loadTargetInstances(String filename) throws Exception {
		ServerWizard2.LOGGER.info("Loading Instances: " + filename);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new XmlHandler());
		Document document = builder.parse(filename);
		NodeList targetInstanceList = document.getElementsByTagName("targetInstance");
		for (int i = 0; i < targetInstanceList.getLength(); ++i) {
			Element t = (Element) targetInstanceList.item(i);
			String type = SystemModel.getElement(t, "type");
			if (type.length() > 0) {
				Target targetModel = this.getTargetModel(type);
				if (targetModel == null) {
					ServerWizard2.LOGGER.severe("Invalid target type: " + type);
					throw new Exception("Invalid target type: " + type);
				} else {
					Target target = new Target(targetModel);
					target.readInstanceXML(t, targetModels);
					if (target.instanceModel) {
						targetInstances.put(type, target);
						target.isLibraryTarget = true;
					} else {
						// this.targetLookup.put(target.getName(), target);
						this.targetUnitList.add(target);
						target.isLibraryTarget = true;
					}
				}
			} else {
				throw new Exception("Empty Target Type");
			}
		}
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
}
