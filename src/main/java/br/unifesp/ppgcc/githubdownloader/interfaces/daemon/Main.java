package br.unifesp.ppgcc.githubdownloader.interfaces.daemon;

public class Main {

	public static void main(String[] args) {

		if(args.length != 1 || (!args[0].equals("1") && !args[0].equals("2"))){
			System.out.println("\nPasse um número como parâmetro na execução da classe:");
			System.out.println("\n1 - GitHubDownloadMain");
			System.out.println("2 - UnzipProjectsMain");
			return;
		}
		
		char c = args[0].charAt(0);
		switch (c) {
		case '1':
			GitHubDownloadMain.main(null);
			break;
		case '2':
			UnzipProjectsMain.main(null);
			break;
		}
	}
 }