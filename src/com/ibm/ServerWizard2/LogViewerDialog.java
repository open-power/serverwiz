package com.ibm.ServerWizard2;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.wb.swt.SWTResourceManager;

public class LogViewerDialog extends Dialog {

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	private String data="";
	
	public LogViewerDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
	}
	public void setData(String data) {
		this.data=data;
	}
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		FillLayout fl_container = new FillLayout(SWT.HORIZONTAL);
		fl_container.marginHeight = 20;
		fl_container.marginWidth = 20;
		container.setLayout(fl_container);
		
		StyledText styledText = new StyledText(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		styledText.setFont(SWTResourceManager.getFont("Courier New", 8, SWT.NORMAL));
		styledText.setText(data);
		return container;
	}
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(723, 629);
	}

}
