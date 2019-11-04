package com.ibm.ServerWizard2.view;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.ibm.ServerWizard2.model.Field;

public class FieldFilter extends ViewerFilter {
	
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
		if(field.attributeName.matches(searchString)) {
			return true;
		}
		if(field.name.matches(searchString)) {
			return true;
		}
		if(field.value.matches(searchString)) {
			return true;
		}
		if(field.desc.matches(searchString)) {
			return true;
		}
		if(field.group.matches(searchString)) {
			return true;
		}
		
		return false;
	}

}
