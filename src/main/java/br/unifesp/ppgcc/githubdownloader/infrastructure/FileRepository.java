package br.unifesp.ppgcc.githubdownloader.infrastructure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import br.unifesp.ppgcc.githubdownloader.domain.GitHubProject;
import br.unifesp.ppgcc.githubdownloader.infrastructure.util.ConfigProperties;
import br.unifesp.ppgcc.githubdownloader.infrastructure.util.HttpDownloadUtility;
import br.unifesp.ppgcc.githubdownloader.infrastructure.util.UnzipUtility;

public class FileRepository {

	private String inputFilePath;
	private String inputTempFilePath;
	private String outputFilePath;
	private String outputPath;
	private String unzipPath;
	
	public FileRepository() {

		try{
			String path = ConfigProperties.getProperty("path.download") + "/";
			String executionName = ConfigProperties.getProperty("execution_name");

			this.inputFilePath = path + "input.txt"; 
			this.inputTempFilePath = path + "input_temp.txt";
			
			this.outputPath = path + executionName + "/"; 
			this.outputFilePath = outputPath + "@output.txt"; 
			this.unzipPath = path + executionName + "_unzip/"; 

		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getNotProcessedTotal() throws Exception {
		int total = 0;
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(this.inputFilePath));
		while (br.readLine() != null) {
			total++;
		}
		br.close();

		return total;
	}

	public List<GitHubProject> listGitHubProjects() throws Exception {
		List<GitHubProject> gitHubProjects = new ArrayList<GitHubProject>();
		
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(this.inputFilePath));
		String repository = br.readLine();
		while (repository != null) {
			gitHubProjects.add(new GitHubProject(repository));
			repository = br.readLine();
		}
		br.close();

		return gitHubProjects;
	}

	public List<GitHubProject> listAllGitHubProjects() throws Exception {
		List<GitHubProject> gitHubProjects = new ArrayList<GitHubProject>();
		
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(this.inputFilePath));
		String repository = br.readLine();
		while (repository != null) {
			gitHubProjects.add(new GitHubProject(repository));
			repository = br.readLine();
		}
		br.close();

		return gitHubProjects;
	}

	public GitHubProject getNextGitHubProject() throws Exception {
		BufferedReader br = null;
		br = new BufferedReader(new FileReader(this.inputFilePath));

		String repository = br.readLine();
		br.close();
		
		if(repository == null)
			return null;
		
		repository = repository.split(";")[0];
		return new GitHubProject(repository);
	}

	public void setDownloadedGitHubProject(GitHubProject gitHubProject) throws Exception {

		writeOutputFileLine(gitHubProject);
		removeInputFileFirstLine();
	}

	private void writeOutputFileLine(GitHubProject gitHubProject) throws Exception {
		FileWriter fileWriter = new FileWriter(this.outputFilePath, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		bufferedWriter.write(gitHubProject.getRepository());
		bufferedWriter.write(";" + gitHubProject.getZipFileSizeInBytes());
		bufferedWriter.write(";" + gitHubProject.getDownloadDurationTime());
		bufferedWriter.write(";" + gitHubProject.getDownloadStartTime());
		bufferedWriter.write(";" + gitHubProject.getDownloadFinishTime());
		bufferedWriter.newLine();

		// Always close files.
		bufferedWriter.close();
	}

	private void removeInputFileFirstLine() throws Exception {
		Scanner fileScanner = new Scanner(new File(this.inputFilePath));
		fileScanner.nextLine();

		FileWriter fileStream = new FileWriter(this.inputTempFilePath);
		BufferedWriter out = new BufferedWriter(fileStream);
		while (fileScanner.hasNextLine()) {
			String next = fileScanner.nextLine();
			if (next.equals("\n"))
				out.newLine();
			else
				out.write(next);
			out.newLine();
		}
		out.close();
		fileScanner.close();

		File oldFile = new File(this.inputFilePath);
		oldFile.delete();
		File newFile = new File(this.inputTempFilePath);
		newFile.renameTo(oldFile);
	}
	
	public boolean renameFile(GitHubProject gitHubProject) throws Exception {
		String oldPath = this.outputPath + gitHubProject.getOldDownloadedFileName();
		String newPath = this.outputPath + gitHubProject.getDownloadedFileName();
		
		File oldFile = new File(oldPath);

		if(!oldFile.isFile())
			return false;
	
		File newFile = new File(newPath);
		return oldFile.renameTo(newFile);
	}
	
	public void downloadGitHubProject(GitHubProject gitHubProject) throws Exception {
		gitHubProject.setDownloadStartTime(System.currentTimeMillis());

		new File(this.outputPath).mkdir();
		
		long contentLength = HttpDownloadUtility.downloadFile(gitHubProject, this.outputPath);
		gitHubProject.setZipFileSizeInBytes(contentLength);

		gitHubProject.setDownloadFinishTime(System.currentTimeMillis());
	}

	public List<String> getDownloadedProjects() throws Exception {
		return this.getZipFiles(this.outputPath);
	}

	public List<String> getDownloadedProjectsUnzipedFolder() throws Exception {
		return this.getZipFiles(this.unzipPath);
	}

	private List<String> getZipFiles(String path) throws Exception {

		File dir = new File(path);
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".zip");
		    }
		});

		List<String> downloadedProjects = new ArrayList<String>();
		for(File file : files){
			downloadedProjects.add(file.getName());
		}
		
		return downloadedProjects;
	}
	
	public void unzipProjectAndDeleteZipFile(GitHubProject gitHubProject) throws Exception {
		UnzipUtility unzipUtility = new UnzipUtility();
		unzipUtility.unzip(this.unzipPath + gitHubProject.getDownloadedFileName(), unzipPath);
		
		//remane folder name
		this.renameUnzippedFolder(gitHubProject);
		
		//delete zip file
		File zipProject = new File(this.unzipPath + gitHubProject.getDownloadedFileName());
		if(zipProject.isFile()){
			zipProject.delete();
		}
	}

	private boolean renameUnzippedFolder(GitHubProject gitHubProject) throws Exception {
		String oldPath = this.unzipPath + gitHubProject.getOldDownloadedFileName();
		String newPath = this.unzipPath + gitHubProject.getDownloadedFileName();
		oldPath = oldPath.replace(".zip", "");
		newPath = newPath.replace(".zip", "");
		
		File oldFolder = new File(oldPath);

		if(!oldFolder.isDirectory())
			return false;
	
		File newFolder = new File(newPath);
		return oldFolder.renameTo(newFolder);
	}
	

}
