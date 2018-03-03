package com.ucpaas.sms.task.service;

import com.jsmsframework.common.constant.SysConstant;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.CreditRemarkType;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.finance.entity.JsmsAgentAccount;
import com.jsmsframework.common.enums.BusinessType;
import com.jsmsframework.finance.entity.JsmsAgentBalanceBill;
import com.jsmsframework.finance.service.JsmsAgentAccountService;
import com.jsmsframework.finance.service.JsmsAgentBalanceBillService;
import com.jsmsframework.finance.service.JsmsSaleCreditBillService;
import com.jsmsframework.sale.credit.service.JsmsSaleCreditService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.util.DateUtilsNew;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**修复代理商授信历史数据并生成对应销售授信账单
 * Created by Don on 2017/11/16.
 */
@Service
public class FixAgentCreditHisServiceImpl implements  FixAgentCreditHisService{

    @Autowired
    private JsmsSaleCreditService jsmsSaleCreditService;
    @Autowired
    private JsmsSaleCreditBillService jsmsSaleCreditBillService;
    @Autowired
    private JsmsAgentAccountService jsmsAgentAccountService;
    @Autowired
    private JsmsAgentInfoService jsmsAgentInfoService;
    @Autowired
    private JsmsAgentBalanceBillService jsmsAgentBalanceBillService;


