package br.unifesp.ppgcc.githubdownloader.domain;

import java.util.concurrent.TimeUnit;

public class GitHubProject {

	private String repository;

	private long downloadStartTime;
	private long downloadFinishTime;
	private long zipFileSizeInBytes;

	public GitHubProject(String repository) {
		this.repository = repository;
	}

	public String getDownloadLink(){
		return "https://github.com/" + this.repository + "/archive/master.zip";
	}
	
	public String getOldDownloadedFileName(){
		return this.getGitHubRepositoryName() + "-master.zip";
	}

	public String getDownloadedFileName(){
		return getGitHubRepositoryUser() + "~" + getGitHubRepositoryName() + ".zip";
	}

	public String getDownloadDurationTime() {
		long seconds = TimeUnit.MILLISECONDS.toSeconds(downloadFinishTime - downloadStartTime);
		return seconds + " segundos";
	}

	private String getGitHubRepositoryUser(){
		return repository == null ? "" : repository.split("/")[0];
	}
	
	private String getGitHubRepositoryName(){
		return repository == null ? "" : repository.split("/")[1];
	}
	
	//accessors
	public String getRepository() {
		return repository;
	}

	public long getDownloadStartTime() {
		return downloadStartTime;
	}

	public void setDownloadStartTime(long downloadStartTime) {
		this.downloadStartTime = downloadStartTime;
	}

	public long getDownloadFinishTime() {
		return downloadFinishTime;
	}

	public void setDownloadFinishTime(long downloadFinishTime) {
		this.downloadFinishTime = downloadFinishTime;
	}

	public long getZipFileSizeInBytes() {
		return zipFileSizeInBytes;
	}

	public void setZipFileSizeInBytes(long zipFileSizeInBytes) {
		this.zipFileSizeInBytes = zipFileSizeInBytes;
	}
}
