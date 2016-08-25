package com.ibm.ServerWizard2.model;

public class ConnectionEndpoint {
	//private Target target;
	private String path="";
	private String targetName="";
	
	//public Target getTarget() {
	//	return target;
	//}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	//public void setTarget(Target target) {
	//	this.target = target;
	//}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getName() {
		return path+targetName;
	}
}
