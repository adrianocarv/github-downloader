package br.unifesp.ppgcc.githubdownloader.application;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;

import br.unifesp.ppgcc.githubdownloader.domain.GitHubProject;
import br.unifesp.ppgcc.githubdownloader.infrastructure.FileRepository;

public class GitHubDownloaderService {

	private FileRepository repository = new FileRepository();
	
	//Controll
	private int total;
	private int i;
	private long duraction;
	private long totalduraction;
	private long timestamp; 

	public GitHubDownloaderService() {
	}

	public void execute() throws Exception {

		this.setupControllVariables();
		
		//Print
		total = repository.getNotProcessedTotal();
		this.printTotalHeader(total);
		
		GitHubProject gitHubProject = repository.getNextGitHubProject();
		
		while(gitHubProject != null){

			this.downloadGitHubRepository(gitHubProject);
			
			gitHubProject = repository.getNextGitHubProject();
		}

		//Print
		this.printFinish();
	}

	public void renameFiles() throws Exception {

		this.setupControllVariables();
		
		List<GitHubProject> gitHubProjects = repository.listGitHubProjects();

		//Print
		total = gitHubProjects.size();
		this.printTotalHeader(total);
		
		
		for(GitHubProject gitHubProject : gitHubProjects){
			this.renameFile(gitHubProject);
		}

		//Print
		this.printFinish();
	}

	public void deleteUnzipedFolders() throws Exception {

		this.setupControllVariables();
		
		List<GitHubProject> gitHubProjects = repository.listGitHubProjects();

		//Print
		total = gitHubProjects.size();
		this.printTotalHeader(total);
		
		
		for(GitHubProject gitHubProject : gitHubProjects){
			this.deleteUnzipedFolder(gitHubProject);
		}

		//Print
		this.printFinish();
	}

	public void selectRandomProjects(int subsetSize) throws Exception {

		this.setupControllVariables();
		
		List<GitHubProject> gitHubProjects = repository.listAllGitHubProjects();

		//Print
		total = gitHubProjects.size();
		this.printTotalHeader(total);
		
	    Random random = new Random();
	    
		StringBuffer randomProjects = new StringBuffer();
	    for (int i = 0; i < subsetSize; i++) {
			int index = random.nextInt(total);
			randomProjects.append(gitHubProjects.get(index).getRepository()+"\n");
			System.out.println(gitHubProjects.get(index).getRepository());
	    }
		repository.saveRandomProjects(randomProjects);
		
	    //Print
		this.printFinish();
	}

	public void compareZipWithUnzipFolders() throws Exception {

		this.setupControllVariables();
		
		List<String> zipProjects = repository.getDownloadedProjects();
		List<String> unzipProjects = repository.getUnzipedFolderProjects();
		
		System.out.println("\nEstá no 'zipProjects', mas não no 'unzipProjects'\n");
		int zipNot = 0;
		for(String zipProject : zipProjects){
			zipProject = StringUtils.substringBeforeLast(zipProject, ".zip");
			if(!unzipProjects.contains(zipProject)){
				System.out.println(zipProject);
				zipNot++;
			}
		}
		System.out.println("\nEstá no 'zipProjects', mas não no 'unzipProjects' = " + zipNot + "\n");

		System.out.println("\nEstá no 'unzipProjects', mas não no 'zipProjects'\n");
		int unzipNot = 0;
		for(String unzipProject : unzipProjects){
			if(!zipProjects.contains(unzipProject+".zip")){
				System.out.println(unzipProject);
				unzipNot++;
			}
		}
		System.out.println("\nEstá no 'unzipProjects', mas não no 'zipProjects' = " + unzipNot + "\n");

		System.out.println("zipProjects = " + zipProjects.size());
		System.out.println("unzipProjects = " + unzipProjects.size());
		
		//Print
		this.printFinish();
	}

	public void convertToSorcererCrawledFormat() throws Exception {

		this.setupControllVariables();
		
		//1. Create crawled folder, if necessary
		repository.createCrawledFolder();

		//2. Get unzipped folders
		List<String> unzipProjects = repository.getUnzipedFolderProjects();

		//Print
		total = unzipProjects.size();
		this.printTotalHeader(total);
		
		
		//3. For each unzipped folders convertToSorcererCrawledFormat  
		int folderNumber = repository.getNextCrawledFolderNumber();
		for(String unzipProject : unzipProjects){
			
			System.out.println("\nConvertendo: " + unzipProject + " PARA " + folderNumber);
			repository.convertToSorcererCrawledFormat(unzipProject, folderNumber);
			folderNumber++;
			
			//Print
			this.printRecord(new GitHubProject(unzipProject));
		}
		
		//Print
		this.printFinish();
	}
	
	public void copyNotUnzipped() throws Exception {

		this.setupControllVariables();
		
		List<String> zipProjects = repository.getDownloadedProjects();
		List<String> unzipProjects = repository.getUnzipedFolderProjects();
		List<String> notUnzippedProjects = new ArrayList<String>();
		
		System.out.println("\nEstá no 'zipProjects', mas não no 'unzipProjects'\n");
		int zipNot = 0;
		for(String zipProject : zipProjects){
			String projectName = StringUtils.substringBeforeLast(zipProject, ".zip");
			if(!unzipProjects.contains(projectName)){
				System.out.println(zipProject);
				notUnzippedProjects.add(zipProject);
				zipNot++;
			}
		}
		System.out.println("\nEstá no 'zipProjects', mas não no 'unzipProjects' = " + zipNot + "\n");

		for(int i = 0; i < notUnzippedProjects.size(); i++){
			String notUnzippedProject = notUnzippedProjects.get(i);
			String msg = "Copiando " + (i+1) + "/" + notUnzippedProjects.size() + ": " + notUnzippedProject;
			System.out.println(msg);
			repository.copyZipedProject(notUnzippedProject);
		}
		
		System.out.println("zipProjects = " + zipProjects.size());
		System.out.println("unzipProjects = " + unzipProjects.size());
		
		//Print
		this.printFinish();
	}
	
