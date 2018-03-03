package com.ucpaas.sms.task.service;

import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring.xml"})
public class BalanceAlarmDoServiceTest {

    @Autowired
    private BalanceAlarmDoService balanceAlarmDoService;

    @Test
    public void doAlarmOemClient() throws Exception {
        JsmsTaskAlarmSetting taskAlarmSetting = new JsmsTaskAlarmSetting();
        taskAlarmSetting.setTaskAlarmPhone("18320829501");
        taskAlarmSetting.setTaskAlarmAmount(new BigDecimal(1));
        taskAlarmSetting.setCreateTime(new Date());
        taskAlarmSetting.setWebId(3);
        taskAlarmSetting.setStatus(1);
        taskAlarmSetting.setUserAlarmContent("【余额提醒】您好，账户“{客户ID}-{客户名称}”的{短信类型}短信剩余量已低于{}{}，请及时充值。");
        taskAlarmSetting.setSaleAlarmContent("【余额提醒】您的客户“{客户ID}（{客户名称}）”，当前{短信类型}短信余额为{}，请及时跟进充值确保使用通畅。");
        taskAlarmSetting.setTaskAlarmContent("【余额提醒】{销售名字}的客户&ldquo;{客户ID}（{客户名称}）&rdquo;，当前{短信类型}短信余额为{}，请及时跟进充值确保使用通畅。");
        taskAlarmSetting.setTaskAlarmType(0);
        taskAlarmSetting.setTaskAlarmFrequecy(1);
        taskAlarmSetting.setBeginTime("07:00");
        taskAlarmSetting.setEndTime("23:00");
        boolean b =  balanceAlarmDoService.doAlarmOemClient(DateTime.now() , taskAlarmSetting);
        System.err.println(b);
    }

}
