package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
public class SmsSaleStatMailReportServiceImplTest {

    @Autowired
    private SmsSaleStatMailReportService smsSaleStatMailReportService;

    @Test
    public void statReportAndSend() throws Exception {
        System.err.println(smsSaleStatMailReportService.saleReportAndSend(new TaskInfo()));
    }

}
