package com.ibm.ServerWizard2.utility;

import java.io.File;
import java.util.Vector;

public class Github { 
	private Vector<GithubRepository> repositories = new Vector<GithubRepository>();
	private String localPath;

	public Github(String path) {
		this.localPath = path;
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}
	}
	public String getLocation() {
		return this.localPath;
	}
	public boolean isRepository(GithubRepository repo) {
		return repositories.contains(repo);
	}
	
	public Vector<GithubRepository> getRepositories() {
		return repositories;
	}
	
	public String getRepositoriesStr() {
		String repos[] = new String[this.repositories.size()];
		for (int i = 0; i < this.repositories.size(); i++) {
			repos[i] = this.repositories.get(i).getRemoteUrl();
		}
		return String.join(",", repos);
	}
	
	public String getPasswordStr() {
		String repos[] = new String[this.repositories.size()];
		for (int i = 0; i < this.repositories.size(); i++) {
			repos[i] = this.repositories.get(i).needsPasswordStr();
		}
		return String.join(",", repos);
	}
}
