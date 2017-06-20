package com.ibm.ServerWizard2.model;

import java.io.Writer;

import org.w3c.dom.Element;

public class AttributeValueNative extends AttributeValue {
	private Field field;

	public AttributeValueNative(Attribute a) {
		super(a);
		field = new Field();
		field.attributeName=a.name;
		field.desc=a.desc;
		field.group=a.group;
		fields.add(field);
	}
	public AttributeValueNative(AttributeValueNative a) {
		super(a);
		field=fields.get(0);
	}
	public void readXML(Element e) {
		field.value = SystemModel.getElement(e, "default");
		field.name = SystemModel.getElement(e, "name");
		field.readonly = this.readonly;
	}

	public void readInstanceXML(Element e) {
		String v = SystemModel.getElement(e, "default");
		if (!v.isEmpty() || field.value.isEmpty()) {
			field.value = v;
		}
	}

	@Override
	public void writeInstanceXML(Writer out) throws Exception {
		out.write("\t\t<default>" + field.value + "</default>\n");
	}

	@Override
	public String getValue() {
		return field.value;
	}

	@Override
	public String toString() {
		return "default(" + field.name + ")";
	}

	@Override
	public void setValue(String value) {
		field.value = value;
	}

	@Override
	public Boolean isEmpty() {
		return field.value.isEmpty();
	}

	@Override
	public void setValue(AttributeValue value) {
		AttributeValueNative n = (AttributeValueNative) value;
		field.value = n.field.value;
		field.name = n.field.name;
	}

	@Override
	public String compare(Object o) {
		String cmp = "";
		AttributeValueNative s = (AttributeValueNative) o;
		if (!field.value.equals(s.field.value)) {
			cmp = field.attributeName +" : "+field.value + " != "+s.field.value;
		}
		return cmp;
	}
}
