package com.ibm.ServerWizard2.view;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PasswordPrompt extends Dialog {
	private Text text;
	public char passwd[];
	private String label;

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public PasswordPrompt(Shell parentShell,String label) {
		super(parentShell);
		this.label = label;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		
		Label lblEnterPasswordFor = new Label(container, SWT.NONE);
		GridData gd_lblEnterPasswordFor = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblEnterPasswordFor.widthHint = 352;
		gd_lblEnterPasswordFor.heightHint = 21;
		lblEnterPasswordFor.setLayoutData(gd_lblEnterPasswordFor);
		lblEnterPasswordFor.setText("Enter Password For:");
		
		Label lblRepository = new Label(container, SWT.NONE);
		GridData gd_lblRepository = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblRepository.widthHint = 419;
		gd_lblRepository.heightHint = 20;
		lblRepository.setLayoutData(gd_lblRepository);
		lblRepository.setText(this.label);
		
		Composite composite_1 = new Composite(container, SWT.NONE);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_composite_1.widthHint = 418;
		gd_composite_1.heightHint = 38;
		composite_1.setLayoutData(gd_composite_1);
		
		text = new Text(composite_1, SWT.BORDER | SWT.PASSWORD);
		text.setBounds(10, 10, 115, 21);
		
		Button btnOk = new Button(composite_1, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				passwd = text.getTextChars();
				close();
			}
		});
		btnOk.setBounds(143, 6, 75, 25);
		btnOk.setText("Ok");

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(447, 174);
	}
}
