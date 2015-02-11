package com.ibm.ServerWizard2;

import java.util.Vector;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class ArrayLabelProvider extends ColumnLabelProvider {
	public int column = 0;
	public ArrayLabelProvider(int column) {
		super();
		this.column=column;
	}
	@Override
	public String getText(Object element) {
		 @SuppressWarnings("unchecked")
		Vector<String> v = (Vector<String>)element;
		return v.get(this.column);
	}
	
}
