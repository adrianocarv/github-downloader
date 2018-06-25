package br.unifesp.ppgcc.githubdownloader.infrastructure.util;

import java.net.URL;
import java.util.Properties;

public class ConfigProperties {

	private static Properties properties;

	private ConfigProperties() {
	}

	static {
		properties = new Properties();
		URL url = ClassLoader.getSystemResource("config.properties");
		try {
	        properties.load(url.openStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String key) throws Exception {
		return properties.getProperty(key);
	}

	public static void setProperty(String key, String value) throws Exception {
		properties.setProperty(key, value);
	}
}