package com.ibm.ServerWizard2.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusLogger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.ServerWizard2.ServerWizard2;
import com.ibm.ServerWizard2.utility.Github;
import com.ibm.ServerWizard2.utility.GithubRepository;
import com.ibm.ServerWizard2.utility.ServerwizMessageDialog;

import org.eclipse.wb.swt.SWTResourceManager;

public class GitDialog extends Dialog {
	private Text txtNewRepo;
	Github git = new Github(ServerWizard2.GIT_LOCATION);

	private ListViewer listViewer;
	private Button btnCloned;
	private Button btnAdded;
	private Button btnRemoved;
	private Button btnChanged;
	private Button btnConflicting;
	private Button btnUntracked;
	private Button btnMissing;
	private Button btnNeedsPassword;
	private Button btnRefresh;
	private Text txtLocation;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public GitDialog(Shell parentShell) {
		super(parentShell);
		StatusLogger.getLogger().setLevel(Level.FATAL);
	}

	private boolean isStatusSet(Set<String> status) {
		if (status.size() > 0) {
			for (String s : status) {
				ServerWizard2.LOGGER.info(s);
			}
			return true;
		}
		return false;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();

		Label lblGitRepositoryUrl = new Label(container, SWT.NONE);
		lblGitRepositoryUrl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblGitRepositoryUrl.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblGitRepositoryUrl.setText("Git Repository URL:");
		listViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);

