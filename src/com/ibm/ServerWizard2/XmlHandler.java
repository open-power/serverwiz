package com.ibm.ServerWizard2;

import java.util.ArrayList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {
	protected boolean validationError = false;
	protected SAXParseException saxParseException = null;
	protected ArrayList<String> warnings = new ArrayList<String>();
	
	protected String detailedErrorString = "";

	public void error(SAXParseException exception) throws SAXException {
		detailedErrorString += "Line:" + exception.getLineNumber() + " , Col:" + exception.getColumnNumber() + ", Error:" + exception.getMessage() + "\n";
		validationError = true;
		saxParseException = exception;
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		detailedErrorString += "Line:" + exception.getLineNumber() + " , Col:" + exception.getColumnNumber() + ", Error:" + exception.getMessage() + "\n";
		validationError = true;
		saxParseException = exception;
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