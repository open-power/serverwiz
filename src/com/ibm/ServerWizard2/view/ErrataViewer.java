package com.ibm.ServerWizard2.view;

import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ibm.ServerWizard2.model.Errata;

public class ErrataViewer extends Dialog {

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	Vector<Errata> errata = new Vector<Errata>();
	private Table table;
	private StyledText styledText;
	
	public ErrataViewer(Shell parentShell, Vector<Errata> errata) {
		super(parentShell);
		this.errata = errata;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));

		table = new Table(container, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setSize(100, 100);

		styledText = new StyledText(container, SWT.BORDER);
		styledText.setFont(SWTResourceManager.getFont("Courier New", 8, SWT.NORMAL));
		
		for (int i=errata.size()-1;i>=0;i--) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(errata.get(i).getId());
			item.setData(errata.get(i));
		}
		
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (table.getSelectionCount() > 0) {
					TableItem item = table.getSelection()[0];
					Errata e = (Errata) item.getData();
					styledText.setText(e.getDetail());
				}
			}
		});
		
		table.addListener (SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem) event.item;
				Errata e = (Errata) item.getData();
				if (item.getChecked()) {
					e.setApplied(true);
				} else {
					e.setApplied(false);
				}
			}
		});
		
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(675, 377);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Errata Viewer");
	}
}
