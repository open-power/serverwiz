package com.ibm.ServerWizard2.view;

import java.util.Vector;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;

import com.ibm.ServerWizard2.model.Field;
import com.ibm.ServerWizard2.utility.ServerwizMessageDialog;

public class AttributeEditingSupport extends EditingSupport {
	private CellEditor editor;
	private TableViewer viewer;
	private Boolean ignoreReadonly = false;

	public AttributeEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer=viewer;
	}

	public void setIgnoreReadonly() {
		ignoreReadonly = true;
	}
	@Override
	protected boolean canEdit(Object obj) {
		Field f = (Field)obj;
		if (this.ignoreReadonly == true) { return true; }
		return !f.readonly;
	}

	@Override
	protected CellEditor getCellEditor(Object obj) {
		Field f = (Field) obj;
		if (f.type.equals("enumeration")) {
			ComboBoxViewerCellEditor e = new ComboBoxViewerCellEditor(viewer.getTable(),SWT.READ_ONLY);
			e.setContentProvider(new ArrayContentProvider());
			e.setInput(f.enumerator.enumList);
			this.editor=e;
		} else if (!f.array.isEmpty()) {
			this.editor = new ArrayDialogCellEditor(viewer.getTable(),f);
		} else if (f.type.equals("boolean")) {
			Vector<String> tf = new Vector<String>();
			ComboBoxViewerCellEditor e = new ComboBoxViewerCellEditor(viewer.getTable(),SWT.READ_ONLY);
			e.setContentProvider(new ArrayContentProvider());
			tf.add("true");
			tf.add("false");
			e.setInput(tf);
			this.editor=e;
		} else {
			this.editor = new TextCellEditor(viewer.getTable());
			AttributeValidator attrValidator = new AttributeValidator(f);
			this.editor.setValidator(attrValidator);
			this.editor.addListener(new ICellEditorListener() {

				public void applyEditorValue() {
				}

				public void cancelEditor() {
					// TODO Auto-generated method stub

				}

				public void editorValueChanged(boolean arg0, boolean arg1) {
					if (!arg1) {
						ServerwizMessageDialog.openError(null, "Invalid format", editor.getErrorMessage());
					}
				}

			});
		}
		return editor;
	}

	@Override
	protected Object getValue(Object obj) {
		Field f = (Field) obj;
		return f.value;
	}

	@Override
	protected void setValue(Object obj, Object value) {
		Field f = (Field) obj;
		if (f.value==null || !f.value.equals((String)value)) { 
			f.value = (String) value;
			this.getViewer().update(obj, null);
			this.viewer.getTable().notifyListeners(SWT.CHANGED, null);
		}
	}
}
