package com.ibm.ServerWizard2.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.ibm.ServerWizard2.model.Field;

public class AttributeTableFilter extends ViewerFilter {
	
	private String searchString;
	
	void setSearchText(String s) {
		this.searchString = ".*" + s + ".*";
	}

	@Override
	public boolean select(Viewer viewer, 
			Object parentElement,
			Object element) {
		// TODO Auto-generated method stub
		if(searchString == null || searchString.isEmpty()) {
			return true;
		}
		
		Field field = (Field) element;
		if(field.attributeName.toLowerCase().matches(searchString)) {
			return true;
		}
		if(field.name.toLowerCase().matches(searchString)) {
			return true;
		}
		if(field.value.toLowerCase().matches(searchString)) {
			return true;
		}
		if(field.desc.toLowerCase().matches(searchString)) {
			return true;
		}
		if(field.group.toLowerCase().matches(searchString)) {
			return true;
		}
		
		return false;
	}

}
