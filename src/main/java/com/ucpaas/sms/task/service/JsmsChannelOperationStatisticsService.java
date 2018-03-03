package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import com.ucpaas.sms.task.entity.record.JsmsChannelOperationStatistics;

import com.jsmsframework.common.dto.JsmsPage;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
public interface JsmsChannelOperationStatisticsService {

    int insert(JsmsChannelOperationStatistics model);

    int insertOrUpdateByDate(JsmsChannelOperationStatistics model);

    Boolean isExistByDate(JsmsChannelOperationStatistics model);

    int insertBatch(List<JsmsChannelOperationStatistics> modelList);

    int update(JsmsChannelOperationStatistics model);
    
    int updateSelective(JsmsChannelOperationStatistics model);

    JsmsChannelOperationStatistics getById(Integer id);
    
    JsmsPage queryList(JsmsPage page);

    List<JsmsChannelOperationStatistics> findList(Map params);

    int count(Map<String, Object> params);

    BigDecimal getCostfee(Map params);
}
