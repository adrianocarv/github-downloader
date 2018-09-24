package br.unifesp.ppgcc.githubdownloader.interfaces.daemon;

import br.unifesp.ppgcc.githubdownloader.application.GitHubDownloaderService;
import br.unifesp.ppgcc.githubdownloader.infrastructure.util.LogUtils;

public class ConvertToSorcererCrawledFormatMain {

	private static long startTime = -1;
	
	public static void main(String[] args) {
		setStartTime();
		
		LogUtils.getLogger().info("");
		LogUtils.getLogger().info("Aplicativo iniciado");
		LogUtils.getLogger().info("");
		
		try {

			LogUtils.getLogger().info("Service");
			GitHubDownloaderService service = new GitHubDownloaderService();

			//EXECUTE EXECUTE EXECUTE
			service.convertToSorcererCrawledFormat();

		} catch (Exception e) {
			LogUtils.getLogger().error(e);
			e.printStackTrace();
		}

		LogUtils.getLogger().info("");
		LogUtils.getLogger().info("Aplicativo finalizado. Tempo de execucao: " + getDuractionTime());
		LogUtils.getLogger().info("");
	}
	
	private static void setStartTime(){
		startTime = System.currentTimeMillis();
	}

	private static String getDuractionTime(){
		long duraction = System.currentTimeMillis() - startTime;
		
		duraction /= 1000;
		if(duraction < 60)
			return duraction + " segundos.";
		
		duraction /= 60;
		return duraction + " minutos.";
	}
 }