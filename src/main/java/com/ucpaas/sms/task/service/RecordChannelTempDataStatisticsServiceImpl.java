package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.*;

import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.task.entity.message.Param;
import com.ucpaas.sms.task.entity.record.RecordChannelTempStatistics;
import com.ucpaas.sms.task.mapper.message.ParamMapper;
import com.ucpaas.sms.task.mapper.record.RecordChannelTempDataStatisticsMapper;

/**
 * @author huangwenjie
 * @description 通道侧统计任务生成临时统计数据
 * @date 2017-03-16
 */
@Service
public class RecordChannelTempDataStatisticsServiceImpl implements RecordChannelTempDataStatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordChannelTempDataStatisticsServiceImpl.class);

    @Autowired
    private RecordChannelTempDataStatisticsMapper recordChannelTempDataStatisticsMapper;
    @Autowired
    private ParamMapper paramMapper;
    @Autowired
    private JsmsAccountService jsmsAccountService;


    @Override
    public List<RecordChannelTempStatistics> generateData(String statTime) {
        Param param = paramMapper.getByParamKey("MAX_RECORD_IDENTIFY");
        int max_identify = 0;
        try {
            max_identify = Integer.valueOf(param.getParamValue());
            LOGGER.debug("查询系统参数MAX_RECORD_IDENTIFY（通道标识范围） = {}", max_identify);
        } catch (Exception e) {
            LOGGER.error("统计record库失败, 查询系统参数MAX_RECORD_IDENTIFY为空", e);
            throw e;
        }

        List<RecordChannelTempStatistics> tmpSmsRecordChannelStatistics = new ArrayList<>();
        for (int i = 0; i <= max_identify; i++) {
            LOGGER.debug("统计通道流水表t_sms_record_" + i + "_" + statTime);
            Map params = new HashMap<>(2);
            params.put("identify", i);
            params.put("statTime", statTime);

            // 取出字段，用于兼容表结构修改
            List<String> columnsList = recordChannelTempDataStatisticsMapper.getTableSchema("t_sms_record_" + i + "_" + statTime);


            // 将字段加入参数，用于判断是否存在
            for (String column : columnsList) {
                params.put(column, 1);
            }

            // 存储结果集
            Map<String, RecordChannelTempStatistics> mergeMap = new HashMap<>(16);
            // 存储belongSale为空的Clientid
            Set<String> clientIds = new HashSet<>();
            List<RecordChannelTempStatistics> recordChannelTempStatisticsList = recordChannelTempDataStatisticsMapper.generateDataFromRecord(params);
            // 迭代存储belongSale为空的Clientid
            for (RecordChannelTempStatistics recordChannelTempStatistics : recordChannelTempStatisticsList) {
                if (null == recordChannelTempStatistics.getBelongSale()) {
                    clientIds.add(recordChannelTempStatistics.getClientid());
                }
            }
            LOGGER.debug("统计通道流水库中,belongSale为空的clientid" + JsonUtil.toJson(clientIds));

            if (!clientIds.isEmpty()) {
                // 根据Clientid获取客户信息
                List<JsmsAccount> jsmsAccountList = jsmsAccountService.getByIds(clientIds);
                // 循环遍历,存储Clientid和belongSale,便于获取
                Map<String, Long> belongSaleMap = new HashMap<>(jsmsAccountList.size());
                for (JsmsAccount jsmsAccount : jsmsAccountList) {
                    belongSaleMap.put(jsmsAccount.getClientid(), jsmsAccount.getBelongSale());
                }
                LOGGER.debug("获取account表中的clientid的belongSale" + JsonUtil.toJson(belongSaleMap));

                // 补全belongSale为空的数据,
                for (RecordChannelTempStatistics recordChannelTempStatistics : recordChannelTempStatisticsList) {
                    if (null == recordChannelTempStatistics.getBelongSale()) {
                        Long belongSale = belongSaleMap.get(recordChannelTempStatistics.getClientid());
                        recordChannelTempStatistics.setBelongSale(belongSale);
                    }
                }

                // 如channelid,clientid,paytype,belong_sale,belong_business,smstype都为一样,则合并总数
                for (RecordChannelTempStatistics recordChannelTempStatistics: recordChannelTempStatisticsList) {
                    Integer channelid = recordChannelTempStatistics.getChannelid();
                    String clientid = recordChannelTempStatistics.getClientid();
                    Integer paytype = recordChannelTempStatistics.getPaytype();
                    Long belongSale = recordChannelTempStatistics.getBelongSale();
                    Long belongBusiness = recordChannelTempStatistics.getBelongBusiness();
                    Integer smstype = recordChannelTempStatistics.getSmstype();
                    // 根据分组条件,组合成key
                    String key = channelid+ "-" + clientid+ "-" + paytype+ "-" + belongSale+ "-" + belongBusiness + "-" + smstype;
                    LOGGER.debug("根据分组条件组成的key为:" + key);
                    if (mergeMap.containsKey(key)) {
                        RecordChannelTempStatistics value = mergeMap.get(key);
                        BigDecimal chargetotal = recordChannelTempStatistics.getChargetotal().add(value.getChargetotal());
                        BigDecimal costtotal = recordChannelTempStatistics.getCosttotal().add(value.getCosttotal());
                        BigDecimal sendtotal = recordChannelTempStatistics.getSendtotal().add(value.getSendtotal());
                        BigDecimal notsend = recordChannelTempStatistics.getNotsend().add(value.getNotsend());
                        BigDecimal submitsuccess = recordChannelTempStatistics.getSubmitsuccess().add(value.getSubmitsuccess());
                        BigDecimal subretsuccess = recordChannelTempStatistics.getSubretsuccess().add(value.getSubretsuccess());
                        BigDecimal reportsuccess = recordChannelTempStatistics.getReportsuccess().add(value.getReportsuccess());
                        BigDecimal submitfail = recordChannelTempStatistics.getSubmitfail().add(value.getSubmitfail());
                        BigDecimal subretfail = recordChannelTempStatistics.getSubretfail().add(value.getSubretfail());
                        BigDecimal reportfail = recordChannelTempStatistics.getReportfail().add(value.getReportfail());
                        recordChannelTempStatistics.setChargetotal(chargetotal);
                        recordChannelTempStatistics.setCosttotal(costtotal);
                        recordChannelTempStatistics.setSendtotal(sendtotal);
                        recordChannelTempStatistics.setNotsend(notsend);
                        recordChannelTempStatistics.setSubmitsuccess(submitsuccess);
                        recordChannelTempStatistics.setSubretsuccess(subretsuccess);
                        recordChannelTempStatistics.setReportsuccess(reportsuccess);
                        recordChannelTempStatistics.setSubmitfail(submitfail);
                        recordChannelTempStatistics.setSubretfail(subretfail);
                        recordChannelTempStatistics.setReportfail(reportfail);
                        // 累加完,替换原有的
                        mergeMap.put(key, recordChannelTempStatistics);
                        LOGGER.debug("合并累加前" + JsonUtil.toJson(value));
                        LOGGER.debug("合并累加后" + JsonUtil.toJson(recordChannelTempStatistics));
                    } else {
                        mergeMap.put(key, recordChannelTempStatistics);
                    }
                }
                tmpSmsRecordChannelStatistics.addAll(mergeMap.values());
            } else {
                tmpSmsRecordChannelStatistics.addAll(recordChannelTempStatisticsList);
            }

        }


        return tmpSmsRecordChannelStatistics;
    }

}
