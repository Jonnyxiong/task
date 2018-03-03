package com.ucpaas.sms.task.util;

import com.ucpaas.sms.task.constant.AlarmConstant;
import com.ucpaas.sms.task.entity.AccessSmsBO;
import com.ucpaas.sms.task.util.encrypt.EncryptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dylan on 2017/8/9.
 */
public class SendSMSUtil {
    private static final Logger logger = LoggerFactory.getLogger(SendSMSUtil.class);


    /**
     * 告警短信发送
     * @param template 短信模板
     * @param mobiles 以英文逗号隔开
     * @param params 模板对应参数
     */
    public static void sendAlarmSMS(String template,String mobiles,List<String> params){

        AccessSmsBO smsModel = new AccessSmsBO();
        // 通过超频校验开始发送短信
        smsModel.setClientid(ConfigUtils.sms_alarm_clientid);
        smsModel.setPassword(EncryptUtils.encodeMd5(ConfigUtils.sms_alarm_paasword));
        smsModel.setMobile(mobiles);
        smsModel.setContent(convertTemplate(template,params));
        smsModel.setSmstype(AlarmConstant.SmsType.NOTIFY.getValue());

        String resultJson;
        String smsp_access_url = ConfigUtils.smsp_access_url.replace("{clientid}", smsModel.getClientid());
        logger.info("-------------smsp_access_url---------->{}", smsp_access_url);
        if (smsp_access_url.startsWith("https")) {
            logger.debug("使用https协议请求短信接口");
            // 线上
            resultJson = HttpUtils.httpPost(smsp_access_url, JsonUtils.toJson(smsModel), true);
        } else {
            logger.debug("使用http协议请求短信接口");
            resultJson = HttpUtils.httpPost(smsp_access_url, JsonUtils.toJson(smsModel), false);
        }
        logger.debug("告警短信响应 --> result = {}",resultJson);
    }

    /**
     * @param template 短信模板
     * @param params 模板参数
     * @return 转换后的短信内容
     */
    public static String convertTemplate(String template,List<String> params){

        Pattern r = Pattern.compile("\\{[^\\}]*\\}");
        Matcher matcher = r.matcher(template);
        int count = 0;
        while(matcher.find()) {
            template = template.replaceFirst(r.toString(), params.get(count));
            ++count;
        }
        logger.debug("即将发送的短信内容 --> {}",template);
        return template;
    }
}
