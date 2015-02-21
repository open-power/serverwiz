package com.ibm.ServerWizard2;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class AttributeValidator implements ICellEditorValidator {
	Field f;
	public AttributeValidator(Field f) {
		this.f=f;
	}

	public String isValid(Object arg0) {
		String s = (String)arg0;
		String rtn=null;
		if (f.type.equals("uint8_t")) {
			if (!this.isValidByte(s)) {
				rtn="Invalid number format for uint8_t";
			}
		} else if (f.type.equals("uint32_t") || f.type.equals("uint16_t")) {
			if (!this.isValidInt(s)) {
				rtn="Invalid number format for uint16_t or uint32_t";
			}
		}
		return rtn;
	}
	private boolean isValidByte(String s) {
/*		try {
			Byte.decode(s);
		} catch (Exception e) {
			return false;
		}*/
		return true;
	}
	private boolean isValidInt(String s) {
		try {
			Integer.decode(s);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
