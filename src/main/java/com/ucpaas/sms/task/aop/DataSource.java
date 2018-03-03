package com.ucpaas.sms.task.aop;


import com.ucpaas.sms.task.enum4sms.DataSourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DataSource {
	/**
	 * 
	 * 参考{@link com.ucpaas.sms.task.enum4sms.DataSourceType}
	 * 
	 * @return
	 * 
	 */
	DataSourceType value();

}
