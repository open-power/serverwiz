package com.ibm.ServerWizard2.utility;

public class GitProgressMonitor implements org.eclipse.jgit.lib.ProgressMonitor {

	javax.swing.ProgressMonitor pim;
	private int progress = 0;
	public GitProgressMonitor(javax.swing.ProgressMonitor pim) {
		this.pim = pim;
	}
	
	public void beginTask(String arg0, int arg1) {
		progress = 0;
		pim.setNote(arg0);
		pim.setMaximum(arg1);
	}

	@Override
	public void endTask() {
		pim.close();
	}

	@Override
	public boolean isCancelled() {
		return pim.isCanceled();
	}

	@Override
	public void start(int arg0) {
		progress = 0;
	}

	@Override
	public void update(int arg0) {
		progress = progress+arg0;
		pim.setProgress(progress);
	}
}