    private static final Logger logger= LoggerFactory.getLogger("FixAgentCreditHisService");
    /**
     * @param taskInfo
     * @return
     */
    @Override
    @Transactional("message")
    public boolean fixAgentCreditHis(TaskInfo taskInfo) {

        //查询是否有执行过任务
        Map<String,Object> isParams=new HashedMap();
        isParams.put("remark",CreditRemarkType.初始.getDesc());
        int count=jsmsSaleCreditBillService.count(isParams);
        if (count>0){
            logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】任务已执行完毕,本次操作不执行");
            return true;
        }else {
            Calendar begin = Calendar.getInstance();
            logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】开始 = {}", DateUtilsNew.formatDateTime(begin.getTime()));
            Map<String,Object> params=new HashedMap();
            params.put("isCredit","1");
            List<JsmsAgentAccount> agentAccountLists= jsmsAgentAccountService.queryList(params);

            //1.财务授信给销售并生成账单

            //a.查询所有有授信记录的代理商，授信总额
            List<Map<String,Object>> saleDatas=jsmsAgentAccountService.querySumCreditBySale(params);

            //b.查询所有有超额欠款的代理商，总额度
            isParams.put("isSupper","1");
            List<Map<String,Object>> supperData=jsmsAgentAccountService.querySumCreditBySale(isParams);

            Long saleId,suSaleId;
            BigDecimal creditMoney,supperMoney;
            String remark= CreditRemarkType.初始.getDesc();
            if(saleDatas.size()>0){
                for (Map<String, Object> saledata : saleDatas) {
                    saleId=(Long)saledata.get("saleId");
                    creditMoney=new BigDecimal(saledata.get("creditSum").toString());
                    if(supperData.size()>0){
                        for (Map<String, Object> supper : supperData) {
                            suSaleId=(Long)supper.get("saleId");
                            supperMoney=new BigDecimal(supper.get("vailSum").toString());
                            if(Objects.equals(suSaleId,saleId)){
                                creditMoney=creditMoney.add(supperMoney);
                            }
                            logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】销售ID{}名下超欠款需重新补充额度为{}",suSaleId,supperMoney);
                        }
                    }

                    R r=jsmsSaleCreditService.creditForSale(SysConstant.SYS_ID, BusinessType.财务给销售授信.getValue(),null,saleId,creditMoney,remark);
                    if(Objects.equals(r.getCode(), SysConstant.FAIL_CODE)){
                        logger.error("【修复代理商授信历史数据并生成对应销售授信账单】财务授信给销售ID为{}并生成账单失败,原因是："+r.getMsg(),saleId);
                        throw  new RuntimeException("【修复代理商授信历史数据并生成对应销售授信账单】财务授信给销售为并生成账单失败");
                    }else {
                        logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】财务授信给销售授信信息为{}并生成账单成功", JsonUtil.toJson(saledata));
                    }
                }
            }

            logger.debug("----------------------【修复代理商授信历史数据并生成对应销售授信账单】财务给销售授信历史记录完毕----------------------");





            //2.初始化销售授信及账单、代理商授信余额及未回款授信额度
            BigDecimal needCreditMoney=BigDecimal.ZERO;

            for (JsmsAgentAccount acc : agentAccountLists) {
                JsmsAgentInfo agent=jsmsAgentInfoService.getByAgentId(acc.getAgentId());
                if(agent==null){
                    logger.error("【修复代理商授信历史数据并生成对应销售授信账单】初始化销售授信及账单失败,代理商ID={}不存在",acc.getAgentId());
                    throw  new RuntimeException("【修复代理商授信历史数据并生成对应销售授信账单】代理商ID="+acc.getAgentId()+"不存在");
                }else {
                    logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】销售授信代理商{}开始",JsonUtil.toJson(acc));

                    JsmsAgentAccount updateAcc=new JsmsAgentAccount();
                    updateAcc.setAgentId(acc.getAgentId());
                    JsmsAgentBalanceBill updatebill=new JsmsAgentBalanceBill();
                    if(acc.getBalance().compareTo(BigDecimal.ZERO)!=1){
                        if(acc.getBalance().add(acc.getCreditBalance()).compareTo(BigDecimal.ZERO)==-1){
                            needCreditMoney=acc.getBalance().abs();
                            updateAcc.setCreditBalance(needCreditMoney);
                            updateAcc.setCurrentCredit(BigDecimal.ZERO);
                            updateAcc.setNoBackPayment(needCreditMoney);
                            updatebill.setCreditBalance(updateAcc.getCreditBalance());
                            updatebill.setCurrentCredit(updateAcc.getCurrentCredit());
                            updatebill.setNoBackPayment(updateAcc.getNoBackPayment());
                        }else {
                            needCreditMoney=acc.getCreditBalance();
                            updateAcc.setCurrentCredit(acc.getCreditBalance().add(acc.getBalance()));
                            updateAcc.setNoBackPayment(acc.getBalance().abs());
                            updatebill.setCurrentCredit(updateAcc.getCurrentCredit());
                            updatebill.setNoBackPayment(updateAcc.getNoBackPayment());
                            updatebill.setCreditBalance(acc.getCreditBalance());
                        }


                    }else {
                        needCreditMoney=acc.getCreditBalance();
                        updateAcc.setCurrentCredit(acc.getCreditBalance());
                        updatebill.setCurrentCredit(acc.getCreditBalance());
                        updatebill.setCreditBalance(acc.getCreditBalance());
                    }

                    //存在归属销售 继续
                    if(agent.getBelongSale()!=null){

                        R r2=jsmsSaleCreditService.creditForSale(SysConstant.SYS_ID,BusinessType.销售给客户授信.getValue(),acc.getAgentId(),agent.getBelongSale(),needCreditMoney,remark);
                        if(Objects.equals(r2.getCode(), SysConstant.FAIL_CODE)){
                            logger.error("【修复代理商授信历史数据并生成对应销售授信账单】销售ID为{}授信给代理商ID{}并生成账单失败,原因是："+r2.getMsg(),agent.getBelongSale(),acc.getAgentId());
                            throw  new RuntimeException("【修复代理商授信历史数据并生成对应销售授信账单】生成账单失败");
                        }else {
                            logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】销售ID为{}授信给代理商ID{}并生成账单成功", agent.getBelongSale(),acc.getAgentId());
                        }
                    }



                    int upAgent=jsmsAgentAccountService.updateSelective(updateAcc);
                    if(upAgent>0){
                        logger.info("【修复代理商授信历史数据并生成对应销售授信账单】初始化代理商ID={}的授信余额及未回款授信额度成功",acc.getAgentId());

                    }else {
                        logger.error("【修复代理商授信历史数据并生成对应销售授信账单】初始化代理商ID={}的授信余额及未回款授信额度失败",acc.getAgentId());
                        throw  new RuntimeException("【修复代理商授信历史数据并生成对应销售授信账单】初始化代理商ID="+acc.getAgentId()+"的授信余额及未回款授信额度失败");
                    }


                    //2017-11-30补充功能点，代理商流水需要修数据  最后一条数据
                    Map<String,Object> billparam=new HashedMap();
                    billparam.put("agentId",updateAcc.getAgentId());
                    int billcount=jsmsAgentBalanceBillService.count(billparam);
                    if(billcount>0){
                        JsmsAgentBalanceBill agbill=jsmsAgentBalanceBillService.getBill4Max(updateAcc.getAgentId());
                        //更新流水最新记录
                        updatebill.setId(agbill.getId());
                        int upagentbill=jsmsAgentBalanceBillService.updateSelective(updatebill);
                        if(upagentbill>0){
                            logger.info("【修复代理商授信历史数据并生成对应销售授信账单】初始化代理商流水{}授信相关成功",JsonUtil.toJson(updatebill));

                        }else {
                            logger.error("【修复代理商授信历史数据并生成对应销售授信账单】初始化代理商流水{}授信相关失败",JsonUtil.toJson(updatebill));
                            throw  new RuntimeException("【修复代理商授信历史数据并生成对应销售授信账单】初始化代理商流水="+JsonUtil.toJson(updatebill)+"授信相关失败");
                        }
                    }else {
                        logger.info("【修复代理商授信历史数据并生成对应销售授信账单】代理商流水{}不存在,不更新流水！",JsonUtil.toJson(updatebill));
                    }


                }

            }




            Calendar end = Calendar.getInstance();
            logger.debug("【修复代理商授信历史数据并生成对应销售授信账单】结束 = {}", DateUtilsNew.formatDateTime(end.getTime()));

        }

        return true;


    }

}
