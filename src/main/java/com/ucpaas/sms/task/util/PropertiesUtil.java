package com.ucpaas.sms.task.util;

import java.io.IOException;
import java.util.Map;

public class PropertiesUtil {

	
	private static final String fileName = "/application.properties";
	
	public static String get(String key) throws IOException {
		Map<String, String> prop = FileUtils.readProperties(fileName);
		return prop.get(key);
	}
	
	
	public static void put(String key, String value) throws IOException {
		Map<String, String> prop = FileUtils.readProperties(fileName);
		prop.put(key, value);
		FileUtils.wirteProperties(fileName, prop);
	}
}
