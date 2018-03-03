package com.ucpaas.sms.task.service;


import com.jsmsframework.common.util.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
public class RecordChannelTempDataStatisticsServiceImplTest {

    @Autowired
    private RecordChannelTempDataStatisticsService recordChannelTempDataStatisticsService;

    @Test
    public void generateData() throws Exception {
        System.err.println(JsonUtil.toJson(recordChannelTempDataStatisticsService));
    }


}
