package com.ibm.ServerWizard2.view;

import java.util.Vector;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ibm.ServerWizard2.ServerWizard2;
import com.ibm.ServerWizard2.controller.TargetWizardController;
import com.ibm.ServerWizard2.model.Connection;
import com.ibm.ServerWizard2.model.ConnectionEndpoint;
import com.ibm.ServerWizard2.model.Field;
import com.ibm.ServerWizard2.model.Target;
import com.ibm.ServerWizard2.utility.GithubFile;



public class MainDialog extends Dialog {
	private TableViewer viewer;
	private Tree tree;
	private TreeColumn columnName;
	private Text txtInstanceName;
	private Combo combo;
	private Menu popupMenu;
	private Composite container;
	private TreeItem selectedEndpoint;
	private String currentPath;

	private Target targetForConnections;
	private ConnectionEndpoint source;
	private ConnectionEndpoint dest;

	private TargetWizardController controller;

	// Buttons
	private Button btnAddTarget;
	private Button btnCopyInstance;
	private Button btnDefaults;
	private Button btnDeleteTarget;
	private Button btnSave;
	private Button btnOpen;
	private Button btnClone;
	private Button btnOpenLib;
	private Button btnDeleteConnection;
	private Button btnSaveAs;

	// document state
	private Boolean dirty = false;
	public String mrwFilename = "";

	private Button btnRunChecks;
	private SashForm sashForm;
	private SashForm sashForm_1;

	private Composite compositeBus;
	private Label lblInstanceType;
	private Composite compositeInstance;
	private Composite composite;
	private Composite buttonRow1;

	private Vector<Field> attributes;
	private Combo cmbBusses;
	private Label lblChooseBus;
	private Label lblSelectedCard;
	private Boolean busMode = false;
	private TabFolder tabFolder;
	private TabItem tbtmAddInstances;
	private TabItem tbtmAddBusses;
	private Combo cmbCards;
	private Boolean targetFound = false;
	private List listBusses;
	private Label lblBusDirections;
	private Label lblInstanceDirections;
	private Composite compositeDir;
	private Button btnHideBusses;
	private Button btnShowHidden;
	private Combo showFilter;
	
