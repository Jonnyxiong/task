package com.ucpaas.sms.task.aop;


import com.ucpaas.sms.task.enum4sms.DataSourceType;
import com.ucpaas.sms.task.util.db.HandleDataSource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Order(1)
@Component
public class DataSourceAspect {

	private static Logger log = LoggerFactory.getLogger(DataSourceAspect.class);

	@Pointcut("@annotation(com.ucpaas.sms.task.aop.DataSource)")
	public void pointCut() {
	};

	@Before(value = "pointCut()")
	public void before(JoinPoint point) {

		Object target = point.getTarget();
		String method = point.getSignature().getName(); 
		Class<?> clazz = target.getClass();
		Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
		try {
			Method m = clazz.getMethod(method, parameterTypes);
			if (m != null && m.isAnnotationPresent(DataSource.class)) {
				DataSource data = m.getAnnotation(DataSource.class);
				log.debug("设置数据源为" + data.value().name());
				HandleDataSource.putDataSource(data.value().name());
			} else {
				log.error("设置数据源失败,将使用默认的数据源");
			}

		} catch (Exception e) {
			log.error("设置数据源失败,将使用默认的数据源", e);
		}
	}

	@After(value = "pointCut()")
	public void after() {
		log.debug("设置默认数据源为WRITE");
		HandleDataSource.putDataSource(DataSourceType.WRITE.name());
	}

	@AfterThrowing(value = "pointCut()")
	public void afterThrowing() {
		log.debug("设置默认数据源为WRITE");
		HandleDataSource.putDataSource(DataSourceType.WRITE.name());
	}

}