	public void listAllNotDownloaded() throws Exception {

		this.setupControllVariables();
		
		List<GitHubProject> gitHubProjects = repository.listAllGitHubProjects();

		//Print
		total = gitHubProjects.size();
		this.printTotalHeader(total);
		
		List<String> downloadedProjects = repository.getDownloadedProjects();
		int totalNotDownloaded = 0;
		
		for(GitHubProject gitHubProject : gitHubProjects){
			if(!downloadedProjects.contains(gitHubProject.getDownloadedFileName())){
				System.out.println(gitHubProject.getRepository());
				totalNotDownloaded++;
			}
		}

		System.out.println("\nNão baixados: " + totalNotDownloaded);
		
		//Print
		this.printFinish();
	}
	
	public void listAllDownloadedButNotInList() throws Exception {

		this.setupControllVariables();
		
		List<String> downloadedProjects = repository.getDownloadedProjects();

		//Print
		total = downloadedProjects.size();
		this.printTotalHeader(total);
		
		List<String> projectsList = new ArrayList<String>();
		for(GitHubProject gitHubProject : repository.listAllGitHubProjects()){
			projectsList.add(gitHubProject.getDownloadedFileName());
		}
		int totalNotInList = 0;
		
		for(String downloadedProject : downloadedProjects){
			if(!projectsList.contains(downloadedProject)){
				System.out.println(downloadedProject);
				totalNotInList++;
				//repository.moveFile(downloadedProject);
			}
		}

		System.out.println("\nBaixados não listados: " + totalNotInList);
		
		//Print
		this.printFinish();
	}

	public void unzipProjects() throws Exception {

		this.setupControllVariables();
		
		List<String> downloadedProjects = repository.getDownloadedProjectsUnzipedFolder();

		//Print
		total = downloadedProjects.size();
		this.printTotalHeader(total);
		
		for(String zipProject : downloadedProjects){
			this.unzipProject(zipProject);
		}

		//Print
		this.printFinish();
	}

	private void unzipProject(String zipProject) throws Exception {

		String repo = zipProject.replace("~", "/");
		repo = StringUtils.substringBeforeLast(repo, ".zip");
		GitHubProject gitHubProject = new GitHubProject(repo);

		System.out.println("\n\nExtraindo " + gitHubProject.getDownloadedFileName() + "...");
		
		repository.unzipProject(gitHubProject);
		
		//Print
		this.printRecord(gitHubProject);
	}
	
	private void renameFile(GitHubProject gitHubProject) throws Exception {

		repository.renameFile(gitHubProject);
		
		//Print
		this.printRecord(gitHubProject);
	}

	private void deleteUnzipedFolder(GitHubProject gitHubProject) throws Exception {

		String folderName = gitHubProject.getRepository();
		System.out.println("\n\nDeletando " + folderName);
		repository.deleteUnzipedFolder(folderName);
		
		//Print
		this.printRecord(gitHubProject);
	}

	private void downloadGitHubRepository(GitHubProject gitHubProject) throws Exception {

		System.out.println("\n\nBaixando " + gitHubProject.getDownloadLink());

		repository.downloadGitHubProject(gitHubProject);
		
		System.out.println("Duração = " + gitHubProject.getDownloadDurationTime());

		//Print
		this.printRecord(gitHubProject);
		
		repository.setDownloadedGitHubProject(gitHubProject);
	}
	
	private void setupControllVariables() {
		this.total = 0;
		this.i = 1;
		this.duraction = 0;
		this.totalduraction = 0;
		this.timestamp = System.currentTimeMillis(); 
	}

	private void printTotalHeader(int total) throws Exception{
		System.out.println("\nTotal não processados: " + total + "\n");
	}
	
	private void printRecord(GitHubProject gitHubProject){
		duraction = System.currentTimeMillis() - timestamp;
		timestamp = System.currentTimeMillis();
		totalduraction += duraction;
		long average = (totalduraction/i);

		long estimateTime = average * (total-i);
		String estimateTimeMsg = "";
		if(estimateTime / 1000 < 60)
			estimateTimeMsg = estimateTime / 1000 + " seg";
		else if( estimateTime / 1000 / 60 < 60)
			estimateTimeMsg = estimateTime / 1000 / 60 + " min";
		else
			estimateTimeMsg = estimateTime / 1000 / 60 / 60 + " horas";
		
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String estimateEndMsg = format.format(new Date(timestamp + estimateTime));
		
		String recordMsg = "Processado: " + i + " de " + total;
		String timeMsg = " média = " + average + ", tempo estimado = " + estimateTimeMsg + ", conclusão estimada = " + estimateEndMsg;
		System.out.print(recordMsg + timeMsg + "\r");
		i++;
	}
	
	private void printFinish() throws Exception{
		System.out.println("\n\nProcessamento concluído!\n");
	}
}
