package com.ibm.ServerWizard2.utility;

import java.io.File;
import java.util.Vector;

public class Github {
	private Vector<GithubRepository> repositories = new Vector<GithubRepository>();

	public Github() {	
		File f = new File(GithubRepository.GIT_LOCAL_LOCATION);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	public boolean isRepository(GithubRepository repo) {
		return repositories.contains(repo);
	}
	
	public boolean addRepository(String repo, boolean needsPass) {
		for (GithubRepository g : repositories) {
			if (g.getRemoteUrl().equals(repo)) {
				return false;
			}
		}
		GithubRepository g = new GithubRepository(repo,needsPass);
		repositories.add(g);
		return true;
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
