package com.ucpaas.sms.task.util.db;


import com.ucpaas.sms.task.enum4sms.DataSourceType;

public class HandleDataSource {

	public static final ThreadLocal<String> holder = new ThreadLocal<String>();

	public static void putDataSource(String datasource) {
		holder.set(datasource);
	}

	public static String getDataSource() {
		return holder.get();
	}

	public static void main(String[] args) {
		System.out.println(DataSourceType.READ);
	}
}
