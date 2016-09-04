package com.ibm.ServerWizard2.utility;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.ProgressMonitor;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;

import com.ibm.ServerWizard2.ServerWizard2;
import com.ibm.ServerWizard2.view.PasswordPrompt;

public class GithubRepository implements Comparable<GithubRepository> {
	private String remoteUrl;
	private File rootDirectory;
	private File gitDirectory;
	private boolean needsPassword;
	private boolean passwordValidated = false;
	private UsernamePasswordCredentialsProvider credentials;
	
	private boolean cloned;
	private final RefSpec refSpec = new RefSpec("+refs/heads/*:refs/remotes/origin/*");

	public GithubRepository(String remoteUrl, String localDir, boolean needsPassword) {
		this.remoteUrl = remoteUrl;
		this.needsPassword = needsPassword;
		File f = new File(remoteUrl);
		String localPath = localDir + File.separator + f.getName().replace(".git", "");
		rootDirectory = new File(localPath);
		gitDirectory = new File(localPath + File.separator + ".git");
		cloned = false;
		if (RepositoryCache.FileKey.isGitRepository(this.getGitDirectory(), FS.DETECTED)) {
			cloned = true;
		}
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	public File getGitDirectory() {
		return gitDirectory;
	}

	public void getPassword() {
		if (!this.needsPassword) {
			credentials = new UsernamePasswordCredentialsProvider("", "");
			return;
		}
		PasswordPrompt dlg = new PasswordPrompt(null, this.remoteUrl);
		dlg.open();
		credentials = new UsernamePasswordCredentialsProvider("", dlg.passwd);
		passwordValidated = false;
	}

	public String getRemoteUrl() {
		return this.remoteUrl;
	}
	public boolean needsPassword() {
		return this.needsPassword;
	}
	public String needsPasswordStr() {
		if (this.needsPassword) {
			return "true";
		}
		return "false";
	}

	public void checkRemote() throws Exception {
		if (!passwordValidated) {
			this.getPassword();
		}

		Collection<Ref> refs;
		boolean found = false;

		try {
			refs = Git.lsRemoteRepository().setCredentialsProvider(credentials).setHeads(true).setTags(true)
					.setRemote(this.remoteUrl).call();
			if (!refs.isEmpty()) {
				found = true;
				passwordValidated = true;
				for (Ref ref : refs) {
					ServerWizard2.LOGGER.info("Ref: " + ref);
				}
			}
		} catch (Exception e) {
			ServerWizard2.LOGGER.severe(e.getMessage());
			this.betterError(e);
		}
		if (!found) {
			passwordValidated = false;
			throw new Exception("Invalid Remote Repository or bad Password:\n" + this.getRemoteUrl());
		}
	}

	public void cloneRepository() throws Exception {
		if (!passwordValidated) {
			this.getPassword();
		}
		if (isCloned()) {
			return;
		}
		ServerWizard2.LOGGER.info("Cloning " + this.remoteUrl + " into " + this.getRootDirectory().getAbsolutePath());
		cloned = false;
		try {
			ProgressMonitor pim = new ProgressMonitor(null, "Cloning...", "", 0, 1);
			GitProgressMonitor gpim = new GitProgressMonitor(pim);
			Git result = Git.cloneRepository()
					.setCredentialsProvider(credentials)
					.setProgressMonitor(gpim)
					.setURI(this.getRemoteUrl()).setDirectory(this.getRootDirectory()).call();

			cloned = true;
			result.close();
		} catch (Exception e1) {
			passwordValidated = false;
			this.betterError(e1);
		}
	}

	public void fetch(boolean reset) throws Exception {
		if (!passwordValidated) {
			this.getPassword();
		}

		ServerWizard2.LOGGER.info("Fetching " + this.rootDirectory.getAbsolutePath());

		try {
			ProgressMonitor pim = new ProgressMonitor(null, "Fetching...", "", 0, 1);
			GitProgressMonitor gpim = new GitProgressMonitor(pim);
			Git result = Git.open(this.rootDirectory);
			FetchResult fetch = result.fetch().setRemote(this.getRemoteUrl()).setCredentialsProvider(credentials)
					.setProgressMonitor(gpim).setRefSpecs(refSpec).call();

			ServerWizard2.LOGGER.info(fetch.getMessages());
			if (reset) {
				ServerWizard2.LOGGER.info("Resetting to head: " + this.getRemoteUrl());
				result.reset().setMode(ResetType.HARD).call();
			} else {
				ServerWizard2.LOGGER.info("Rebase: " + this.getRemoteUrl());
				RebaseResult r = result.rebase().setUpstream("refs/remotes/origin/master").call();
				ServerWizard2.LOGGER.info(r.getStatus().toString());
			}
			result.close();
		} catch (Exception e1) {
			passwordValidated = false;
			this.betterError(e1);
		}
	}

	public org.eclipse.jgit.api.Status status() {
		org.eclipse.jgit.api.Status status = null;
		try {
			ProgressMonitor pim = new ProgressMonitor(null, "Fetching...", "", 0, 1);
			GitProgressMonitor gpim = new GitProgressMonitor(pim);
			Git result = Git.open(this.getRootDirectory());
			status = result.status().setProgressMonitor(gpim).call();

			result.close();
		} catch (Exception e1) {
			ServerWizard2.LOGGER.severe(e1.getMessage());
			status = null;
		}
		return status;
	}
	
	// The chained exceptions from jgit don't explain error
	// Need to dive down to root cause and make better error message.
	public void betterError(Exception e) throws Exception {
		HashMap<String,String> errorLookup = new HashMap<String,String>();
		errorLookup.put("java.net.UnknownHostException", "Unable to connect to location:");
		errorLookup.put("org.eclipse.jgit.errors.NoRemoteRepositoryException", "Remote Repository not found:");
		errorLookup.put("org.eclipse.jgit.errors.TransportException", "Invalid Remote Repository or Bad Password: ");
		ServerWizard2.LOGGER.severe(e.getCause().toString());
		Throwable t = getCause(e);
		ServerWizard2.LOGGER.severe(t.toString());
		String errorMsg = t.getMessage();
		if (errorLookup.containsKey(t.getClass().getName())) {
			errorMsg = errorLookup.get(t.getClass().getName()) + "\n" + t.getMessage();
		}
		throw new Exception(errorMsg);
	}

	public boolean isCloned() {
		return cloned;
	}

	public void clear() {
		// make sure stored password is destroyed
		credentials.clear();
	}

	public String toString() {
		return this.remoteUrl + "; " + this.getGitDirectory().getAbsolutePath() + "; " + this.needsPasswordStr();
	}

	@Override
	public int compareTo(GithubRepository o) {
		return this.getRemoteUrl().compareTo(o.getRemoteUrl());
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;

		if (o instanceof String) {
			String s = (String) o;
			if (this.getRemoteUrl().equals(s)) {
				return true;
			}
			return false;
		} else if (o instanceof GithubRepository) {
			GithubRepository p = (GithubRepository) o;
			return this.compareTo(p) == 0 ? true : false;
		}
		return false;
	}
	Throwable getCause(Throwable e) {
	    Throwable cause = null; 
	    Throwable result = e;

	    while(null != (cause = result.getCause())  && (result != cause) ) {
	        result = cause;
	    }
	    return result;
	}
}
