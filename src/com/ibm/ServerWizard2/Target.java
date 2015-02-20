package com.ibm.ServerWizard2;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Target implements Comparable<Target>, java.io.Serializable {
	private static final long serialVersionUID = 1L;

	private String name = "";
	private String type = "";
	private int position = -1;
	public String parent = ""; // says which parent to inherit attributes from
								// target_types
	public boolean instanceModel = false;
	public boolean isLibraryTarget = false;
	private Vector<String> parentType = new Vector<String>();
	private TreeMap<String, Attribute> attributes = new TreeMap<String, Attribute>();
	//private Vector<Target> children = new Vector<Target>();
	private Vector<String> children = new Vector<String>();
	private Vector<String> childrenHidden = new Vector<String>();
	private TreeMap<Target,Vector<Connection>> busses = new TreeMap<Target,Vector<Connection>>();
	private Boolean busInited=false;
	private Boolean hidden = false;
	private HashMap<String,Boolean> childrenBusTypes = new HashMap<String,Boolean>();

	public Target() {
	}

	public Target(Target s) {
		this.setName(s.name);
		this.setType(s.type);
		this.parent = s.parent;
		this.position = s.position;
		this.hidden = s.hidden;
		this.parentType.addAll(s.parentType);
		this.isLibraryTarget=s.isLibraryTarget;

		for (Map.Entry<String, Attribute> entry : s.getAttributes().entrySet()) {
			String key = new String(entry.getKey());
			Attribute value = new Attribute(entry.getValue());
			this.attributes.put(key, value);
		}
	}
	public TreeMap<Target,Vector<Connection>> getBusses() {
		return busses;
	}

	public void hide(Boolean h) {
		this.hidden=h;
	}
	public Boolean isHidden() {
		return this.hidden;
	}

	public void linkBusses(Target t){
		busses=t.getBusses();
	}

	public String getName() {
		if (position==-1 && !name.isEmpty()) {
			return name;
		}
		if (!name.isEmpty()) {
			return name + "-" + position;
		}
		return getIdPrefix() + "-" + position;
	}

	public String getRawName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	////////////////////////////////////////////
	// Target children handling
	public Vector<String> getChildren() {
		return this.children;
	}
	public Vector<String> getHiddenChildren() {
		return this.childrenHidden;
	}
	public Vector<String> getAllChildren() {
		Vector<String> all = new Vector<String>();
		all.addAll(this.children);
		all.addAll(this.childrenHidden);
		return all;
	}
	public void copyChildren(Target t) {
		for (String c : t.children) {
			this.children.add(c);
		}
		for (String c : t.childrenHidden) {
			this.childrenHidden.add(c);
		}
	}
	public void removeChildren(String child) {
		children.remove(child);
		childrenHidden.remove(child);
	}
	public void addChild(String child,boolean hidden) {
		if (hidden) {
			childrenHidden.add(child);
		} else {
			children.add(child);
		}
	}

	public Boolean isPluggable() {
		String c = this.getAttribute("CLASS");

		if (c == null) {
			return false;
		}

		if (c.equals("CONNECTOR")) {
			return true;
		}
		return false;
	}

	public void setPosition(String pos) {
		this.position = Integer.parseInt(pos);
	}

	public void setPosition(int pos) {
		this.position = pos;
	}

	public int getPosition() {
		return this.position;
	}

	public Vector<String> getParentType() {
		return this.parentType;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public String getIdPrefix() {
		String t = type.split("-")[1];
		if (t.equals("processor")) {
			t = "proc";
		}
		return t;
	}

	////////////////////////////////////////////////////////
	// Attribute handling
	public TreeMap<String, Attribute> getAttributes() {
		return attributes;
	}
	public void copyAttributes(Target s) {
		for (Map.Entry<String, Attribute> entry : s.getAttributes().entrySet()) {
			String key = new String(entry.getKey());
			Attribute value = new Attribute(entry.getValue());
			this.attributes.put(key, value);
		}
	}

	public void linkAttributes(Target t) {
		this.attributes.clear();
		this.attributes = t.getAttributes();
	}
	public boolean attributeExists(String attribute) {
		if (attributes.get(attribute) == null) {
			return false;
		}
		return true;
	}
	public String getAttribute(String attribute) {
		if (attributes.get(attribute) == null) {
			return "";
		}
		return attributes.get(attribute).getValue().getValue();
	}

	public void copyAttributesFromParent(Target s) {
		for (Map.Entry<String, Attribute> entry : s.getAttributes().entrySet()) {
			String key = entry.getKey();
			Attribute tmpAttribute = entry.getValue();
			Attribute localAttribute = this.attributes.get(key);
			// inherited attribute was already added when instance created
			if (localAttribute != null) {
				localAttribute.inherited = s.type;
			}
			else {
				Attribute attribute = new Attribute(tmpAttribute);
				attribute.inherited = s.type;
				this.attributes.put(key, attribute);
			}
		}
	}

	public void updateAttributeValue(String attributeName, String value) {
		Attribute attribute = attributes.get(attributeName);
		if (attribute == null) {
			throw new NullPointerException("Invalid Attribute " + attributeName + " in Target "
					+ this.type);
		}
		AttributeValue val = attribute.getValue();
		val.setValue(value);
	}

	public void updateAttributeValue(String attributeName, AttributeValue value) {
		Attribute attribute = attributes.get(attributeName);
		if (attribute == null) {
			throw new NullPointerException("Invalid Attribute " + attributeName + " in Target "
					+ this.type);
		}
		attribute.getValue().setValue(value);
	}
	public Boolean isInput() {
		String dir = this.getAttribute("DIRECTION");
		if (dir.equals("IN") || dir.equals("INOUT")) {
			return true;
		}
		return false;
	}
	public Boolean isOutput() {
		String dir = this.getAttribute("DIRECTION");
		if (dir.equals("OUT") || dir.equals("INOUT")) {
			return true;
		}
		return false;
	}

	public Boolean isSystem() {
		return (this.getAttribute("CLASS").equals("SYS"));
	}
	public Boolean isCard() {
		return (this.getAttribute("CLASS").equals("CARD") || this.getAttribute("CLASS").equals(
				"MOTHERBOARD"));
	}
	public Boolean isConnector() {
		return (this.getAttribute("CLASS").equals("CONNECTOR"));
	}
	public Boolean isNode() {
		return (this.getAttribute("CLASS").equals("ENC"));
	}

	public void setAttributeValue(String attr, String value) {
		Attribute attribute = this.attributes.get(attr);
		if (attribute == null) {
			return;
		}
		attribute.getValue().setValue(value);
	}

	public void setSpecialAttributes() {
		this.setAttributeValue("POSITION", String.valueOf(this.getPosition()));

	}
/*
	public String toString() {
		String s = "TARGET: " + this.type;
		for (Map.Entry<String, Attribute> entry : this.getAttributes().entrySet()) {
			Attribute attr = new Attribute(entry.getValue());
			s = s + "\t" + attr.toString() + "\n";
		}
		return s;
	}
*/
	public int compareTo(Target arg0) {
		Target t = (Target)arg0;
		return this.getType().compareTo(t.getType());
	}


	///////////////////////////////////////////////////
	// connection/bus handling
	public Boolean isBusHidden(String busType) {
		return !this.childrenBusTypes.containsKey(busType);
	}
	public HashMap<String,Boolean> hideBusses(HashMap<String,Target> targetLookup) {
		HashMap<String,Boolean> childBusTypes;
		childrenBusTypes.clear();
		String thisBusType = this.getAttribute("BUS_TYPE");
		if (!thisBusType.isEmpty() &&!thisBusType.equals("NA")) {
			this.childrenBusTypes.put(thisBusType,true);
		}
		for (String childStr : this.getChildren()) {
			Target child = targetLookup.get(childStr);
			if (child==null) {
				ServerWizard2.LOGGER.severe("Child Target "+childStr+" not found");
			}
			String busType = child.getAttribute("BUS_TYPE");
			if (!busType.isEmpty() && !busType.equals("NA")) {
				this.childrenBusTypes.put(busType, true);
			}
			childBusTypes = child.hideBusses(targetLookup);
			for (String bus : childBusTypes.keySet()) {
				this.childrenBusTypes.put(bus, true);
			}
		}
		return this.childrenBusTypes;
	}
	public Connection addConnection(Target busTarget,ConnectionEndpoint source,ConnectionEndpoint dest, boolean cabled) {
		if (busTarget==null || source==null || dest==null) {
			//TODO: error message
			return null;
		}
		Vector<Connection> c = busses.get(busTarget);
		if (c==null) {
			c = new Vector<Connection>();
			busses.put(busTarget, c);
		}
		Connection conn = new Connection();
		conn.busType = busTarget.getType();
		conn.source=source;
		conn.dest=dest;
		conn.cabled = cabled;
		conn.busTarget = new Target(busTarget);
		c.add(conn);
		return conn;
	}
	public void deleteConnection(Target busTarget,Connection conn) {
		Vector<Connection> connList = busses.get(busTarget);
		connList.remove(conn);
	}
	public void initBusses(Vector<Target> v) {
		if (busInited) { return; }
		this.busInited=true;

		for (Target s : v) {
			Vector<Connection> connections = new Vector<Connection>();
			this.busses.put(s, connections);
		}
	}

	////////////////////////////////////////////////////
	// XML file handling
	public void readModelXML(Element target, HashMap<String, Attribute> attrMap) {
		type = SystemModel.getElement(target, "id");
		parent = SystemModel.getElement(target, "parent");
		NodeList parentList = target.getElementsByTagName("parent_type");
		for (int i = 0; i < parentList.getLength(); i++) {
			Element e = (Element) parentList.item(i);
			parentType.add(e.getChildNodes().item(0).getNodeValue());
		}
		NodeList attributeList = target.getElementsByTagName("attribute");
		for (int i = 0; i < attributeList.getLength(); ++i) {
			String attrId = SystemModel.getElement((Element) attributeList.item(i), "id");
			Attribute attributeLookup = attrMap.get(attrId);
			if (attributeLookup == null) {
				throw new NullPointerException("Invalid attribute id: " + attrId + "(" + type + ")");
			}
			Attribute a = new Attribute(attributeLookup);
			if (a.value==null) {
				throw new NullPointerException("Unknown attribute value type: " + attrId + "(" + type + ")");
			}
			attributes.put(a.name, a);
			a.value.readInstanceXML((Element) attributeList.item(i));
		}
	}

	public void readInstanceXML(Element t, TreeMap<String, Target> targetModels) throws Exception {
		instanceModel = true;
		name = SystemModel.getElement(t, "instance_name");
		type = SystemModel.getElement(t, "type");
		String library_target = SystemModel.getElement(t, "library_target");
		if (library_target.equals("true")) {
			this.isLibraryTarget=true;
		} else {
			this.isLibraryTarget=false;
		}
		if (name.isEmpty()) {
			name = SystemModel.getElement(t, "id");
			instanceModel = false;
		} else {
			String tmpPos = SystemModel.getElement(t, "position");
			if (!tmpPos.isEmpty()) {
				setPosition(tmpPos);
			}
		}

		NodeList childList = t.getElementsByTagName("child_id");
		for (int j = 0; j < childList.getLength(); ++j) {
			Element attr = (Element) childList.item(j);
			//TargetName targetName = new TargetName(attr.getFirstChild().getNodeValue(),false);
			children.add(attr.getFirstChild().getNodeValue());
		}
		childList = t.getElementsByTagName("hidden_child_id");
		for (int j = 0; j < childList.getLength(); ++j) {
			Element attr = (Element) childList.item(j);
			//argetName targetName = new TargetName(attr.getFirstChild().getNodeValue(),true);
			childrenHidden.add(attr.getFirstChild().getNodeValue());
		}

		NodeList attrList = t.getElementsByTagName("attribute");
		for (int j = 0; j < attrList.getLength(); ++j) {
			Element attr = (Element) attrList.item(j);
			String id = SystemModel.getElement(attr, "id");
			Attribute a = attributes.get(id);
			if (a==null) {
				ServerWizard2.LOGGER.info("Attribute dropped: "+id+" from "+this.getName());
			} else {
				a.value.readInstanceXML(attr);
			}
		}
		NodeList busList = t.getElementsByTagName("bus");
		for (int j = 0; j < busList.getLength(); ++j) {
			Element bus = (Element) busList.item(j);
			String busType = SystemModel.getElement(bus, "bus_type");
			Connection conn = new Connection();
			conn.busType=busType;
			Target busTarget=targetModels.get(busType);
			if (busTarget==null) {
				throw new Exception("Invalid Bus Type "+busType+" for target "+this.getName());
			}
			conn.busTarget = new Target(busTarget);
			conn.readInstanceXML(bus);
			busses.get(busTarget).add(conn);
		}
	}
	public void writeInstanceXML(Writer out,HashMap<String,Target> targetLookup,HashMap<String,Boolean>targetWritten) throws Exception {
		if (targetWritten.containsKey(this.getName())) {
			return;
		}
		targetWritten.put(this.getName(), true);
		out.write("<targetInstance>\n");
		out.write("\t<id>" + this.getName() + "</id>\n");
		out.write("\t<type>" + this.getType() + "</type>\n");
		out.write("\t<library_target>" + this.isLibraryTarget + "</library_target>\n");
		//out.write("\t<class>" + this.getAttribute("CLASS") + "</class>\n");
		if (!this.name.isEmpty()) {
			out.write("\t<instance_name>" + this.name + "</instance_name>\n");
		} else {
			out.write("\t<instance_name>" + this.getIdPrefix() + "</instance_name>\n");
		}

		out.write("\t<position>" + getPosition() + "</position>\n");
		//write children
		for (String childStr : this.children) {
			out.write("\t<child_id>"+childStr+"</child_id>\n");
		}
		for (String childStr : this.childrenHidden) {
			out.write("\t<hidden_child_id>"+childStr+"</hidden_child_id>\n");
		}
		//write attributes
		for (Map.Entry<String, Attribute> entry : getAttributes().entrySet()) {
			Attribute attr = new Attribute(entry.getValue());
			attr.writeInstanceXML(out);

		}
		//write busses
		for (Map.Entry<Target, Vector<Connection>> entry : busses.entrySet()) {
			for (Connection conn : entry.getValue()) {
				conn.writeInstanceXML(out);
			}
		}
		out.write("</targetInstance>\n");

		//recursively write children
		for (String childStr : this.getAllChildren()) {
			Target child = targetLookup.get(childStr);
			child.writeInstanceXML(out, targetLookup, targetWritten);
		}
	}
}
