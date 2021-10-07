package com.ibm.ServerWizard2.model;

import java.io.Writer;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ibm.ServerWizard2.ServerWizard2;


public class Connection {
	public int id = 0;
	public String busType = "";
	public ConnectionEndpoint source;
	public ConnectionEndpoint dest;
	public Boolean cabled=false;
	public Target busTarget;
	public String getName() {
		String seperator = " => ";
		if (cabled) {
			seperator=" =c=> ";
		}
		return source.getName()+seperator+dest.getName();
	}
	public Connection() {
		// TODO Auto-generated constructor stub
	}
	public Connection(Connection other) {
		id = other.id;
		busType = other.busType;
		source = new ConnectionEndpoint();
		dest = new ConnectionEndpoint();
		source.setTargetName(other.source.getTargetName());
		source.setPath(other.source.getPath());
		dest.setTargetName(other.dest.getTargetName());
		dest.setPath(other.dest.getPath());
		cabled = other.cabled;
		busTarget = new Target(other.busTarget);
	}
	public void writeInstanceXML(Writer out) throws Exception {
		out.write("\t<bus>\n");
		out.write("\t\t<bus_id>"+getName()+"</bus_id>\n");
		out.write("\t\t<bus_type>"+busType+"</bus_type>\n");
		String c="no";
		if (cabled) { c="yes"; }
		out.write("\t\t<cable>"+c+"</cable>\n");
		out.write("\t\t<source_path>"+source.getPath()+"</source_path>\n");
		out.write("\t\t<source_target>"+source.getTargetName()+"</source_target>\n");
		out.write("\t\t<dest_path>"+dest.getPath()+"</dest_path>\n");
		out.write("\t\t<dest_target>"+dest.getTargetName()+"</dest_target>\n");

		//write attributes
		for (Map.Entry<String, Attribute> entry : busTarget.getAttributes().entrySet()) {
			Attribute attr = new Attribute(entry.getValue());
			attr.writeBusInstanceXML(out);
		}
		out.write("\t</bus>\n");
	}
	public void readInstanceXML(Element t) throws Exception {
		source = new ConnectionEndpoint();
		dest = new ConnectionEndpoint();
		busType = SystemModel.getElement(t, "bus_type");
		String cable = SystemModel.getElement(t, "cable");
		cabled=false;
		if (cable.equals("yes")) { cabled=true; }
		source.setPath(SystemModel.getElement(t, "source_path"));
		source.setTargetName(SystemModel.getElement(t, "source_target"));
		dest.setPath(SystemModel.getElement(t, "dest_path"));
		dest.setTargetName(SystemModel.getElement(t, "dest_target"));
		
		NodeList attrList = t.getElementsByTagName("bus_attribute");
		for (int j = 0; j < attrList.getLength(); ++j) {
			Element attr = (Element) attrList.item(j);
			if (attr==null) {
				throw new Exception("Problem with bus source="+source.getPath());
			}
			String id = SystemModel.getElement(attr, "id");
			Attribute a = busTarget.getAttributes().get(id);
			if (a==null) {
				ServerWizard2.LOGGER.warning("Attribute: "+id+" is invalid for bus "+source.getPath());
			} else {
				a.value.readInstanceXML(attr);
			}
		}
	}
}
