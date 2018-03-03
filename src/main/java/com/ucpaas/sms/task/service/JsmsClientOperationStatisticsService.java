package com.ucpaas.sms.task.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import com.ucpaas.sms.task.entity.access.JsmsClientOperationStatistics;

import com.jsmsframework.common.dto.JsmsPage;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
public interface JsmsClientOperationStatisticsService {

    int insert(JsmsClientOperationStatistics model);

    @Transactional(value = "access")
    int insertOrUpdateUnique(JsmsClientOperationStatistics model);

    Boolean isExistByUnique(JsmsClientOperationStatistics model);

    int insertBatch(List<JsmsClientOperationStatistics> modelList);

    int update(JsmsClientOperationStatistics model);
    
    int updateSelective(JsmsClientOperationStatistics model);

    JsmsClientOperationStatistics getById(Integer id);

    JsmsPage queryList(JsmsPage page);

    List<JsmsClientOperationStatistics> findList(Map params);

    int count(Map<String, Object> params);

    BigDecimal getSalefee(Map params);
}
