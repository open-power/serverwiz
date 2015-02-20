package com.ibm.ServerWizard2;

import java.io.Writer;

import org.w3c.dom.Element;

public class AttributeValueSimple extends AttributeValue {

	private String array = "";
	private Field field;

	public AttributeValueSimple(Attribute a) {
		super(a);
		field = new Field();
		field.attributeName = a.name;
		field.desc=a.desc;
		fields.add(field);
	}
	public AttributeValueSimple(AttributeValueSimple a) {
		super(a);
		field=fields.get(0);
	}

	@Override
	public void readXML(Element e) {
		// value = e.getElementsByTagName("default").;
		Element a = (Element) e.getElementsByTagName("uint8_t").item(0);
		if (a != null) {
			type = "uint8_t";
			field.value = SystemModel.getElement(a, "default");
			field.type=type;
		}
		a = (Element) e.getElementsByTagName("uint16_t").item(0);
		if (a != null) {
			type = "uint16_t";
			field.value = SystemModel.getElement(a, "default");
			field.type=type;
		}
		a = (Element) e.getElementsByTagName("uint32_t").item(0);
		if (a != null) {
			type = "uint32_t";
			field.value = SystemModel.getElement(a, "default");
			field.type=type;
		}
		a = (Element) e.getElementsByTagName("string").item(0);
		if (a != null) {
			type = "string";
			field.value = SystemModel.getElement(a, "default");
			field.type=type;
		}
		a = (Element) e.getElementsByTagName("enumeration").item(0);
		if (a != null) {
			type = "enumeration";
			field.value = SystemModel.getElement(a, "default");
			field.name = SystemModel.getElement(a, "id");
			field.type=type;
		}
		array = SystemModel.getElement(e, "array");
		field.array = array;
		field.readonly = this.readonly;
	}

	public void readInstanceXML(Element e) {
		field.value = SystemModel.getElement(e, "default");
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
		// TODO Auto-generated method stub
		return field.value + " (" + type + ")";
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
		AttributeValueSimple n = (AttributeValueSimple) value;
		field.value = n.field.value;
		field.name = n.field.name;
	}
}
