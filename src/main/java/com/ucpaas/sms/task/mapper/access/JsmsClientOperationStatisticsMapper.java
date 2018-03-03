package com.ucpaas.sms.task.mapper.access;

import com.jsmsframework.common.interceptor.SimpleCountSQL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.entity.access.JsmsClientOperationStatistics;
import com.jsmsframework.common.dto.JsmsPage;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
@Repository
public interface JsmsClientOperationStatisticsMapper{

	int insert(JsmsClientOperationStatistics model);

	int insertBatch(List<JsmsClientOperationStatistics> modelList);
	int updateSelectiveUnique(JsmsClientOperationStatistics model);

	int update(JsmsClientOperationStatistics model);

	int updateSelective(JsmsClientOperationStatistics model);

	JsmsClientOperationStatistics getById(Integer id);

	@SimpleCountSQL
	List<JsmsClientOperationStatistics> queryList(JsmsPage<JsmsClientOperationStatistics> page);

	List<JsmsClientOperationStatistics> findList(Map params);

	int count(Map<String, Object> params);

	BigDecimal getSalefee(Map params);

}