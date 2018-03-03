package com.ucpaas.sms.task.util;

public class FilePathUtils {
	
	
	/** 
	 * @Title: getClassPath 
	 * @Description: 获取类路径
	 * @return
	 * @return: String
	 */
	public static String getClassPath(){
		String path = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
		return path;
	}

}
