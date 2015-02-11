package com.ibm.ServerWizard2;

import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class ArrayEditingSupport extends EditingSupport {

    private final CellEditor editor;
    private int column;

	public ArrayEditingSupport(TableViewer viewer,int column) {
		super(viewer);
		this.editor = new TextCellEditor(viewer.getTable());
		this.column=column; 
	}

	@Override
	protected boolean canEdit(Object arg0) {
		return true;
	}

	@Override
	protected CellEditor getCellEditor(Object arg0) {
		return editor;
	}

	@Override
	protected Object getValue(Object arg0) {
		@SuppressWarnings("unchecked")
		Vector<String> v = (Vector<String>)arg0;
		return v.get(column);
	}

	@Override
	protected void setValue(Object arg0, Object arg1) {
		@SuppressWarnings("unchecked")
		Vector<String> v = (Vector<String>)arg0;
		v.set(column, (String)arg1);
		this.getViewer().update(arg0, null);
	}
}
