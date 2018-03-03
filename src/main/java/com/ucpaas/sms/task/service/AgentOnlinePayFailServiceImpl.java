package com.ucpaas.sms.task.service;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.entity.JsmsParam;
import com.jsmsframework.common.enums.PaymentState;
import com.jsmsframework.common.service.JsmsParamService;
import com.jsmsframework.common.util.DateUtil;
import com.jsmsframework.finance.entity.JsmsOnlinePayment;
import com.jsmsframework.finance.service.JsmsOnlinePaymentLogService;
import com.jsmsframework.finance.service.JsmsOnlinePaymentService;
import com.ucpaas.sms.task.dao.MessageMasterDao;
import com.ucpaas.sms.task.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.task.entity.access.CustomerStatTemp;
import com.ucpaas.sms.task.entity.access.SmsAccessSendStat;
import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;
import com.ucpaas.sms.task.enum4sms.AccessChannelStatisticsType;
import com.ucpaas.sms.task.exception.AccessChannelStatisticsException;
import com.ucpaas.sms.task.mapper.access.AccessChannelStatisticsMapper;
import com.ucpaas.sms.task.mapper.message.AgentBalanceBillMapper;
import com.ucpaas.sms.task.mapper.onlinepay.JsmsOnlinePayMapper;
import com.ucpaas.sms.task.model.ResultVO;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.statistic.AccessStatisticStrategy;
import com.ucpaas.sms.task.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author fangmingzhi
 * @title
 * @description 支付失败
 * @date 2017-02-21
 */
@Service
public class AgentOnlinePayFailServiceImpl implements AgentOnlinePayFailService {
    private static final Logger logger = LoggerFactory.getLogger(AgentOnlinePayFailServiceImpl.class);

    private static final String ONLINE_PAYMENT_OVERTIME = "ONLINE_PAYMENT_OVERTIME";
    @Autowired
    private JsmsOnlinePaymentService jsmsOnlinePaymentService;

    @Autowired
    private JsmsOnlinePayMapper jsmsOnlinePayMapper;

    @Autowired
    private JsmsParamService jsmsParamService;

    @Override
    public boolean execute(TaskInfo taskInfo) {
        logger.info("支付失败任务执行");
        JsmsPage jsmsPage=new JsmsPage();
        List<JsmsParam> jsmsParams=jsmsParamService.getByParamKey(ONLINE_PAYMENT_OVERTIME);
        String cancelTime="";
        /*if(CollectionUtils.isEmpty(jsmsParams)){
            cancelTime="1440";
        }else {
            String a[]=jsmsParams.get(0).getParamValue().split("|");
            cancelTime=jsmsParams.get(0).getParamValue().split("\\|")[1];
        }*/

        jsmsPage.getParams().put("submitTimeStart", DateUtils.getBeforeMinite(Long.valueOf(1)));
        List<JsmsOnlinePayment> jsmsOnlinePayments=jsmsOnlinePayMapper.queryPayFailList(jsmsPage);
        logger.info("查询到{}条数据",jsmsOnlinePayments.size());
        for(JsmsOnlinePayment jsmsOnlinePayment:jsmsOnlinePayments){
            logger.info("一条数据:{}-:-{}",jsmsOnlinePayment.getPaymentId(),jsmsOnlinePayment.getPaymentState());
            if(jsmsOnlinePayment.getPaymentState().equals(PaymentState.SUBMIT.getValue())) {
                logger.info("{}订单支付失败状态修改",jsmsOnlinePayment.getPaymentId());
                jsmsOnlinePayment.setPaymentState(PaymentState.FAIL.getValue());
                jsmsOnlinePaymentService.updateSelective(jsmsOnlinePayment);
            }
        }
        jsmsOnlinePaymentService.queryList(jsmsPage);
        return true;

    }
}
