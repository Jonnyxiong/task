package com.ucpaas.sms.task.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.task.entity.record.RecordChannelTempStatistics;
import com.ucpaas.sms.task.model.ResultVO;

public interface RecordChannelTempDataStatisticsService {

	public List<RecordChannelTempStatistics> generateData(String statTime);
    
}
