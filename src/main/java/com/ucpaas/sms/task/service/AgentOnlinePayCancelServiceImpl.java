package com.ucpaas.sms.task.service;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.entity.JsmsParam;
import com.jsmsframework.common.enums.PaymentState;
import com.jsmsframework.common.service.JsmsParamService;
import com.jsmsframework.finance.entity.JsmsOnlinePayment;
import com.jsmsframework.finance.service.JsmsOnlinePaymentService;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.mapper.onlinepay.JsmsOnlinePayMapper;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.ConfigUtils;
import com.ucpaas.sms.task.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 支付取消
 */
@Service
@Transactional
public class AgentOnlinePayCancelServiceImpl implements AgentOnlinePayCancelService {


	private static final Logger logger = LoggerFactory.getLogger(AgentOnlinePayCancelServiceImpl.class);
	private static final String ONLINE_PAYMENT_OVERTIME = "ONLINE_PAYMENT_OVERTIME";
	@Autowired
	private JsmsOnlinePaymentService jsmsOnlinePaymentService;

	@Autowired
	private JsmsOnlinePayMapper jsmsOnlinePayMapper;

	@Autowired
	private JsmsParamService jsmsParamService;

	@Override
	public boolean execute(TaskInfo taskInfo) {
		logger.info("支付取消任务执行");
        JsmsPage jsmsPage=new JsmsPage();
		/*List<JsmsParam> jsmsParams=jsmsParamService.getByParamKey(ONLINE_PAYMENT_OVERTIME);
		String cancelTime="";
		if(CollectionUtils.isEmpty(jsmsParams)){
			cancelTime="15";
		}else {

			cancelTime=jsmsParams.get(0).getParamValue().split("\\|")[0];
		}*/
        jsmsPage.getParams().put("submitTimeStart", DateUtils.getBeforeMinite(Long.valueOf(1)));
        List<JsmsOnlinePayment> jsmsOnlinePayments=jsmsOnlinePayMapper.queryCancelList(jsmsPage);
		logger.info("查询到{}调数据",jsmsOnlinePayments.size());
        for(JsmsOnlinePayment jsmsOnlinePayment:jsmsOnlinePayments){
			logger.info("一条数据:{}-:-{}",jsmsOnlinePayment.getPaymentId(),jsmsOnlinePayment.getPaymentState());
            if(jsmsOnlinePayment.getPaymentState().equals(PaymentState.WEI_ZHI_FU.getValue())) {
				logger.info("{}订单支付取消",jsmsOnlinePayment.getPaymentId());
                jsmsOnlinePayment.setPaymentState(PaymentState.CANCEL.getValue());
                jsmsOnlinePaymentService.updateSelective(jsmsOnlinePayment);
            }
        }
		return true;
	}
}
