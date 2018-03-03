package com.ucpaas.sms.task.test.freemarker;

import freemarker.core.ParseException;
import freemarker.template.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Test {
	public static void main(String[] args) throws TemplateNotFoundException, MalformedTemplateNameException,
			ParseException, IOException, TemplateException, MessagingException {
		// Create your Configuration instance, and specify if up to what
		// FreeMarker
		// version (here 2.3.25) do you want to apply the fixes that are not
		// 100%
		// backward-compatible. See the Configuration JavaDoc for details.
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		cfg.setDirectoryForTemplateLoading(new File(Test.class.getResource("/templates").getFile()));

		// Set the preferred charset template files are stored in. UTF-8 is
		// a good choice in most applications:
		cfg.setDefaultEncoding("UTF-8");

		// Sets how errors will appear.
		// During web page *development*
		// TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		// Don't log exceptions inside FreeMarker that it will thrown at you
		// anyway:
		cfg.setLogTemplateExceptions(false);

		// Create the root hash. We use a Map here, but it could be a JavaBean
		// too.
		Map<String, Object> data = new HashMap<>();
		
		data = DataGenerator.generate();
 
		Template temp = cfg.getTemplate("ali-daily-template.ftl");

//		FileOutputStream fileOutput = new FileOutputStream(new File("C:/Users/bsmwhd2/Desktop/test.html"));
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(buffer);
		temp.process(data, out);
		
		String emailContent =  buffer.toString();
		out.close();
		
		ApplicationContext application = new ClassPathXmlApplicationContext("spring.xml");
		JavaMailSender javaMailSender = application.getBean(JavaMailSender.class);

		MimeMessage msg = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
		helper.setFrom("admin@ucpaas.com");
		helper.setTo("niutao@ucpaas.com");
		helper.setSubject("大客户阿里发送详情-6月-1日(测试)");
		helper.setText(emailContent, true);
		javaMailSender.send(msg);
		System.out.println("发送成功");
		
		
	}
}
