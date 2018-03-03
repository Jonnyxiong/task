package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.dao.AccessMasterDao;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.service.common.CommonService;
import com.ucpaas.sms.task.util.ConfigUtils;
import com.ucpaas.sms.task.util.DateUtils;
import com.ucpaas.sms.task.util.JsonUtils;
import com.ucpaas.sms.task.util.UcpaasDateUtils;

/**
 * 代理商业务
 */
@Service
public class AgentTaskServiceImpl implements AgentTaskService {
	private static final Logger logger = LoggerFactory.getLogger(AgentTaskServiceImpl.class);
	@Autowired
	private MessageMasterDao messageMasterDao;
	@Autowired
	private AccessMasterDao accessMasterDao;
	@Autowired
	private CommonService commonService;
	
	private static final int batch = 1000; // 同步数据批处理大小

	/**
	 * 代理商佣金计算
	 */
	@Override
	public boolean commissionCompute(TaskInfo taskInfo) {
		String formart = taskInfo.getExecuteType().getFormat();
		DateTime executePrev = UcpaasDateUtils.parseDate(taskInfo.getExecutePrev(), formart);
		DateTime commisionExecuteTime = UcpaasDateUtils.parseDate(taskInfo.getExecutePrev(), formart).minusDays(2);
		String commisionExecuteTimeStr = commisionExecuteTime.toString("yyyy-MM-dd");
		taskInfo.setExecutePrev(commisionExecuteTime.toString(formart));
		
		logger.info("【代理商佣金计算】- 开始, 计算佣金日期 = {}" + commisionExecuteTimeStr);
		
		if(!syncAccessStat(taskInfo)){
			logger.info("【代理商佣金计算，代理商数据为空】，"+taskInfo.getExecutePrev()+"日的access客户运维运营报表数据为空");
			return true;
		}else{
			taskInfo.setProcedureName("p_agent_commision_compute");
			
			boolean result = commonService.callProcedure(taskInfo);
			taskInfo.setExecutePrev(executePrev.toString(formart));
			
			logger.info("【代理商佣金计算】- 结束, 计算佣金日期 = {}" + commisionExecuteTimeStr);
			return result;
		}
		
		
	}
	
