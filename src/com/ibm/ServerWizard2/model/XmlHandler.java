package com.ibm.ServerWizard2.model;

import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {
	
	
	private ArrayList<String> warnings = new ArrayList<String>();
	
	private String detailedErrorString = "";

	public void error(SAXParseException exception) throws SAXException {
		detailedErrorString += "Line:" + exception.getLineNumber() + " , Col:" + exception.getColumnNumber() + ", Error:" + exception.getMessage() + "\n";
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		detailedErrorString += "Line:" + exception.getLineNumber() + " , Col:" + exception.getColumnNumber() + ", Error:" + exception.getMessage() + "\n";
	}

	public void warning(SAXParseException exception) throws SAXException {
		warnings.add(exception.getMessage());
	}
	
	public String getDetailedErrorString(){
		return detailedErrorString;
	}
	
	public ArrayList<String> getWarnings(){
		return warnings;
	}
}