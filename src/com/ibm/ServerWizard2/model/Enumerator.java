package com.ibm.ServerWizard2.model;

import java.util.HashMap;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Enumerator {
	public String id="";
	public String desc="";
	public String group="";
	public HashMap<String,String> enumValues = new HashMap<String,String>();
	public Vector<String> enumList = new Vector<String>();
	
	
	public void addEnum(String name,String value) {
		enumValues.put(name, value);
		enumList.add(name);
	}
	public void readXML(Element e) {
		id = SystemModel.getElement(e, "id");
		desc = SystemModel.getElement(e,"description");
		group = SystemModel.getElement(e,"group");
	    NodeList enumList = e.getElementsByTagName("enumerator");
	    for (int i = 0; i < enumList.getLength(); ++i) {
	    	Element en=(Element)enumList.item(i);
	    	String name=SystemModel.getElement(en, "name");
	    	String value=SystemModel.getElement(en, "value");
	    	addEnum(name,value);
	    }
	}
	public Integer getEnumInt(String e) {
		return Integer.decode(enumValues.get(e));
	}
	public String getEnumStr(String e) {
		return enumValues.get(e);
	}

}
