package com.ibm.ServerWizard2.model;

import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.ServerWizard2.utility.ServerwizMessageDialog;

public class Errata {
	private String id = "";
	private String desc = "";
	private Boolean applied = false;
	private Vector<Attribute> attributes = new Vector<Attribute>();
	private String compareDetail = "";
	private Vector<Target> targets = new Vector<Target>();
	private String targetType = "";

	public Errata() {
		
	}
	public Errata(Errata e) {
		id = e.id;
		desc = e.desc;
		applied = e.applied;
		targetType = e.targetType;
		//don't deep copy attributes
		attributes = e.attributes;
	}
	public void addTarget(Target t) {
		targets.add(t);
	}
	public String getDetail() {
		return compareDetail;
	}
	public void setDetail(String d) {
		this.compareDetail = d;
	}
	public void setApplied(Boolean applied) {
		this.applied = applied;
	}
	public Boolean isApplied() { 
		return applied;
	}
	public String getTargetType() {
		return this.targetType;
	}
	public void updateAttributes() {
		for (Target t : targets) {
			for (Attribute a : attributes) {
				Attribute attrNew = new Attribute(a);
				t.getAttributes().put(attrNew.name,attrNew);
			}
		}
	}
	public void read(Element t, HashMap<String, Attribute> attributeModel) {
		attributes.removeAllElements();
		this.id = SystemModel.getElement(t, "errata_id");
		this.desc = SystemModel.getElement(t, "description");
		this.targetType = SystemModel.getElement(t, "target_type");
		NodeList attributeList = t.getElementsByTagName("attribute");
		for (int j = 0; j < attributeList.getLength(); ++j) {
			String attrId = SystemModel.getElement((Element) attributeList.item(j), "id");
			Attribute aModel = attributeModel.get(attrId);
			if (aModel == null) {
				ServerwizMessageDialog.openError(null, "Error", "Invalid attribute " + attrId + " in Errata: " + id);
				return;
			} else {
				Attribute a = new Attribute(aModel);
				a.value.readInstanceXML((Element) attributeList.item(j));
				attributes.add(a);
			}
		}
	}
	public String getId() { 
		return id;
	}
	public String getDesc() {
		return desc;
	}
	public Vector<Attribute> getAttributes() {
		return attributes;
	}
	public void readInstance() {

	}
}
