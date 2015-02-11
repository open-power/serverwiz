package com.ibm.ServerWizard2;

import java.io.Writer;
import java.util.Vector;

import org.w3c.dom.Element;

public abstract class AttributeValue {
	
	protected String type="";
	protected Vector<Field> fields;
	protected Attribute attribute = null;
	protected boolean readonly = true;
	
	public abstract void readXML(Element value);
	public abstract void readInstanceXML(Element value);
	public abstract void writeInstanceXML(Writer out) throws Exception;
	public abstract String getValue();
	public abstract void setValue(AttributeValue value);
	public abstract void setValue(String value);
	public abstract String toString();
	public abstract Boolean isEmpty();
	public void setEnumerator(Enumerator enumerator) {
		for (Field f : fields) {
			f.enumerator=enumerator;
		}
	}

	public AttributeValue(Attribute attribute) {
		fields = new Vector<Field>();
		this.attribute=attribute;
	}
	public AttributeValue(AttributeValue a) {
		fields = new Vector<Field>();	
		for (Field f : a.fields) {
			Field f2 = new Field(f);
			fields.add(f2);
		}
		type = a.type;
		attribute = a.attribute;
		readonly = a.readonly;
	}
	public String getType() {
		return type;
	}
	public Vector<Field> getFields() {
		return fields;
	}
}
