package com.ibm.ServerWizard2;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ArrayDialogCellEditor extends DialogCellEditor {
	Field field;
	
	public ArrayDialogCellEditor(Composite parent,Field field) {
		super(parent);
		this.field=new Field(field);
	}
	
	@Override
	protected Object doGetValue() {
		return field.value;
	}
	
	@Override
	protected void doSetValue(Object value) {
		field.value=(String)value;
	}
	
	@Override
	protected Object openDialogBox(Control arg0) {
		ArrayEditorDialog dlg = new ArrayEditorDialog(arg0.getShell(),SWT.NONE);
		dlg.setData(field.value, field.array, field.type);
		if (dlg.open()==0) {
			this.markDirty();
			 return dlg.getData();
		}
		return null;
	}
}
