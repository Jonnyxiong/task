package com.ucpaas.sms.task.service;

import com.jsmsframework.channel.entity.JsmsChannelAttributeRealtimeWeight;
import com.jsmsframework.channel.entity.JsmsChannelAttributeWeightConfig;
import com.jsmsframework.channel.enums.ConfigExValueEnum;
import com.jsmsframework.channel.service.JsmsChannelAttributeRealtimeWeightService;
import com.jsmsframework.channel.service.JsmsChannelAttributeWeightConfigService;
import com.jsmsframework.common.util.BeanUtil;
import com.ucpaas.sms.task.dao.CommonDao;
import com.ucpaas.sms.task.dao.RecordSlaveDao;
import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateByClientid;
import com.ucpaas.sms.task.entity.stats.ChannelSuccessRateRealtime;
import com.ucpaas.sms.task.entity.stats.ClientSuccessRateRealtime;
import com.ucpaas.sms.task.mapper.stats.ChannelSuccessRateByClientidMapper;
import com.ucpaas.sms.task.mapper.stats.ChannelSuccessRateRealtimeMapper;
import com.ucpaas.sms.task.mapper.stats.ClientSuccessRateRealtimeMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.model.halfhour.*;
import com.ucpaas.sms.task.util.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sun.rmi.runtime.Log;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

@Service
public class RealTimeChannelSuccessRateWeightUpdateServiceImpl implements RealTimeChannelSuccessRateWeightUpdateService {

	private static final Logger logger = LoggerFactory.getLogger("RealTimeChannelSuccessRateWeightUpdateService");

	private static final long SLEEP_TIME = 5000; //5s



	@Autowired
	private CommonDao commonDao;
	@Autowired
	private RecordSlaveDao recordSlaveDao;

	@Autowired
	private ChannelSuccessRateRealtimeMapper channelSuccessRateRealtimeMapper;

	@Autowired
	private JsmsChannelAttributeRealtimeWeightService jsmsChannelAttributeRealtimeWeightService;

	@Autowired
	private JsmsChannelAttributeWeightConfigService jsmsChannelAttributeWeightConfigService;


	public RealTimeChannelSuccessRateWeightUpdateServiceImpl() throws IOException {
	}