	private AttributeEditingSupport attributeEditor;
	private Label label;
	private Label label_1;
	private Composite composite_1;
	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public MainDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.BORDER | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ServerWiz2");
	}

	public void setController(TargetWizardController t) {
		controller = t;
	}

	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);
		container.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		GridLayout gl_container = new GridLayout(1, false);
		gl_container.verticalSpacing = 0;
		container.setLayout(gl_container);

		composite = new Composite(container, SWT.NONE);
		
		RowLayout rl_composite = new RowLayout(SWT.HORIZONTAL);
		rl_composite.spacing = 20;
		rl_composite.wrap = false;
		rl_composite.fill = true;
		composite.setLayout(rl_composite);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_composite.widthHint = 918;
		gd_composite.heightHint = 154;
		composite.setLayoutData(gd_composite);

		sashForm_1 = new SashForm(container, SWT.BORDER | SWT.VERTICAL);
		GridData gd_sashForm_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_sashForm_1.heightHint = 375;
		gd_sashForm_1.widthHint = 712;
		sashForm_1.setLayoutData(gd_sashForm_1);
		
		buttonRow1 = new Composite(container, SWT.NONE);
		GridData gd_buttonRow1 = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_buttonRow1.widthHint = 866;
		buttonRow1.setLayoutData(gd_buttonRow1);
		GridLayout rl_buttonRow1 = new GridLayout(18, false);
		buttonRow1.setLayout(rl_buttonRow1);

		this.createButtonsForButtonBar2(buttonRow1);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		new Label(buttonRow1, SWT.NONE);
		
		sashForm = new SashForm(sashForm_1, SWT.NONE);

		// Target Instances View
		tree = new Tree(sashForm, SWT.BORDER | SWT.VIRTUAL);
		tree.setHeaderVisible(true);
		tree.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		columnName = new TreeColumn(tree, 0);
		columnName.setText("Instances");
		columnName
				.setToolTipText("To add a new instance, choose parent instance.  A list of child instances will appear in Instance Type combo.\r\n"
						+ "Select and Instance type.  You can optionally enter a custom name.  Then click 'Add Instance' button.");

		columnName.setResizable(true);
		
		composite_1 = new Composite(sashForm_1, SWT.NONE);
		
		showFilter = new Combo(composite_1, SWT.READ_ONLY);
		showFilter.setFont(SWTResourceManager.getFont("Arial", 9, SWT.READ_ONLY));
		showFilter.setBounds(118, 0, 336, 23);
		
		Label lblAttributeFilter = new Label(composite_1, SWT.NONE);
		lblAttributeFilter.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblAttributeFilter.setBounds(15, 3, 97, 15);
		lblAttributeFilter.setText("Attribute Filter:");

		// Create attribute table
		viewer = new TableViewer(sashForm_1, SWT.VIRTUAL | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);

		this.createAttributeTable();

		// //////////////////////////////////////////////////////////
		// Tab folders
		tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		tabFolder.setLayoutData(new RowData(SWT.DEFAULT, 119));

		tbtmAddInstances = new TabItem(tabFolder, SWT.NONE);
		tbtmAddInstances.setText("Instances");

		// //////////////////////////////////////
		// Add instances tab
		compositeInstance = new Composite(tabFolder, SWT.BORDER);
		tbtmAddInstances.setControl(compositeInstance);
		compositeInstance.setLayout(new GridLayout(3, false));

		lblInstanceType = new Label(compositeInstance, SWT.NONE);
		lblInstanceType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInstanceType.setText("Instance Type:");
		lblInstanceType.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));

		combo = new Combo(compositeInstance, SWT.READ_ONLY);
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 167;
		combo.setLayoutData(gd_combo);
		combo.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));

		btnAddTarget = new Button(compositeInstance, SWT.NONE);
		btnAddTarget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnAddTarget.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnAddTarget.setText("Add Instance");
		btnAddTarget.setEnabled(false);

		Label lblName = new Label(compositeInstance, SWT.NONE);
		lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblName.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblName.setText("Custom Name:");

		txtInstanceName = new Text(compositeInstance, SWT.BORDER);
		GridData gd_txtInstanceName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtInstanceName.widthHint = 175;
		txtInstanceName.setLayoutData(gd_txtInstanceName);
		txtInstanceName.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));

		btnDeleteTarget = new Button(compositeInstance, SWT.NONE);
		btnDeleteTarget.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnDeleteTarget.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnDeleteTarget.setText("Delete Instance");
		
		btnShowHidden = new Button(compositeInstance, SWT.CHECK);
		btnShowHidden.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		GridData gd_btnShowHidden = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnShowHidden.heightHint = 20;
		btnShowHidden.setLayoutData(gd_btnShowHidden);
		btnShowHidden.setText(" Show Hidden");
		
		btnCopyInstance = new Button(compositeInstance, SWT.NONE);
		btnCopyInstance.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnCopyInstance.setText("Copy Node or Connector");
		btnCopyInstance.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnCopyInstance.setEnabled(false);

		btnDefaults = new Button(compositeInstance, SWT.NONE);
		btnDefaults.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		btnDefaults.setText("Restore Defaults");
		btnDefaults.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnDefaults.setEnabled(false);

		// ////////////////////////////////////////////////////
		// Add Busses Tab

		tbtmAddBusses = new TabItem(tabFolder, SWT.NONE);
		tbtmAddBusses.setText("Busses");

		compositeBus = new Composite(tabFolder, SWT.BORDER);
		tbtmAddBusses.setControl(compositeBus);
		compositeBus.setLayout(new GridLayout(2, false));

		lblChooseBus = new Label(compositeBus, SWT.NONE);
		lblChooseBus.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblChooseBus.setAlignment(SWT.RIGHT);
		GridData gd_lblChooseBus = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblChooseBus.widthHint = 88;
		lblChooseBus.setLayoutData(gd_lblChooseBus);
		lblChooseBus.setText("Select Bus:");

		cmbBusses = new Combo(compositeBus, SWT.READ_ONLY);
		cmbBusses.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		cmbBusses.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		cmbBusses.add("NONE");
		cmbBusses.setData(null);
		cmbBusses.select(0);

		lblSelectedCard = new Label(compositeBus, SWT.NONE);
		lblSelectedCard.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblSelectedCard.setAlignment(SWT.RIGHT);
		GridData gd_lblSelectedCard = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblSelectedCard.widthHint = 93;
		lblSelectedCard.setLayoutData(gd_lblSelectedCard);
		lblSelectedCard.setText("Select Card:");

		cmbCards = new Combo(compositeBus, SWT.READ_ONLY);
		cmbCards.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		cmbCards.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		btnDeleteConnection = new Button(compositeBus, SWT.NONE);
		btnDeleteConnection.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnDeleteConnection.setText("Delete Connection");
		btnDeleteConnection.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));

		btnHideBusses = new Button(compositeBus, SWT.CHECK);
		btnHideBusses.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnHideBusses.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnHideBusses.setText("Show only busses of selected type");
		btnHideBusses.setSelection(true);

		StackLayout stackLayout = new StackLayout();
		compositeDir = new Composite(composite, SWT.NONE);
		compositeDir.setLayout(stackLayout);
		compositeDir.setLayoutData(new RowData(382, SWT.DEFAULT));

		lblInstanceDirections = new Label(compositeDir, SWT.NONE);
		lblInstanceDirections.setFont(SWTResourceManager.getFont("Arial", 8, SWT.NORMAL));
		lblInstanceDirections.setText("Select 'chip' to create a new part or 'sys-' to create a system\r\n"
				+ "1. Select parent instance in Instance Tree\r\n"
				+ "2. Select new instance type in dropdown\r\n"
				+ "3. (Optional) Enter custom name\r\n" + "4. Click \"Add Instance\"");
		lblInstanceDirections.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		stackLayout.topControl = this.lblInstanceDirections;

		lblBusDirections = new Label(compositeDir, SWT.NONE);
		lblBusDirections.setFont(SWTResourceManager.getFont("Arial", 8, SWT.NORMAL));
		lblBusDirections.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lblBusDirections
				.setText("Steps for adding a new connection:\r\n"
						+ "1. Select a bus type from dropdown\r\n"
						+ "2. Select the card on which the bus is on from dropdown\r\n"
						+ "3. Navigate to connection source in Instances Tree view on left\r\n"
						+ "4. Right-click on source and select \"Set Source\"\r\n"
						+ "5. Navigate to connection destination\r\n6. Right-click on destination and select \"Add Connection\"");

		listBusses = new List(sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		listBusses.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));

		this.addEvents();
		this.setDirtyState(false);

		// load file if passed on command line
		if (!mrwFilename.isEmpty()) {
			controller.readXML(mrwFilename);
			setFilename(mrwFilename);
		}
		for (Target t : controller.getBusTypes()) {
			cmbBusses.add(t.getType());
			cmbBusses.setData(t.getType(), t);
		}
		attributes = new Vector<Field>();
		this.initInstanceMode();
		sashForm.setWeights(new int[] { 1, 1 });
		columnName.pack();
		sashForm_1.setWeights(new int[] {302, 37, 171});

		showFilter.removeAll();
		showFilter.add("");
		for (String a : controller.getAttributeFilters().keySet()) {
			showFilter.add(a);
		}

		
		return container;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		parent.setEnabled(false);
		 GridLayout layout = (GridLayout)parent.getLayout();
		  layout.marginHeight = 0;
	}
	
	protected void createButtonsForButtonBar2(Composite row1) {
		row1.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		
		Button btnNew = createButton(row1, IDialogConstants.NO_ID, "New", false);
		GridData gd_btnNew = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNew.widthHint = 70;
		btnNew.setLayoutData(gd_btnNew);
		btnNew.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dirty) {
					if (!MessageDialog.openConfirm(null, "Save Resource", mrwFilename
							+ "has been modified. Ignore changes?")) {
						return;
					}
					ServerWizard2.LOGGER.info("Discarding changes");
				}
				try {
					controller.initModel();
					setFilename("");
					initInstanceMode();
					setDirtyState(false);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		btnOpen = createButton(row1, IDialogConstants.NO_ID, "Open...", false);
		GridData gd_btnOpen = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnOpen.widthHint = 70;
		btnOpen.setLayoutData(gd_btnOpen);
		btnOpen.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnOpen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (dirty) {
					if (!MessageDialog.openConfirm(null, "Save Resource", mrwFilename
							+ "has been modified. Ignore changes?")) {
						return;
					}
					ServerWizard2.LOGGER.info("Discarding changes");
				}
				Button b = (Button) e.getSource();
				FileDialog fdlg = new FileDialog(b.getShell(), SWT.OPEN);
				String ext[] = { "*.xml" };
				fdlg.setFilterExtensions(ext);
				String filename = fdlg.open();
				if (filename == null) {
					return;
				}
				Boolean dirty = controller.readXML(filename);
				setFilename(filename);
				initInstanceMode();
				setDirtyState(dirty);
			}
		});
		btnOpen.setToolTipText("Loads XML from file");

		btnSave = createButton(row1, IDialogConstants.NO_ID, "Save", false);
		GridData gd_btnSave = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnSave.widthHint = 70;
		btnSave.setLayoutData(gd_btnSave);
		btnSave.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = mrwFilename;
				if (mrwFilename.isEmpty()) {
					Button b = (Button) e.getSource();
					FileDialog fdlg = new FileDialog(b.getShell(), SWT.SAVE);
					String ext[] = { "*.xml" };
					fdlg.setFilterExtensions(ext);
					fdlg.setOverwrite(true);
					filename = fdlg.open();
					if (filename == null) {
						return;
					}
				}
				controller.writeXML(filename);
				setFilename(filename);
				setDirtyState(false);
			}
		});
		btnSave.setText("Save");

		btnSaveAs = createButton(row1, IDialogConstants.NO_ID, "Save As...", false);
		GridData gd_btnSaveAs = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnSaveAs.widthHint = 70;
		btnSaveAs.setLayoutData(gd_btnSaveAs);
		btnSaveAs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				FileDialog fdlg = new FileDialog(b.getShell(), SWT.SAVE);
				String ext[] = { "*.xml" };
				fdlg.setFilterExtensions(ext);
				fdlg.setOverwrite(true);
				String filename = fdlg.open();
				if (filename == null) {
					return;
				}
				controller.writeXML(filename);
				setFilename(filename);
				setDirtyState(false);
			}
		});

		btnSaveAs.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnSaveAs.setEnabled(true);
		
		label = new Label(buttonRow1, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_sep = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sep.heightHint = 30;
		gd_sep.widthHint = 30;
		label.setLayoutData(gd_sep);

		btnClone = createButton(row1, IDialogConstants.NO_ID, "Manage Library", false);
		GridData gd_btnClone = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnClone.widthHint = 110;

		btnClone.setLayoutData(gd_btnClone);
		btnClone.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnClone.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GitDialog dlg = new GitDialog(btnClone.getShell());
				dlg.open();
			}
		});
		btnClone.setToolTipText("Retrieves Library from github");
		
		
		btnOpenLib = createButton(row1, IDialogConstants.NO_ID, "Open Lib", false);
		GridData gd_btnOpenLib = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnOpenLib.widthHint = 90;

		btnOpenLib.setLayoutData(gd_btnOpenLib);
		btnOpenLib.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnOpenLib.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				DirectoryDialog fdlg = new DirectoryDialog(b.getShell(), SWT.OPEN);
				fdlg.setFilterPath(ServerWizard2.getWorkingDir());
				String libPath = fdlg.open();
				if (libPath == null) {
					return;
				}
				controller.loadLibrary(libPath);
			}
		});
		btnOpenLib.setToolTipText("Loads External Library");
		
		
		btnRunChecks = createButton(row1, IDialogConstants.NO_ID, "Export HTML", false);
		GridData gd_btnRunChecks = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnRunChecks.widthHint = 90;
		btnRunChecks.setLayoutData(gd_btnRunChecks);
		btnRunChecks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String tempFile = System.getProperty("java.io.tmpdir")
						+ System.getProperty("file.separator") + "~temp.xml";
				controller.writeXML(tempFile);
				String htmlFilename = mrwFilename;
				htmlFilename = htmlFilename.replace(".xml", "") + ".html";
				controller.runChecks(tempFile,htmlFilename);
			}
		});
		btnRunChecks.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));

		Button btnForceUpdate = createButton(row1, IDialogConstants.NO_ID, "Force Update", false);
		GridData gd_btnForceUpdate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnForceUpdate.widthHint = 90;
		btnForceUpdate.setLayoutData(gd_btnForceUpdate);
		btnForceUpdate.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnForceUpdate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GithubFile.removeUpdateFile(true);
			}
		});
		
		label_1 = new Label(buttonRow1, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_sep2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sep2.heightHint = 30;
		gd_sep2.widthHint = 30;
		label_1.setLayoutData(gd_sep2);

		Button btnExit = createButton(row1, IDialogConstants.CLOSE_ID, "Exit", false);
		GridData gd_btnExit = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnExit.widthHint = 80;
		btnExit.setLayoutData(gd_btnExit);
		btnExit.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				b.getShell().close();
			}
		});
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(933, 796);
	}

	// ////////////////////////////////////////////////////
	// Utility helpers
	private Target getSelectedTarget() {
		if (tree.getSelectionCount() > 0) {
			return (Target) tree.getSelection()[0].getData();
		}
		return null;
	}

	private void initBusMode() {
		busMode = true;
		this.lblBusDirections.setEnabled(true);
		this.lblBusDirections.setVisible(true);
		this.lblInstanceDirections.setVisible(false);
		this.lblInstanceDirections.setEnabled(false);

		// update card combo
		cmbCards.removeAll();
		this.targetForConnections = null;
		for (Target target : controller.getConnectionCapableTargets()) {
			cmbCards.add(target.getName());
			cmbCards.setData(target.getName(), target);
		}
		if (cmbCards.getItemCount() > 0) {
			cmbCards.select(-1);
		}
		for (TreeItem item : tree.getItems()) {
			Target target = (Target) item.getData();
			controller.hideBusses(target);
		}
		this.source = null;
		this.dest = null;
		this.selectedEndpoint = null;
		refreshInstanceTree();
		refreshConnections();
		attributes.clear();
		viewer.setInput(attributes);
		viewer.refresh();
		this.updateView();
	}

	private void initInstanceMode() {
		tabFolder.setSelection(0);
		busMode = false;
		this.lblInstanceDirections.setEnabled(true);
		this.lblInstanceDirections.setVisible(true);

		this.lblBusDirections.setEnabled(false);
		this.lblBusDirections.setVisible(false);

		this.targetForConnections = null;
		this.refreshInstanceTree();
		this.listBusses.removeAll();
		refreshConnections();
		attributes.clear();
		viewer.setInput(attributes);
		viewer.refresh();
		this.updateView();
	}

	private void updateChildCombo(Target targetInstance) {
		btnAddTarget.setEnabled(false);
		Vector<Target> v = controller.getChildTargets(targetInstance);
		combo.removeAll();
		if (v != null) {
			for (Target target : v) {
				combo.add(target.getType());
				combo.setData(target.getType(), target);
			}
			if (combo.getItemCount() > 0) {
				combo.select(0);
			}
			btnAddTarget.setEnabled(true);
		}
	}

	/*
	 * Updates button enabled states based on if target is selected
	 * Also updates attribute table based on selected target
	 */
	private void updateView() {
		Target targetInstance = getSelectedTarget();
		if (targetInstance == null) {
			btnAddTarget.setEnabled(false);
			btnDeleteTarget.setEnabled(false);
			btnCopyInstance.setEnabled(false);
			btnDefaults.setEnabled(false);
			updateChildCombo(null);
			return;
		}
		updatePopupMenu(targetInstance);
		updateChildCombo(targetInstance);

		//A target is selected so show the associated attributes
		TreeItem item = tree.getSelection()[0];
		ConnectionEndpoint ep = this.getEndpoint(item, null);
		int s = showFilter.getSelectionIndex();
		String show = "";
		if (s>-1) {
			show = showFilter.getItem(s);
		}
		attributes = controller.getAttributesAndGlobals(targetInstance, "/"+ep.getName(),show);
		viewer.setInput(attributes);
		viewer.refresh();
		
		if (targetInstance.isSystem()) {
			btnDeleteTarget.setEnabled(false);
		} else {
			btnDeleteTarget.setEnabled(true);
		}
		if (targetInstance.isNode() || targetInstance.isConnector()) {
			btnCopyInstance.setEnabled(true);
		} else {
			btnCopyInstance.setEnabled(false);
		}
		btnDefaults.setEnabled(true);
	}

	/*
	 * Creates right-click popup menu for adding connections
	 * 
	 */
	private void updatePopupMenu(Target selectedTarget) {
		if (selectedTarget == null || tree.getSelectionCount()==0) {
			return;
		}
		if (popupMenu != null) {
			popupMenu.dispose();
		}

		popupMenu = new Menu(tree);

		if (busMode) {
		if (cmbBusses.getSelectionIndex() > 0) {
			if (targetForConnections != null && selectedTarget.getAttribute("CLASS").equals("UNIT")) {
				if (selectedTarget.isOutput()) {
					MenuItem srcItem = new MenuItem(popupMenu, SWT.NONE);
					srcItem.setText("Set Source");
					srcItem.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							source = getEndpoint(true);
						}
					});
				}
			}
			if (source != null && selectedTarget.isInput()) {
				MenuItem connItem = new MenuItem(popupMenu, SWT.NONE);
				connItem.setText("Add Connection");
				connItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						dest = getEndpoint(false);
						addConnection(false);
					}
				});
				MenuItem cableItem = new MenuItem(popupMenu, SWT.NONE);
				cableItem.setText("Add Cable");
				cableItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						dest = getEndpoint(false);
						addConnection(true);
					}
				});
			}
		} else {
			targetForConnections = null;
			source = null;
			dest = null;
		}
		}
		TreeItem item = tree.getSelection()[0];
		TreeItem parentItem = item.getParentItem();
		if (parentItem != null) {
			Target configParentTarget = (Target) parentItem.getData();
			if (configParentTarget.attributeExists("IO_CONFIG_SELECT")) {
				MenuItem deconfigItem = new MenuItem(popupMenu, SWT.NONE);
				deconfigItem.setText("Deconfig");
				deconfigItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setConfig(false);
					}
				});
				MenuItem configItem = new MenuItem(popupMenu, SWT.NONE);
				configItem.setText("Select Config");
				configItem.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setConfig(true);
					}
				});
			}
		}
		tree.setMenu(popupMenu);
	}

	public void setConfig(boolean config) {
		TreeItem item = tree.getSelection()[0];
		Target target = (Target) item.getData();
		TreeItem parentItem = item.getParentItem();

		if (parentItem == null) {
			ServerWizard2.LOGGER.warning(target.getName() + " parent is null");
			return;
		}
		TreeItem grandParentItem = parentItem.getParentItem();
		Target configParentTarget = (Target) parentItem.getData();
		String configNum = target.getAttribute("IO_CONFIG_NUM");
		if (configNum.isEmpty()) {
			ServerWizard2.LOGGER.warning(target.getName() + " IO_CONFIG_NUM attribute is empty");
			return;
		}
		ConnectionEndpoint ep = getEndpoint(parentItem,null);
		String path = "/"+ep.getName();
		if (config) {
			controller.setGlobalSetting(path, "IO_CONFIG_SELECT", configNum);
		} else {
			controller.setGlobalSetting(path, "IO_CONFIG_SELECT", "0");
		}
		for (TreeItem childItem : parentItem.getItems()) {
			clearTree(childItem);
		}
		currentPath="/"+ep.getPath();
		currentPath=currentPath.substring(0, currentPath.length()-1);
		this.updateInstanceTree(configParentTarget, grandParentItem, parentItem);
	}

	private ConnectionEndpoint getEndpoint(TreeItem item, String stopCard) {
		ConnectionEndpoint endpoint = new ConnectionEndpoint();

		Target target = (Target) item.getData();
		endpoint.setTargetName(target.getName());

		Boolean done = false;
		Boolean found = false;
		TreeItem parentItem = item.getParentItem();

		while (!done) {
			if (parentItem==null) {
				done=true;
			} else {
				Target parentTarget = (Target) parentItem.getData();
				String parentName = parentTarget.getName();
				if (parentName.equals(stopCard)) {
					done = true;
					found = true;
				} else {
					endpoint.setPath(parentName + "/" + endpoint.getPath());
					parentItem = parentItem.getParentItem();
				}
			}
		}

		if (!found && stopCard != null) {
			MessageDialog.openError(null, "Connection Error", "The connection must start and end on or below selected card.");
			endpoint=null;
		}
		return endpoint;
	}

	private ConnectionEndpoint getEndpoint(boolean setBold) {
		TreeItem item = tree.getSelection()[0];
		ConnectionEndpoint endpoint = getEndpoint(item,cmbCards.getText());
		if (setBold && endpoint != null) {
			setEndpointState(item);
		}
		return endpoint;
	}

	public void setEndpointState(TreeItem item) {
		if (item != selectedEndpoint && selectedEndpoint != null) {
			this.setFontStyle(selectedEndpoint, SWT.BOLD, false);
		}
		this.setFontStyle(item, SWT.BOLD | SWT.ITALIC, true);
		selectedEndpoint = item;
	}
	private void clearTreeAll() {
		if (tree.getItemCount() > 0) {
			clearTree(tree.getItem(0));
		}
		tree.removeAll();
	}

	private void clearTree(TreeItem treeitem) {
		for (int i = 0; i < treeitem.getItemCount(); i++) {
			clearTree(treeitem.getItem(i));
		}
		treeitem.removeAll();
		treeitem.dispose();
	}
	private void refreshInstanceTree() {
		currentPath="";
		targetFound = false;
		for (Target target : controller.getRootTargets()) {
			this.updateInstanceTree(target, null);
		}
		if (controller.getRootTargets().size() == 0) {
			this.clearTreeAll();
		}
		btnAddTarget.setEnabled(false);
	}

	public void updateInstanceTree(Target target, TreeItem parentItem) {
		this.updateInstanceTree(target, parentItem, null);
	}

	private void updateInstanceTree(Target target, TreeItem parentItem, TreeItem item) {
		if (target == null) {
			return;
		}
		if (target.isHidden()) {
			return;
		}
		if (parentItem==null) {
			this.clearTreeAll();
		}
		boolean hideBus = false;
		String name = target.getName();
		String lastPath = currentPath;
		currentPath=currentPath+"/"+name;

		if (busMode) {
			if (!target.isSystem() && !cmbBusses.getText().equals("NONE")) {
				if (target.isBusHidden(cmbBusses.getText())) {
					hideBus = true;
					if (btnHideBusses.getSelection()) {
						currentPath=lastPath;
						return;
					}
				}
			}
			if (parentItem != null) {
				if (controller.isGlobalSettings(lastPath, "IO_CONFIG_SELECT")) {
					Field cnfgSelect = controller.getGlobalSetting(lastPath, "IO_CONFIG_SELECT");
					if (!cnfgSelect.value.isEmpty() && !cnfgSelect.value.equals("0")) {
						String cnfg = target.getAttribute("IO_CONFIG_NUM");
						if (!cnfg.equals(cnfgSelect.value)) {
							hideBus = true;
							if (btnHideBusses.getSelection()) {
								currentPath=lastPath;
								return;
							}
						}
					}
				}
			}
			if (!hideBus) {
				String sch = target.getAttribute("SCHEMATIC_INTERFACE");
				if (!sch.isEmpty()) {
					name = name + " (" + sch + ")";
				}
				if (target.isInput() && target.isOutput()) {
					name = name + " <=>";
				} else if (target.isInput()) {
					name = name + " <=";
				} else if (target.isOutput()) {
					name = name + " =>";
				}
			}
		}
		Vector<Target> children = controller.getVisibleChildren(target,this.btnShowHidden.getSelection());
		TreeItem treeitem = item;
		if (item == null) {
			if (parentItem == null) {
				treeitem = new TreeItem(tree, SWT.VIRTUAL | SWT.BORDER);
			} else {
				treeitem = new TreeItem(parentItem, SWT.VIRTUAL | SWT.BORDER);
			}
		}
		treeitem.setText(name);
		treeitem.setData(target);

		if (target.isPluggable()) {
			this.setFontStyle(treeitem, SWT.ITALIC, true);
		}
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				Target childTarget = children.get(i);
				updateInstanceTree(childTarget, treeitem);
			}
		}
		if (target == targetForConnections && busMode) {
			this.setFontStyle(treeitem, SWT.BOLD, true);
			if (!targetFound) {
				tree.select(treeitem);
				for (TreeItem childItem : treeitem.getItems()) {
					tree.showItem(childItem);
				}
				targetFound = true;
			}
		}
		currentPath=lastPath;
	}

	private void addConnection(Connection conn) {
		listBusses.add(conn.getName());
		listBusses.setData(conn.getName(), conn);
	}

	private void refreshConnections() {
		this.source=null;
		this.dest=null;
		listBusses.removeAll();
		if (cmbBusses == null) {
			return;
		}
		Target busTarget = (Target) cmbBusses.getData(cmbBusses.getText());
		if (targetForConnections == null) {
			return;
		}

		if (busTarget == null) {
			for (Target tmpBusTarget : targetForConnections.getBusses().keySet()) {
				for (Connection conn : targetForConnections.getBusses().get(tmpBusTarget)) {
					addConnection(conn);
				}
			}
		} else {
			for (Connection conn : targetForConnections.getBusses().get(busTarget)) {
				addConnection(conn);
			}
		}
	}

	private void addConnection(Boolean cabled) {
		Target busTarget = (Target) cmbBusses.getData(cmbBusses.getText());
		Connection conn = targetForConnections.addConnection(busTarget, source, dest, cabled);
		this.addConnection(conn);
		setDirtyState(true);
	}

	private void deleteConnection() {
		if (targetForConnections == null || listBusses.getSelectionCount() == 0) {
			return;
		}
		Connection conn = (Connection) listBusses.getData(listBusses.getSelection()[0]);
		controller.deleteConnection(targetForConnections, conn.busTarget, conn);
		this.refreshConnections();
		setDirtyState(true);
	}

	private void setFontStyle(TreeItem item, int style, boolean selected) {
		if (item.isDisposed()) {
			return;
		}
		FontData[] fD = item.getFont().getFontData();
		fD[0].setStyle(selected ? style : 0);
		Font newFont = new Font(this.getShell().getDisplay(), fD[0]);
		item.setFont(newFont);
	}

	@Override
	public boolean close() {
		if (dirty) {
			if (!MessageDialog.openConfirm(null, "Save Resource", mrwFilename
					+ "has been modified. Ignore changes?")) {
				return false;
			}
			ServerWizard2.LOGGER.info("Discarding changes and exiting...");
		}
		clearTreeAll();
		for (Control c : container.getChildren()) {
			c.dispose();
		}
		return super.close();
	}

	public void setDirtyState(Boolean dirty) {
		this.dirty = dirty;
		if (this.btnSave != null) {
			this.btnSave.setEnabled(dirty);
		}
		if (dirty) {
			this.getShell().setText("ServerWiz2 - " + this.mrwFilename + " *");
		} else {
			this.getShell().setText("ServerWiz2 - " + this.mrwFilename);
		}
	}

	public void setFilename(String filename) {
		this.mrwFilename = filename;
		if (btnSave != null) {
			this.btnSave.setEnabled(true);
		}
		this.getShell().setText("ServerWiz2 - " + this.mrwFilename);
	}

	private void addEvents() {
		showFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateView();
			}
		});
		btnShowHidden.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				refreshInstanceTree();
			}
		});
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (tabFolder.getSelection()[0]==tbtmAddBusses) {
					initBusMode();
				} else {
					initInstanceMode();
				}
			}
		});
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateView();
			}
		});

		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				final TreeItem treeItem = (TreeItem) e.item;
				getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!treeItem.isDisposed()) {
							treeItem.getParent().getColumns()[0].pack();
						}
					}
				});	
			}
		});

		btnAddTarget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Target chk = (Target) combo.getData(combo.getText());
				if (chk != null) {
					TreeItem selectedItem = null;
					Target parentTarget = null;
					if (tree.getSelectionCount() > 0) { 
						selectedItem = tree.getSelection()[0];
						parentTarget = (Target) selectedItem.getData();
					}
					if (chk.getType().equals("chip") || chk.getType().equals("targetoverride")) {
						ServerWizard2.LOGGER.info("Entering model creation mode");
						attributeEditor.setIgnoreReadonly();
						controller.setModelCreationMode();
						tbtmAddBusses.dispose();
					}
					String nameOverride = txtInstanceName.getText();
					controller.addTargetInstance(chk, parentTarget, selectedItem, nameOverride);
					txtInstanceName.setText("");
					if (tree.getSelectionCount() > 0) {
						selectedItem.setExpanded(true);
					}
					columnName.pack();
					setDirtyState(true);
				}
			}
		});
		btnDeleteTarget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem treeitem = tree.getSelection()[0];
				if (treeitem == null) {
					return;
				}
				controller.deleteTarget((Target) treeitem.getData());
				clearTree(treeitem);
				setDirtyState(true);
			}
		});
		btnCopyInstance.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TreeItem selectedItem = tree.getSelection()[0];
				if (selectedItem == null) {
					return;
				}
				Target target = (Target) selectedItem.getData();
				Target parentTarget = (Target) selectedItem.getParentItem().getData();
				Target newTarget = controller.copyTargetInstance(target, parentTarget, true);
				updateInstanceTree(newTarget, selectedItem.getParentItem());
				TreeItem t = selectedItem.getParentItem();
				tree.select(t.getItem(t.getItemCount() - 1));
				setDirtyState(true);
			}
		});
		btnDefaults.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TreeItem selectedItem = tree.getSelection()[0];
				if (selectedItem == null) {
					return;
				}
				if (!MessageDialog.openConfirm(null, "Restore Defaults", "Are you sure you want to restore default attribute values on this target and all of it's children?")) {
					return;
				}
				ServerWizard2.LOGGER.info("Restoring Defaults");
				Target target = (Target) selectedItem.getData();
				controller.deepCopyAttributes(target);
				setDirtyState(true);
			}
		});
		
		cmbCards.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Target t = (Target) cmbCards.getData(cmbCards.getText());
				targetForConnections = t;
				refreshInstanceTree();
				refreshConnections();
			}
		});
		cmbBusses.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refreshInstanceTree();
				refreshConnections();
			}
		});
		btnDeleteConnection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				deleteConnection();
			}
		});
		listBusses.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (listBusses.getSelectionCount() > 0) {
					Connection conn = (Connection) listBusses.getData(listBusses.getSelection()[0]);
					attributes = controller.getAttributesAndGlobals(conn.busTarget, "","");
					viewer.setInput(attributes);
					viewer.refresh();
				}
			}
		});
		btnHideBusses.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshInstanceTree();
				refreshConnections();
			}
		});
	}

	private void createAttributeTable() {
		Table table = viewer.getTable();
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE); 
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		table.addListener(SWT.CHANGED, new Listener() {
			public void handleEvent(Event event) {
				setDirtyState(true);
			}
		});
		
		final TableViewerColumn colName = new TableViewerColumn(viewer, SWT.NONE);
		colName.getColumn().setWidth(256);
		colName.getColumn().setText("Attribute");
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Field f = (Field) element;
				return f.attributeName;
			}
		});

		final TableViewerColumn colField = new TableViewerColumn(viewer, SWT.NONE);
		colField.getColumn().setWidth(100);
		colField.getColumn().setText("Field");
		colField.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Field f = (Field) element;
				if (f.attributeName.equals(f.name)) {
					return "";
				}
				return f.name;
			}
		});

		final TableViewerColumn colValue = new TableViewerColumn(viewer, SWT.NONE);
		colValue.getColumn().setWidth(100);
		colValue.getColumn().setText("Value");
		colValue.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Field f = (Field) element;
				return f.value;
			}
		});
		attributeEditor = new AttributeEditingSupport(viewer);
		colValue.setEditingSupport(attributeEditor);
		
		final TableViewerColumn colDesc = new TableViewerColumn(viewer, SWT.NONE);
		colDesc.getColumn().setWidth(350);
		colDesc.getColumn().setText("Description");
		colDesc.setLabelProvider(new ColumnLabelProvider() {
		    public String getToolTipText(Object element) {
				Field f = (Field) element;
		    	return f.desc;
		    }
			@Override
			public String getText(Object element) {
				Field f = (Field) element;
				String desc = f.desc.replace("\n", "");
				return desc;
			}
		});

		final TableViewerColumn colGroup = new TableViewerColumn(viewer, SWT.NONE);
		colGroup.getColumn().setWidth(120);
		colGroup.getColumn().setText("Group");
		colGroup.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Field f = (Field) element;
				return f.group;
			}
		});
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
	}
}
