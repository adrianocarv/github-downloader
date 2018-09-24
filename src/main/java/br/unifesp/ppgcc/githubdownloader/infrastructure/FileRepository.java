package br.unifesp.ppgcc.githubdownloader.infrastructure;

import java.io.BufferedReader; 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

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
	private String crawledPath;
	
	
	public FileRepository() {

		try{
			String path = ConfigProperties.getProperty("path.download") + "/";
			String executionName = ConfigProperties.getProperty("execution_name");

			this.inputFilePath = path + "input.txt"; 
			this.inputTempFilePath = path + "input_temp.txt";
			
			this.outputPath = path + executionName + "/"; 
			this.outputFilePath = outputPath + "@output.txt"; 
			this.unzipPath = ConfigProperties.getProperty("path.unzip") + "/"; 
			this.crawledPath = ConfigProperties.getProperty("path.unzip") + "_crawled/1/"; 

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
	
	public void deleteUnzipedFolder(String folderName) throws Exception {
		
		//create expection folder, if necessary
		String pathToDelete = this.unzipPath + "@to_delete/";
		File pathToDeleteFolder = new File(pathToDelete);
		if(!pathToDeleteFolder.isDirectory())
			pathToDeleteFolder.mkdir();
		
		File oldFolder = new File(this.unzipPath + folderName);
		File newFolder = new File(pathToDelete + folderName);

		//move folder
		if(oldFolder.isDirectory())
			oldFolder.renameTo(newFolder);
		
		//File unzipFolder = new File(this.unzipPath + folderName);
		//FileUtils.deleteDirectory(unzipFolder);
	}
	
	public void copyZipedProject(String projectZipFile) throws Exception {
		
		//create expection folder, if necessary
		String pathToCopy = this.unzipPath + "@copy/";
		File pathToDeleteFolder = new File(pathToCopy);
		if(!pathToDeleteFolder.isDirectory())
			pathToDeleteFolder.mkdir();
		
		File oldFolder = new File(this.outputPath + projectZipFile);
		File newFolder = new File(pathToCopy + projectZipFile);

		//copy file
	    Files.copy(oldFolder.toPath(), newFolder.toPath());
	}
	
	public void downloadGitHubProject(GitHubProject gitHubProject) throws Exception {
		gitHubProject.setDownloadStartTime(System.currentTimeMillis());

		new File(this.outputPath).mkdir();
		
		long contentLength = HttpDownloadUtility.downloadFile(gitHubProject, this.outputPath);
		gitHubProject.setZipFileSizeInBytes(contentLength);

		gitHubProject.setDownloadFinishTime(System.currentTimeMillis());
	}

	public List<String> getDownloadedProjects() throws Exception {
		return this.getZipFiles(this.outputPath, true);
	}

	public List<String> getDownloadedProjectsUnzipedFolder() throws Exception {
		return this.getZipFiles(this.unzipPath, true);
	}

	public List<String> getUnzipedFolderProjects() throws Exception {
		return this.getZipFiles(this.unzipPath, false);
	}

	private List<String> getZipFiles(String path, boolean zipFiles) throws Exception {

		File dir = new File(path);
		File[] files = null;
		
		if(zipFiles){
			files = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File current, String name) {
			        return name.toLowerCase().endsWith(".zip");
			    }
			});
		}else{
			files = dir.listFiles(new FilenameFilter() {
			    public boolean accept(File current, String name) {
			        return new File(current, name).isDirectory();
			    }
			});
		}
		
		List<String> downloadedProjects = new ArrayList<String>();
		for(File file : files){
			downloadedProjects.add(file.getName());
		}
		
		return downloadedProjects;
	}
	
	public void unzipProject(GitHubProject gitHubProject) throws Exception {
		
		//1. Create necessary folders: @unzip_phase, @exception, @to_delete 
		File unzipPhaseFolder = new File(this.unzipPath + "@unzip-phase");
		File exceptionFolder = new File(this.unzipPath + "@exception");
		File toDeleteFolder = new File(this.unzipPath + "@to_delete");
		if(!unzipPhaseFolder.isDirectory())
			unzipPhaseFolder.mkdir();
		if(!exceptionFolder.isDirectory())
			exceptionFolder.mkdir();
		if(!toDeleteFolder.isDirectory())
			toDeleteFolder.mkdir();

		//2. Move zip file to @unzip_phase
		File sourceZipFile = new File(this.unzipPath + gitHubProject.getDownloadedFileName());
		File unizipPhaseZipFile = new File(unzipPhaseFolder + "/" + sourceZipFile.getName());
		sourceZipFile.renameTo(unizipPhaseZipFile);
		
		//3. Unzip zip file on @unzip_phase
		UnzipUtility unzipUtility = new UnzipUtility();
		boolean checkUnzip = unzipUtility.unzip(unizipPhaseZipFile.getPath(), unzipPhaseFolder.getPath());
		String uniqueInternalDirectoryName = unzipUtility.getUniqueInternalDirectoryName(unizipPhaseZipFile.getPath());
		File unizipedFileFolder = new File(unzipPhaseFolder + "/" + uniqueInternalDirectoryName);

		//Exception treatment
		if(!checkUnzip || uniqueInternalDirectoryName == null){
			//Move zip file to @exception folder
			unizipPhaseZipFile.renameTo(new File(exceptionFolder.getPath() + "/" + unizipPhaseZipFile.getName()));

			//Move uzipped folder to @to_delete folder
			if(uniqueInternalDirectoryName != null){
				File unizipedFileToDeleteFolder = new File(toDeleteFolder + "/" + unizipedFileFolder.getName());
				unizipedFileFolder.renameTo(unizipedFileToDeleteFolder);
			}
			return;
		}
		
		//4. Rename unzipped-folder
		File unizipedFileNewFolder = new File(unzipPhaseFolder + "/" + StringUtils.substringBeforeLast(gitHubProject.getDownloadedFileName(), ".zip"));
		unizipedFileFolder.renameTo(unizipedFileNewFolder);
		
		//5. Move unziped~folder from @unzip-phase to unizp folder
		unizipedFileNewFolder.renameTo(new File(this.unzipPath + unizipedFileNewFolder.getName()));
		
		//6. Delete Unzip zip file on @unzip-phase
		unizipPhaseZipFile.delete();
	}

	public boolean moveFile(String fileName) throws Exception {
		File file = new File(this.outputPath + fileName);
		return file.renameTo(new File(this.outputPath + "moved/" + fileName));
	}

	public void createCrawledFolder() {
		File crawledFolder = new File(this.crawledPath);
		if(!crawledFolder.isDirectory())
			crawledFolder.mkdirs();
	}

	public int getNextCrawledFolderNumber() {
		File crawledFolder = new File(this.crawledPath);

		File[] files = crawledFolder.listFiles(new FilenameFilter() {
		    public boolean accept(File current, String name) {
		        return new File(current, name).isDirectory();
		    }
		});
		
		return files.length + 1;
	}
	
	public void convertToSorcererCrawledFormat(String unzipProject, int folderNumber) throws IOException {
		
		//Create project.properties file  
		File target = new File(this.crawledPath + folderNumber);
		if(target.isDirectory()){
			throw new IOException("Pasta já existente: " + target.getPath());
		}else{
			target.mkdir();
		}
		Path path = Paths.get(target.getPath() + File.separator + "project.properties");
		String msg = "name=" + unzipProject;
        Files.write(path, msg.getBytes());

        //Move to crawled folder  
        Path source = Paths.get(this.unzipPath + unzipProject);
		File targetContentFolder = new File(target.getPath() + File.separator + "content" + File.separator + unzipProject);
		targetContentFolder.mkdirs();
		Path targetContent = Paths.get(targetContentFolder.getPath());
		Files.move(source, targetContent, StandardCopyOption.REPLACE_EXISTING);
	}
}
