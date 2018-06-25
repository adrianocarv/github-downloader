package br.unifesp.ppgcc.githubdownloader.infrastructure.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import br.unifesp.ppgcc.githubdownloader.domain.GitHubProject;

/**
 * A utility that downloads a file from a URL.
 * 
 * @author www.codejava.net
 *
 */
public class HttpDownloadUtility {
	private static final int BUFFER_SIZE = 4096;

	/**
	 * Downloads a file from a URL
	 * 
	 * @param fileURL
	 *            HTTP URL of the file to be downloaded
	 * @param saveDir
	 *            path of the directory to save the file
	 * @throws IOException
	 */
	public static long downloadFile(GitHubProject gitHubProject, String saveDir) throws IOException {
		String saveFilePath = "";
		long contentLength = -1;
	
		String fileURL = gitHubProject.getDownloadLink();
		URL url = new URL(fileURL);
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		
		httpConn.setConnectTimeout(7000);
		
		int responseCode = httpConn.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");
			String contentType = httpConn.getContentType();
			contentLength = httpConn.getContentLength();

			if (disposition != null) {
				// extracts file name from header field
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(index + 9, disposition.length());
				}
			} else {
				// extracts file name from URL
				fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
			}

			fileName = gitHubProject.getDownloadedFileName();
			
			System.out.println("Content-Type = " + contentType);
			System.out.println("Content-Disposition = " + disposition);
			System.out.println("Content-Length = " + contentLength + " = " + readableFileSize(contentLength));
			System.out.println("fileName = " + fileName);

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();
			saveFilePath = saveDir + File.separator + fileName;

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(saveFilePath);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.close();
			inputStream.close();

			System.out.println("File downloaded");
		} else {
			System.out.println("No file to download. Server replied HTTP code: " + responseCode);
		}
		httpConn.disconnect();
		
		if(contentLength == -1){
			contentLength = new File(saveFilePath).length();
		}
		
		return contentLength;
	}
	
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}	
}