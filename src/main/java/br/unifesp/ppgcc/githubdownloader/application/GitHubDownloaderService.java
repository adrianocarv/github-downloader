package br.unifesp.ppgcc.githubdownloader.application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

	public void selectRandomProjects(int subsetSize) throws Exception {

		this.setupControllVariables();
		
		List<GitHubProject> gitHubProjects = repository.listAllGitHubProjects();

		//Print
		total = gitHubProjects.size();
		this.printTotalHeader(total);
		
	    Random random = new Random();
	    
	    for (int i = 0; i < subsetSize; i++) {
			int index = random.nextInt(total);
			System.out.println(gitHubProjects.get(index).getRepository());
	    }

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
			if(!isDownloadedProject(gitHubProject, downloadedProjects)){
				System.out.println(gitHubProject.getRepository());
				totalNotDownloaded++;
			}
		}

		System.out.println("\nNão baixados: " + totalNotDownloaded);
		
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

	private boolean isDownloadedProject(GitHubProject gitHubProject, List<String> downloadedProjects) throws Exception {

		return downloadedProjects.contains(gitHubProject.getDownloadedFileName());
	}

	private void renameFile(GitHubProject gitHubProject) throws Exception {

		repository.renameFile(gitHubProject);
		
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
	
	private void unzipProject(String zipProject) throws Exception {

		String repo = zipProject.replace("~", "/").replaceFirst(".zip", "");
		GitHubProject gitHubProject = new GitHubProject(repo);

		System.out.println("\n\nExtraindo " + gitHubProject.getDownloadedFileName() + "...");
		
		repository.unzipProjectAndDeleteZipFile(gitHubProject);
		
		//Print
		this.printRecord(gitHubProject);
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
