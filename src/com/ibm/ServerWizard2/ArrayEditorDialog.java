package com.ibm.ServerWizard2;

import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

public class ArrayEditorDialog extends Dialog {

	private Table table;
	int numColumns=0;
	private String rawData="";
	private TableViewer viewer;
	private Vector<Vector<String>> data;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ArrayEditorDialog(Shell parent, int style) {
		super(parent);
		this.setShellStyle(SWT.APPLICATION_MODAL);
		
	}

	/**
	 * Create contents of the dialog.
	 */
	public String getData() {
		rawData="";
		for (Vector<String> v : data) {
			for (int i=1;i<v.size();i++) {
				rawData=rawData+v.get(i)+",";
			}
		}
		rawData=rawData.substring(0, rawData.length()-1);
		return rawData;
	}
	public void setData(String rawData,String array,String type) {
		
		String d[] = array.split(",");
		if (d.length==1) {
			numColumns=1;
		} else {
			numColumns=Integer.parseInt(d[1]);
		}
		this.rawData = rawData;
		String a[] = rawData.split(",");

		data = new Vector<Vector<String>>();
		for(int row=0;row < a.length/numColumns;row++) {
			Vector<String> v_col = new Vector<String>();
			data.add(v_col);
			v_col.add(String.valueOf(row));
			for (int col=0;col<numColumns;col++) {
				v_col.add(a[col+row*numColumns]);
			}
		}
	}
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		viewer = new TableViewer(container, SWT.MULTI | SWT.H_SCROLL
			      | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final TableViewerColumn columnRow = new TableViewerColumn(viewer, SWT.NONE);
		columnRow.getColumn().setWidth(40);
		columnRow.setLabelProvider(new ArrayLabelProvider(0));
		
		for (int i=1;i<numColumns+1;i++) {
			final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setWidth(100);
			column.getColumn().setText(String.valueOf(i-1));
			column.setLabelProvider(new ArrayLabelProvider(i));
			column.setEditingSupport(new ArrayEditingSupport(viewer,i)); 
		}
		// make lines and header visible
		table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(data); 
				
		GridData gd_table = new GridData(SWT.CENTER, SWT.TOP, false, true, 1, 1);
		gd_table.heightHint = 254;
		gd_table.widthHint = 260;
		table.setLayoutData(gd_table);

		return container;
	}
}