	@Override
	public boolean doTheJob(TaskInfo taskInfo) throws ParseException, InterruptedException {
	//	long PERIOD_TIME =1800; //1800s
		int execute=1800;
			Map<String,Object> params=this.getSysParams("CHANNEL_SUCCESS_RATE_UPDATE");
		execute = Integer.valueOf(params.get("param_value").toString())*60;


		logger.info("——————————————————————【实时通道成功率区间权重更新任务】开始——————————————————————");
		//1.判断时间是否满足任务执行
		DateTime now = new DateTime();
		long start = System.currentTimeMillis();
		
		String timeFormart = taskInfo.getExecuteType().getFormat();
		DateTime executeNext = UcpaasDateUtils.parseDate(taskInfo.getExecuteNext(), timeFormart);
		logger.info("*数据时间是:{}",executeNext.toString("yyyy-MM-dd HH:mm:ss"));
		logger.info("*执行时间是:{}",now.toString("yyyy-MM-dd HH:mm:ss"));
		String m = execute+"s执行一次";
		if(isEndDay(executeNext)){
			logger.info("执行当天23:59,时间满足，任务继续");
		}else if((executeNext.getMillis()/1000%execute)!=0){
			logger.info(m+"时间不满足，任务结束");
			return true;
		}else{
			logger.info(m+"时间满足，任务继续");
		}

		//2.取出对应时间的通道成功率数据
		String date = executeNext.toString("yyyyMMdd");
		String nowtime=executeNext.toString("yyyy-MM-dd HH:mm:ss");
		String before=UcpaasDateUtils.parseDate(nowtime, "yyyy-MM-dd HH:mm:ss").minusSeconds(execute).toString("yyyy-MM-dd HH:mm:ss");
		// 根据indentify和时间遍历统计十张流水表中的用户成功率数据
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		List<ChannelSuccessRateRealtime> allTableDataList = new ArrayList<ChannelSuccessRateRealtime>(); // 10张record表的统计数据
		List<ChannelSuccessRateRealtime> oneTableDataList = new ArrayList<ChannelSuccessRateRealtime>(); // 1张流水表的统计数据
		for (int identify = 0; identify < 10; identify++) {
			sqlParams.put("identify", identify);
			sqlParams.put("date", date);
			if(isEndDay(executeNext)|| isStartDay(executeNext)){

			}else {
				sqlParams.put("beforeTime", before);
				sqlParams.put("nowTime", nowtime);
			}
			oneTableDataList = recordSlaveDao.queryAll("smsMonitor.getChannelSuccessRate4weight", sqlParams);
			allTableDataList.addAll(oneTableDataList);
		}


		//3.通过通道
		Integer sNum=0;
		Integer	fNum=0;
		Integer	eNum=0;

		for (ChannelSuccessRateRealtime rate : allTableDataList) {
			JsmsChannelAttributeRealtimeWeight record=new JsmsChannelAttributeRealtimeWeight();
			JsmsChannelAttributeRealtimeWeight weight=jsmsChannelAttributeRealtimeWeightService.getByChannelId(Integer.valueOf(rate.getChannelId()));
			if(null!=weight){

					record.setSuccessRate(rate.getSuccessRate());
					record.setId(weight.getId());
					record.setUpdator(0L);
					record.setUpdateDate(new Date());
				if((rate.getSuccessRate()).compareTo(weight.getSuccessRate())!=0){
					List<JsmsChannelAttributeWeightConfig> configs=jsmsChannelAttributeWeightConfigService.queryAllBySmSType();

						for (JsmsChannelAttributeWeightConfig config : configs) {
							if((rate.getSuccessRate()).compareTo(config.getStartLine())!=-1 && (rate.getSuccessRate()).compareTo(config.getEndLine())==-1){
								if(Objects.equals(ConfigExValueEnum.验证码.getValue(), config.getExValue())){
									record.setYzSuccessWeight(config.getWeight());
								}else if(Objects.equals(ConfigExValueEnum.通知.getValue(), config.getExValue())){
									record.setTzSuccessWeight(config.getWeight());
								}else if(Objects.equals(ConfigExValueEnum.营销.getValue(), config.getExValue())){
									record.setYxSuccessWeight(config.getWeight());
								}else  if(Objects.equals(ConfigExValueEnum.告警.getValue(),config.getExValue())){
									record.setGjSuccessWeight(config.getWeight());
								}
							}else if((rate.getSuccessRate()).compareTo(new BigDecimal(100)) ==0  && config.getEndLine().compareTo(rate.getSuccessRate())==0){
								if(Objects.equals(ConfigExValueEnum.验证码.getValue(), config.getExValue())){
									record.setYzSuccessWeight(config.getWeight());
								}else if(Objects.equals(ConfigExValueEnum.通知.getValue(), config.getExValue())){
									record.setTzSuccessWeight(config.getWeight());
								}else if(Objects.equals(ConfigExValueEnum.营销.getValue(), config.getExValue())){
									record.setYxSuccessWeight(config.getWeight());
								}else  if(Objects.equals(ConfigExValueEnum.告警.getValue(),config.getExValue())){
									record.setGjSuccessWeight(config.getWeight());
								}

							}

						}
					}
					int u=jsmsChannelAttributeRealtimeWeightService.updateSelective(record);

					if(u>0){
						sNum++;
						logger.debug("实时更新半小时成功率及区间权重成功,通道记录为{}",JsonUtils.toJson(rate));
					}else {
						fNum++;
						logger.debug("实时更新半小时成功率及区间权重失败,通道记录为{}",JsonUtils.toJson(rate));
					}


			}else {
				eNum++;
				logger.error("存在异常通道号,在实时成功率区间权重不存在对应通道号 ,记录为:{}", JsonUtils.toJson(rate));
			}

		}


		long end = System.currentTimeMillis();
		logger.info("————————————————————————————————实时更新短信通道成功率及区间更新结果为:成功{}个,失败{}个,异常情况为{}个————————————————————————————————",sNum,fNum,eNum);
		logger.info("【实时更新短信通道成功率及区间任务】结束，统计时间= {}，统计耗时={}ms", executeNext.toString("yyyy-MM-dd HH:mm:ss"), (end - start));
		
		return true;
		
	}


	/**
	 * 获取系统参数
	 */
	public Map<String,Object> getSysParams(String paramKey){
		return commonDao.getUcpaasMessageDao().getOneInfo("common.getSysParam", paramKey);
	}


	public  boolean isEndDay(DateTime time){
		if(time.getHourOfDay()==23 && time.getMinuteOfHour()==59){
			return true;
		}
		return false;

	}
	public  boolean isStartDay(DateTime time){
		if(time.getHourOfDay()==0 && time.getMinuteOfHour()==0 && time.getSecondOfDay()==0){
			return true;
		}
		return false;

	}
}
