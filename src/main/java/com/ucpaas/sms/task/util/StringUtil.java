package com.ucpaas.sms.task.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	public static boolean isEmpty(String s) {
		return null == s || "".equals(s.trim());
	}

	public static boolean isNotEmpty(String s) {
		return !isEmpty(s);
	}

	public static boolean isEmpty(StringBuffer sb) {
		return null == sb || sb.length() == 0 || "".equals(sb.toString().trim());
	}

	public static String trim(String s) {
		return s == null ? "" : s.trim();
	}

	public static String getHostFromURL(String url){
		String host = "";
		Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
		Matcher matcher = p.matcher(url);
		if (matcher.find()) {
			host = matcher.group();
		}
		return host;
	}
}
