package com.ibm.ServerWizard2;

import java.io.Writer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Attribute implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public String name = "";
	public AttributeValue value;

	public String inherited = "";
	public String desc = "";
	public Boolean readable = false;
	public Boolean writeable = false;
	public Boolean readonly = false;
	public Persistency persistency = Persistency.NO_PERSISTENCY;
	public Boolean hide = false;
	private Boolean bitmask = false;
	private Boolean global = false;
	
	public enum Persistency {
		NO_PERSISTENCY, VOLATILE_ZEROED, NON_VOLATILE, VOLATILE
	};

	public Attribute() {
	}

	public Attribute(Attribute a) {
		this.name = a.name;
		this.desc = a.desc;
		this.persistency = a.persistency;
		this.readable = a.readable;
		this.writeable = a.writeable;
		this.inherited = a.inherited;
		this.hide = a.hide;
		this.bitmask = a.bitmask;
		this.global = a.global;
		
		if (a.value instanceof AttributeValueComplex) {
			this.value = new AttributeValueComplex((AttributeValueComplex)a.value);
		}
		else if(a.value instanceof AttributeValueSimple) {
			this.value = new AttributeValueSimple((AttributeValueSimple)a.value);
		}
		else if(a.value instanceof AttributeValueNative) {
			this.value = new AttributeValueNative((AttributeValueNative)a.value);
		}
		else {
			
		}
		
	}

	public AttributeValue getValue() {
		return value;
	}
	public boolean isReadable() {
		return readable;
	}
	public boolean isWriteable() {
		return writeable;
	}
	public boolean isHidden() {
		return hide;
	}

	public Boolean isBitmask() {
		return bitmask;
	}
	public boolean isGlobal() {
		return this.global;
	}
	public String toString() {
		String rtn="Attribute: "+name+" = ";
		rtn="Attribute: "+name+" = "+value.toString()+" inherited="+this.inherited;
		return rtn;
	}

	public void setPersistence(String p) {
		if (p.equals("non-volatile")) {
			persistency = Persistency.NON_VOLATILE;
		} else if (p.equals("volatile-zeroed")) {
			persistency = Persistency.VOLATILE_ZEROED;
		} else if (p.equals("volatile")) {
			persistency = Persistency.VOLATILE;
		} else {
			throw new NullPointerException("Invalid Peristence: "+p);
		}
	}
	public String getPersistence() {
		if (persistency == Persistency.NON_VOLATILE) {
			return "non-volatile";
		} else if(persistency == Persistency.VOLATILE_ZEROED) {
			return "volatile-zeroed";
		} else if(persistency == Persistency.NO_PERSISTENCY) {
			return "";
		} else if(persistency == Persistency.VOLATILE) {
			return "volatile";
		} else { return ""; }
		
	}
	public void readModelXML(Element attribute) {
		//name = attribute.getElementsByTagName("id").item(0).getChildNodes().item(0).getNodeValue();
		name = SystemModel.getElement(attribute, "id");
		desc = SystemModel.getElement(attribute,"description");
		
		String p = SystemModel.getElement(attribute,"persistency");
		if (!p.isEmpty()) { setPersistence(p); }
		
		if (SystemModel.isElementDefined(attribute,"bitmask")) {
			bitmask=true;
		}
		if (SystemModel.isElementDefined(attribute,"global")) {
			global=true;
		}

		if (SystemModel.isElementDefined(attribute,"readable")) {
			readable=true;
		}
		if (SystemModel.isElementDefined(attribute,"writeable")) {
			writeable=true;
		}
		if (SystemModel.isElementDefined(attribute,"serverwizHide") || 
				name.equals("MODEL") || name.equals("TYPE") || name.equals("CLASS")) {
			hide=true;
		}
		if (SystemModel.isElementDefined(attribute,"serverwizReadonly")) {
			readonly=true;
		}
		Node simpleType = attribute.getElementsByTagName("simpleType").item(0);
		if (simpleType!=null) { 
			value = new AttributeValueSimple(this);
			value.readonly =  readonly;
			value.readXML((Element)simpleType);
		}
		Node complexType = attribute.getElementsByTagName("complexType").item(0);
		if (complexType!=null) {
			value = new AttributeValueComplex(this);
			value.readonly =  readonly;
			value.readXML((Element)complexType);
		}
		Node nativeType = attribute.getElementsByTagName("nativeType").item(0);
		if (nativeType!=null) {
			value = new AttributeValueNative(this);
			value.readonly =  readonly;
			value.readXML((Element)nativeType);
		}
	}
	public void writeBusInstanceXML(Writer out) throws Exception {
		out.write("\t\t<bus_attribute>\n");
		out.write("\t\t\t<id>"+name+"</id>\n");
		value.writeInstanceXML(out);
		out.write("\t\t</bus_attribute>\n");
	}
	public void writeInstanceXML(Writer out) throws Exception {
		out.write("\t<attribute>\n");
		out.write("\t\t<id>"+name+"</id>\n");
		value.writeInstanceXML(out);
		out.write("\t</attribute>\n");
	}	
}
