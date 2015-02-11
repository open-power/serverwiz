package com.ibm.ServerWizard2;


public class Field {
	public String attributeName="";
	public String name="";
	public String desc="";
	public String type="";
	public String bits="";
	public String defaultv="";
	public String value="";
	public Enumerator enumerator = null;
	public String array = "";
	public boolean readonly = false;
		
	public Field() {
	
	}
	public Field(Field f) {
		attributeName = f.attributeName;
		name=f.name;
		desc=f.desc;
		type=f.type;
		bits=f.bits;
		value=f.value;
		defaultv=f.defaultv;
		enumerator=f.enumerator;
		array = f.array;
		readonly = f.readonly;
	}
}
