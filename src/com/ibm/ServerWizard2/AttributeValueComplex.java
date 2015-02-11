package com.ibm.ServerWizard2;

import java.io.Writer;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AttributeValueComplex extends AttributeValue {

	public AttributeValueComplex(Attribute a) {
		super(a);
	}
	public AttributeValueComplex(AttributeValueComplex a) {
		super(a);
	}

	public void readXML(Element value) {
		fields = new Vector<Field>();
		type="complex";
		NodeList fieldList = value.getElementsByTagName("field");
		for (int i = 0; i < fieldList.getLength(); ++i) {
			Field f = new Field();
			f.attributeName = this.attribute.name;
			f.name = SystemModel.getElement((Element) fieldList.item(i), "name");
			f.desc = SystemModel
					.getElement((Element) fieldList.item(i), "description");
			f.type = SystemModel.getElement((Element) fieldList.item(i), "type");
			f.bits = SystemModel.getElement((Element) fieldList.item(i), "bits");
			f.defaultv = SystemModel.getElement((Element) fieldList.item(i), "default");
			f.readonly = this.readonly;
			fields.add(f);
		}
	}
	public void readInstanceXML(Element value) {
		NodeList fieldList = value.getElementsByTagName("field");
		for (int i = 0; i < fieldList.getLength(); ++i) {
			String fid=SystemModel.getElement((Element) fieldList.item(i), "id");
			String v=SystemModel.getElement((Element) fieldList.item(i), "value");
			for(int x=0;x<fields.size();x++) {
				Field f = fields.get(x);
				if (f.name.equals(fid)) {
					f.value=v;
				}
			}
		}
	}
	@Override
	public void writeInstanceXML(Writer out) throws Exception {
		String t="\t\t";
		String r=t+"<default>\n";
		for (int i=0;i<fields.size();i++) {
			Field f = new Field(fields.get(i));
			r=r+t+"\t\t<field><id>"+f.name+"</id><value>"+f.value+"</value></field>\n";
		}
		r=r+t+"</default>\n";
		out.write(r);
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return "complex";
	}

	@Override
	public String toString() {
		String r="COMPLEX:\n";
		for (int i=0;i<this.fields.size();i++) {
			Field f = new Field(this.fields.get(i));
			r=f.name+"="+f.value;
		}
		return r;
	}
	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Boolean isEmpty() {
		return false;
	}
	@Override
	public void setValue(AttributeValue value) {
		fields.clear();
		AttributeValueComplex c = (AttributeValueComplex)value;
		for (int i=0;i<c.fields.size();i++) {
			Field f = new Field(c.fields.get(i));
			fields.add(f);
		}
	}
}
