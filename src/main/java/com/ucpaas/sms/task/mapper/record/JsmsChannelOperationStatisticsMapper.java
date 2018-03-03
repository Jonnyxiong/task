package com.ucpaas.sms.task.mapper.record;

import com.jsmsframework.common.interceptor.SimpleCountSQL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.entity.record.JsmsChannelOperationStatistics;
import com.jsmsframework.common.dto.JsmsPage;

/**
 * @description 通道属性
 * @author huangwenjie
 * @date 2018-01-13
 */
@Repository
public interface JsmsChannelOperationStatisticsMapper{

	int insert(JsmsChannelOperationStatistics model);
	
	int insertBatch(List<JsmsChannelOperationStatistics> modelList);

	
	int update(JsmsChannelOperationStatistics model);
	
	int updateSelective(JsmsChannelOperationStatistics model);

    JsmsChannelOperationStatistics getById(Integer id);

	@SimpleCountSQL
	List<JsmsChannelOperationStatistics> queryList(JsmsPage<JsmsChannelOperationStatistics> page);

	List<JsmsChannelOperationStatistics> findList(Map params);

	int count(Map<String, Object> params);
	int updateSelectiveByDate(JsmsChannelOperationStatistics model);

	BigDecimal getCostfee(Map params);

}