		List repositoryList = listViewer.getList();
		repositoryList.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		repositoryList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (listViewer.getList().getSelectionCount() == 0) {
					return;
				}
				GithubRepository g = (GithubRepository) listViewer
						.getElementAt(listViewer.getList().getSelectionIndex());
				txtNewRepo.setText(g.getRemoteUrl());
				btnNeedsPassword.setSelection(g.needsPassword());
				if (g.isCloned()) {
					btnCloned.setSelection(true);
					txtLocation.setText(g.getRootDirectory().getAbsolutePath());
					org.eclipse.jgit.api.Status status = g.status();
					if (status != null) {
						btnAdded.setSelection(isStatusSet(status.getAdded()));
						btnRemoved.setSelection(isStatusSet(status.getRemoved()));
						btnChanged.setSelection(isStatusSet(status.getChanged()) | isStatusSet(status.getModified()));
						btnConflicting.setSelection(isStatusSet(status.getConflicting()));
						btnUntracked.setSelection(isStatusSet(status.getUntracked()));
						btnMissing.setSelection(isStatusSet(status.getMissing()));

					} else {
						ServerwizMessageDialog.openError(null, "Git Error",
								"The Git Repository has been modified.  Please restart Serverwiz." + g.getRemoteUrl());
					}
				} else {
					btnCloned.setSelection(false);
				}
			}
		});
		GridData gd_repositoryList = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_repositoryList.widthHint = 314;
		gd_repositoryList.heightHint = 111;
		repositoryList.setLayoutData(gd_repositoryList);

		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(new GridLayout(5, false));
		GridData gd_composite = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 367;
		gd_composite.heightHint = 52;
		composite.setLayoutData(gd_composite);
		composite.setEnabled(false);

		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 48;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("Status:");

		btnCloned = new Button(composite, SWT.CHECK);
		btnCloned.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnCloned.setText("Cloned");

		btnAdded = new Button(composite, SWT.CHECK);
		btnAdded.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnAdded.setText("Added");

		btnRemoved = new Button(composite, SWT.CHECK);
		btnRemoved.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnRemoved.setText("Removed");

		btnChanged = new Button(composite, SWT.CHECK);
		btnChanged.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnChanged.setText("Changed");
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);

		btnConflicting = new Button(composite, SWT.CHECK);
		btnConflicting.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnConflicting.setText("Conflicting");

		btnUntracked = new Button(composite, SWT.CHECK);
		btnUntracked.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnUntracked.setText("Untracked");

		btnMissing = new Button(composite, SWT.CHECK);
		btnMissing.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnMissing.setText("Missing");

		Composite composite_4 = new Composite(container, SWT.NONE);
		composite_4.setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData gd_composite_4 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite_4.widthHint = 351;
		gd_composite_4.heightHint = 31;
		composite_4.setLayoutData(gd_composite_4);

		Label lblLocalLocation = new Label(composite_4, SWT.NONE);
		lblLocalLocation.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblLocalLocation.setLayoutData(new RowData(93, 24));
		lblLocalLocation.setText("Local Location: ");

		txtLocation = new Text(composite_4, SWT.BORDER);
		txtLocation.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		txtLocation.setEditable(false);
		txtLocation.setLayoutData(new RowData(237, SWT.DEFAULT));

		Composite composite_3 = new Composite(container, SWT.NONE);
		composite_3.setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData gd_composite_3 = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_composite_3.widthHint = 194;
		gd_composite_3.heightHint = 36;
		composite_3.setLayoutData(gd_composite_3);

		btnRefresh = new Button(composite_3, SWT.NONE);
		btnRefresh.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnRefresh.setLayoutData(new RowData(80, SWT.DEFAULT));
		btnRefresh.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (listViewer.getList().getSelectionCount() == 0) {
					return;
				}
				GithubRepository g = (GithubRepository) listViewer
						.getElementAt(listViewer.getList().getSelectionIndex());
				try {
					ServerWizard2.DEFAULT_REMOTE_URL = g.getRemoteUrl();
					boolean Alert = false;
					if (!g.isCloned()) {
						g.cloneRepository();
					}
					else if(g.getRemoteUrl().contains("common-mrw"))
					{
					   Alert = MessageDialog.openConfirm(null, "ALERT", "A Common mrw Repo already exists in disk.\n It will overlay on the existing repo in disk.\n Please delete it in disk and reload with this if need this as base, Confirm to Overlay");
					}
					boolean branch = MessageDialog.openConfirm(null, "Branch name", "Confirm to enter branch name other than master");
					if(branch) {
				      ServerWizard2.branchName = MessageDialogWithToggle.shortenText(null, dialogArea);
					}
					org.eclipse.jgit.api.Status status = g.status();
					if(Alert) {
					if (!status.isClean()) {
						boolean reset = MessageDialog.openQuestion(null, "Repository is Modified",
								"The local repository for:\n" + g.getRemoteUrl() + "\n"
										+ "has been modified. Would you like to ignore changes and reset?");
						if (reset) {
							String r = g.fetch(true);
							ServerwizMessageDialog.openInformation(null, "Refresh Complete", "Reset Successful");

						}
					} else {
						String r = g.fetch(false);
						ServerwizMessageDialog.openInformation(null, "Refresh Complete", "Message: " + r);
					}
					MessageDialog.openInformation(null, "Alert!!", "Please rerun the project for model to load");
					}
				} catch (Exception e) {
					ServerwizMessageDialog.openError(null, "Git Refresh: " + g.getRemoteUrl(), e.getMessage());
				}
			}
		});

		btnRefresh.setText("Refresh");

		Button btnDummy = new Button(composite_3, SWT.NONE);
		btnDummy.setLayoutData(new RowData(14, SWT.DEFAULT));
		btnDummy.setVisible(false);
		btnDummy.setText("New Button");

		Button btnDelete = new Button(composite_3, SWT.NONE);
		btnDelete.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnDelete.setLayoutData(new RowData(80, SWT.DEFAULT));
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (listViewer.getList().getSelectionCount() == 0) {
					return;
				}
				GithubRepository g = (GithubRepository) listViewer
						.getElementAt(listViewer.getList().getSelectionIndex());

				boolean delete = MessageDialog.openConfirm(null, "Delete Respository",
						"Are you sure you want to delete the following repository?\n" + g.getRemoteUrl() + "\n"
								+ "Note: This will NOT delete repository from disk");

				if (!delete) {
					return;
				}
					git.getRepositories().remove(g);
					listViewer.refresh();
			}
		});
		btnDelete.setText("Delete");

		Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_separator = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_separator.heightHint = 7;
		gd_separator.widthHint = 360;
		separator.setLayoutData(gd_separator);

		Composite composite_2 = new Composite(container, SWT.NONE);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite_2.widthHint = 354;
		gd_composite_2.heightHint = 46;
		composite_2.setLayoutData(gd_composite_2);

		Label lblNewRepository = new Label(composite_2, SWT.NONE);
		lblNewRepository.setLayoutData(new RowData(95, SWT.DEFAULT));
		lblNewRepository.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		lblNewRepository.setText("New Repository:");

		txtNewRepo = new Text(composite_2, SWT.BORDER);
		txtNewRepo.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		txtNewRepo.setLayoutData(new RowData(234, SWT.DEFAULT));

		Composite composite_1 = new Composite(container, SWT.NONE);
		RowLayout rl_composite_1 = new RowLayout(SWT.HORIZONTAL);
		rl_composite_1.center = true;
		composite_1.setLayout(rl_composite_1);
		GridData gd_composite_1 = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		gd_composite_1.widthHint = 252;
		gd_composite_1.heightHint = 49;
		composite_1.setLayoutData(gd_composite_1);

		btnNeedsPassword = new Button(composite_1, SWT.CHECK);
		btnNeedsPassword.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnNeedsPassword.setLayoutData(new RowData(148, 40));
		btnNeedsPassword.setText("Needs Password?");

		Button btnAddRemote = new Button(composite_1, SWT.NONE);
		btnAddRemote.setLayoutData(new RowData(93, SWT.DEFAULT));
		btnAddRemote.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		btnAddRemote.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String repo = txtNewRepo.getText();
				if (repo.isEmpty()) {
					ServerwizMessageDialog.openError(null, "Error", "Repository URL is blank");
					return;
				}
				GithubRepository g = new GithubRepository(repo, git.getLocation(), btnNeedsPassword.getSelection());
				g.setShell(getShell());
				if (git.isRepository(g)) {
					ServerwizMessageDialog.openError(null, "Error", "Repository already exists");
					return;
				}
				try {
					g.checkRemote();
					boolean Alert = false;
					if((!ServerWizard2.DEFAULT_REMOTE_URL.isEmpty()) && (g.getRemoteUrl().contains("common-mrw")))
					{
						 Alert = MessageDialog.openConfirm(null, "ALERT", "A Common mrw Repo already exists in disk.\n It will be overlaid on the repo existing in disk.\n Please delete it in disk and refresh with this if need this as base, Confirm to Overlay!!!!");
					}
					if(Alert)
					{
					 g.cloneRepository();}
					git.getRepositories().add(g);
					txtNewRepo.setText("");
					listViewer.refresh();
					ServerwizMessageDialog.openInformation(null, "New Repository","Repositiory added:\n"+g.getRootDirectory());
				} catch (Exception e) {
					ServerwizMessageDialog.openError(null, "Add Remote: " + g.getRemoteUrl(), e.getMessage());
				}
			}
		});
		btnAddRemote.setText("Add Remote");
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		listViewer.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				GithubRepository g = (GithubRepository) element;
				return g.getRemoteUrl();
			}
		});
		listViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				@SuppressWarnings("rawtypes")
				Vector v = (Vector) inputElement;
				return v.toArray();
			}

			@Override
			public void dispose() {
				// TODO Auto-generated method stub

			}

			@Override
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub

			}
		});
		listViewer.setInput(git.getRepositories());
		this.getRepositories();
		listViewer.refresh();
		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		button.setFont(SWTResourceManager.getFont("Arial", 9, SWT.NORMAL));
		button.setText("Exit");
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(409, 523);
	}

	public void getRepositories() {
		try {
			boolean newFile = false;
			File f = new File(ServerWizard2.PROPERTIES_FILE);
			if (!f.exists()) {
				ServerWizard2.LOGGER.info("Preferences file doesn't exist, creating...");
				f.createNewFile();
				newFile = true;
			}
			FileInputStream propFile = new FileInputStream(ServerWizard2.PROPERTIES_FILE);
			Properties p = new Properties();
			p.load(propFile);
			propFile.close();

			if (newFile) {
				p.setProperty("repositories", ServerWizard2.DEFAULT_REMOTE_URL);
				p.setProperty("needs_password", "false");
			}
			String repos = p.getProperty("repositories");
			String pass = p.getProperty("needs_password");
			String repo[] = repos.split(",");
			String needsPass[] = pass.split(",");
			git.getRepositories().removeAllElements();

			for (int i = 0; i < repo.length; i++) {
				boolean n = false;
				if (needsPass[i].equals("true")) {
					n = true;
				}
				GithubRepository g = new GithubRepository(repo[i], ServerWizard2.GIT_LOCATION, n);
				g.setShell(getShell());
				git.getRepositories().add(g);
			}
		} catch (Exception e) {
			ServerWizard2.LOGGER.severe(e.getMessage());
		}
	}

	public boolean close() {
		Properties p = new Properties();
		try {
			FileInputStream propFile = new FileInputStream(ServerWizard2.PROPERTIES_FILE);
			p.load(propFile);
			propFile.close();

			FileOutputStream out = new FileOutputStream(ServerWizard2.PROPERTIES_FILE);
			String repo = git.getRepositoriesStr();
			String pass = git.getPasswordStr();
			p.setProperty("repositories", repo);
			p.setProperty("needs_password", pass);
			p.store(out, "");
			out.close();
		} catch (IOException e) {
			ServerWizard2.LOGGER.severe("Unable to write preferences file");
		}
		return super.close();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Manage Git Repositories");
	}
}