	/**
	 * 同步t_sms_access_channel_statistics表中date等于统计当天的数据
	 * @param taskInfo
	 * @return
	 */
	private boolean syncAccessStat(TaskInfo taskInfo){
		logger.debug("【代理商佣金计算】- 开始同步数据");
		String executePrev = taskInfo.getExecutePrev();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		if(executePrev == null){
			executePrev = df.format(new Date());
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("date", executePrev);
		
		List<Map<String, Object>> accessStatList = accessMasterDao.getSearchList("agentTask.getAccessStat", params);
		int count = accessStatList.size();
		logger.debug("【代理商佣金计算】- 同步ACCESS从库客户运营统计表中{}条数据", count);
		if (count < 1) {
			logger.debug("【代理商佣金计算】- 当天无数据可同步");
			return false;
		}else{
			messageMasterDao.delete("agentTask.deleteAccessStat", params);
			Map<String, Object> sqlParams = new HashMap<String, Object>();

			for (int i = 0; i < count; i += batch) {
				sqlParams.put("accessStatList", accessStatList.subList(i, Math.min(i + batch, count)));
				messageMasterDao.insert("agentTask.insertAccessStat", sqlParams);
			}
		}
		
		logger.debug("【代理商佣金计算】- 结束同步数据");
		return true;
	}

	/**
	 * 代理商季度返利结算
	 */
	@Override
	public boolean rebateCompute(TaskInfo taskInfo) {
		long begin = System.currentTimeMillis();
		Calendar c = Calendar.getInstance();
		logger.debug("start-->rebateCompute,开始执行代理商季度返利，日期:{}"+c.getTime());
		logger.debug("system.properties配置了每月{}号进行对上个季度结算并进行返利",ConfigUtils.session_day);
		int session_day = 3;
		try{
			session_day = Integer.parseInt(ConfigUtils.session_day);
		}catch(Exception e){
			logger.error("system.properties配置的session_day不正确，因为数字，实际配置为session_day="+session_day, e);
			logger.debug("代理商季度返利结束，用时{}ms",(System.currentTimeMillis()-begin));
			return true;
		}
		int datenum=c.get(Calendar.DATE);
		if(datenum!=session_day){
			logger.debug("不到指定日期，所以不进行代理商季度返利结算");
			logger.debug("代理商季度返利结束，用时{}ms",(System.currentTimeMillis()-begin));
			return true;
		}
		int seasonNow = DateUtils.getSeason(c.getTime());
		int yearNow = c.get(Calendar.YEAR);
		int sessonPre = seasonNow - 1 ;  //上个季度
		int yearPreOrNow = yearNow; //有可能是去年
		if(sessonPre == 0){ //去年
			sessonPre = 4;
			yearPreOrNow = yearNow - 1;
		}
		logger.debug("今天属于{}第{}季度，开始统计上一个季度（{}第{}季度）",yearNow,seasonNow,yearPreOrNow,sessonPre);
		Calendar sessonBeginTime = Calendar.getInstance();
		Calendar sessonEndTime = Calendar.getInstance();
		String beginMonth = null ;
		String endMonth = null ;
		switch (sessonPre) {
			case 1://1月至3月
				sessonBeginTime.set(yearPreOrNow, 1, 1, 0, 0, 0);  
				sessonEndTime.set(yearPreOrNow, 3, 31, 23, 59, 59);
				beginMonth=yearPreOrNow+"01";
				endMonth=yearPreOrNow+"03";
				break;
			case 2://4月至6月
				sessonBeginTime.set(yearPreOrNow, 4, 1, 0, 0, 0);
				sessonEndTime.set(yearPreOrNow, 6, 30, 23, 59, 59);
				beginMonth=yearPreOrNow+"04";
				endMonth=yearPreOrNow+"06";
				break;
			case 3://7月至9月
				sessonBeginTime.set(yearPreOrNow, 7, 1, 0, 0, 0);
				sessonEndTime.set(yearPreOrNow, 9, 30, 23, 59, 59);
				beginMonth=yearPreOrNow+"07";
				endMonth=yearPreOrNow+"09";
				break;
			case 4://10月至12月
				sessonBeginTime.set(yearPreOrNow, 10, 1, 0, 0, 0);
				sessonEndTime.set(yearPreOrNow, 12, 31, 23, 59, 59);
				beginMonth=yearPreOrNow+"10";
				endMonth=yearPreOrNow+"12";
				break;
			default:
				break;
		}
		logger.debug("开始统计{}第{}季度,时间:{}至{}",yearPreOrNow,sessonPre,DateUtils.dateToStrDefault(sessonBeginTime.getTime()),DateUtils.dateToStrDefault(sessonEndTime.getTime()));
		
		//统一备注，查看是否返利过，所以不能随便修改此remark变量
		String remark = "###"+yearPreOrNow+"第"+sessonPre+"季度###"; //不能随便修改
		//不能随便修改
		Map existMap = new HashMap<>();
		existMap.put("remark", remark);
		List<Map<String, Object>> isExistRebate = messageMasterDao.getSearchList("agentTask.isExistRebate", existMap);
		if(isExistRebate!=null&&isExistRebate.size()>0&&"false".equalsIgnoreCase(ConfigUtils.repeat_rebate_compute)){
			logger.debug("已存在上个季度的返利，且配置不可重跑repeat_rebate_compute=false，remark={}",remark);
			logger.debug("代理商季度返利结束，用时{}ms",(System.currentTimeMillis()-begin));
			return true;
		}
		
	 
		
		
		
//		List<Integer> agentIdList = messageMasterDao.getOneInfo("agentTask.getAgentIdList", null);
		List<Map<String, Object>> agentList = messageMasterDao.getSearchList("agentTask.getAgentList", null);
		
		/**
		 * begin
		 * 重跑逻辑，不应该有，只是方便测试使用而已
		 * 线上重跑，涉及到业务，历史流水保不保留，重跑流水该不该记。账户金额变更，怎么跟用户解释。 重跑依赖数据是账单表数据和返点比例，可能造成两次跑的金额不一致
		 */
		boolean repeat = false;
		if(isExistRebate!=null&&isExistRebate.size()>0&&"true".equalsIgnoreCase(ConfigUtils.repeat_rebate_compute)){
			logger.debug("已存在上个季度的返利，且配置可以重跑repeat_rebate_compute=true，remark={}",remark);
			logger.debug("现在开始重跑，删掉历史流水，金额发生改变");
			repeat = true;
			Map historyAmount = new HashMap<>();
			for(Map<String, Object> map:isExistRebate){
				Integer agentId =(Integer) map.get("agent_id");
				BigDecimal amount =(BigDecimal) map.get("amount");
				historyAmount.put(agentId, amount);
			}
			for(Map<String, Object> map:agentList){
				Integer agentId =(Integer) map.get("agent_id");
				if(historyAmount.get(agentId)==null){
					continue;
				}
				Map deleteBillParams = new HashMap<>();
				deleteBillParams.put("agentId", agentId);
				deleteBillParams.put("remark", remark);
				logger.debug("删除agent_id={}的返点流水t_sms_agent_rebate_bill",agentId);
				messageMasterDao.delete("agentTask.deleteAgentRebateBill", deleteBillParams);
				deleteBillParams.put("amount", historyAmount.get(agentId));
				logger.debug("扣减agent_id={}的账户t_sms_agent_account，金额amount=",historyAmount.get(agentId));
				messageMasterDao.update("agentTask.minusAgentAcount", deleteBillParams);
			}
			logger.debug("删除和扣减完成");
		}
		/**
		 * end
		 * 重跑逻辑，不应该有，只是方便测试使用而已
		 * 线上重跑，涉及到业务，历史流水保不保留，重跑流水该不该记。账户金额变更，怎么跟用户解释。 重跑依赖数据是账单表数据和返点比例，可能造成两次跑的金额不一致
		 */
		
		
		Map<Integer,Integer> agentTypeMap = new HashMap<>();
		List<Integer> agentIdList = new ArrayList<>();
		for(Map<String,Object> map:agentList){
			agentIdList.add((Integer) map.get("agent_id"));
			agentTypeMap.put((Integer)map.get("agent_id"), (Integer)map.get("agent_type"));
		}
		
		Map<String,Object> consumeParam = new HashMap<>();
		consumeParam.put("beginMonth", beginMonth);
		consumeParam.put("endMonth", endMonth);
		consumeParam.put("agentIdList",agentIdList);
		List<Map<String, Object>> agentLastQuarterConsumeList = accessMasterDao.getSearchList("agentTask.getAgentLastSeasonConsume", consumeParam);
		
		List<Map<String, Object>> priceMap = messageMasterDao.getSearchList("agentTask.getOneSmsPrice", null);
		if(priceMap==null||priceMap.size()==0||priceMap.get(0).get("param_value")==null){
			logger.error("代理商和客户属性配置的OEM_AGENT_REBATE_PRICE 的值错误，请检查");
			logger.error("代理商季度返利结束，用时{}ms");
			logger.debug("遇到错误，代理商季度返利结束，用时{}ms",(System.currentTimeMillis()-begin));
			return false;
		}
		String priceStr = (String) (priceMap.get(0).get("param_value")==null?"0":priceMap.get(0).get("param_value"));
		BigDecimal perSmsPrice = new BigDecimal(priceStr);
		
		for(Map<String,Object> map:agentLastQuarterConsumeList){
			Integer agentId =(Integer) map.get("agent_id");
			Integer agentType = agentTypeMap.get(agentId);
			BigDecimal seasonConsume = (BigDecimal) map.get("seasonConsume");
			BigDecimal chargetotalSum = (BigDecimal) map.get("chargetotalSum");
			logger.debug("代理商agent_id={},agentType={},seasonConsume={},chargetotalSum={}",agentId,agentType,seasonConsume,chargetotalSum);
			BigDecimal rebateAmount = BigDecimal.ZERO;
			BigDecimal rebate_proportion = BigDecimal.ZERO;
			//品牌代理商
			if(agentType==2){
				Map<String, Object> sqlParams = new HashMap<>();
				sqlParams.put("seasonConsume", seasonConsume);
				List<Map<String,Object>> rebateMap = messageMasterDao.getSearchList("agentTask.getAgentRebate", sqlParams);
				if(rebateMap==null||rebateMap.size()==0){
					logger.debug("代理商agent_id={}，找不到合适返点比例，将跳过该代理商，执行下一个代理商",agentId);
					continue;
				}
				rebate_proportion = (BigDecimal) rebateMap.get(0).get("rebate_proportion");
				rebateAmount = seasonConsume.multiply(rebate_proportion);
				
			}
			//OEM代理商
			if(agentType==5){
				Map<String, Object> sqlParams = new HashMap<>();
				sqlParams.put("chargetotalSum", chargetotalSum);
				List<Map<String,Object>> rebateMap = messageMasterDao.getSearchList("agentTask.getOemAgentRebate", sqlParams);
				if(rebateMap==null||rebateMap.size()==0){
					logger.debug("代理商agent_id={}，找不到合适返点比例，将跳过该代理商，执行下一个代理商",agentId);
					continue;
				}
				rebate_proportion = (BigDecimal) rebateMap.get(0).get("rebate_proportion");
				rebateAmount = chargetotalSum.multiply(perSmsPrice).multiply(rebate_proportion);
			}
			
			logger.debug("代理商agent_id={},perSmsPrice={},返点比例rebate_proportion={},返利rebateAmount={}",agentId,perSmsPrice,rebate_proportion,rebateAmount);
			
			//更新代理商信息表的 返点使用比例
			Map agentParams = new HashMap<>();
			agentParams.put("rebate_proportion", rebate_proportion);
			agentParams.put("agentId", agentId);
			messageMasterDao.update("agentTask.updateUseRebate", agentParams);
			logger.debug("成功更新代理商信息表的 返点使用比例",JsonUtils.toJson(map));
			
			//更新代理商账户表的 代理商账户表和累计返点收入（元）
			agentParams.put("rebateAmount", rebateAmount);
			agentParams.put("agentId", agentId);
			messageMasterDao.update("agentTask.updateAgentAcount", agentParams);
			logger.debug("成功更新代理商账户表的 代理商账户表和累计返点收入（元）,{}",JsonUtils.toJson(map));
			
			//增加返点流水
			agentParams.put("remark", remark);
			messageMasterDao.insert("agentTask.insertAgentRebateBill", agentParams);
			logger.debug("成功增加流水,{}",JsonUtils.toJson(map));
			
		}

		logger.debug("end-->rebateCompute,结束代理商季度返利，用时{}ms",(System.currentTimeMillis()-begin));
		
		return true;
	}
	
	
	
}
