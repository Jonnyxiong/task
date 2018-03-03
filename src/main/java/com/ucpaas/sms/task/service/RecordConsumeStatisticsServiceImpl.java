package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.entity.record.RecordConsumeStatistics;
import com.ucpaas.sms.task.mapper.record.RecordConsumeStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class RecordConsumeStatisticsServiceImpl implements RecordConsumeStatisticsService {

    @Autowired
    private RecordConsumeStatisticsMapper recordConsumeStatisticsMapper;

    @Override
    @Transactional
    public int insert(RecordConsumeStatistics model) {
        return this.recordConsumeStatisticsMapper.insert(model);
    }

    @Override
    @Transactional
    public int insertBatch(List<RecordConsumeStatistics> modelList) {
        return this.recordConsumeStatisticsMapper.insertBatch(modelList);
    }

    @Override
    @Transactional
    public int delete(Integer id) {
        RecordConsumeStatistics model = this.recordConsumeStatisticsMapper.getById(id);
        if (model != null)
            return this.recordConsumeStatisticsMapper.delete(id);
        return 0;
    }

    @Override
    @Transactional
    public int update(RecordConsumeStatistics model) {
        RecordConsumeStatistics old = this.recordConsumeStatisticsMapper.getById(model.getId());
        if (old == null) {
            return 0;
        }
        return this.recordConsumeStatisticsMapper.update(model);
    }

    @Override
    @Transactional
    public int updateSelective(RecordConsumeStatistics model) {
        return this.recordConsumeStatisticsMapper.updateSelective(model);
    }

    @Override
    @Transactional
    public RecordConsumeStatistics getById(Integer id) {
        RecordConsumeStatistics model = this.recordConsumeStatisticsMapper.getById(id);
        return model;
    }

    @Override
    @Transactional
    public List<RecordConsumeStatistics> queryList(Map page) {
        List<RecordConsumeStatistics> list = this.recordConsumeStatisticsMapper.queryList(page);
        return list;
    }

    @Override
    @Transactional
    public int count(Map<String, Object> params) {
        return this.recordConsumeStatisticsMapper.count(params);
    }

    @Override
    @Transactional
    public List<RecordConsumeStatistics> queryMonthly(Map<String, Object> sqlParams) {
        return this.recordConsumeStatisticsMapper.queryMonthly(sqlParams);
    }

    @Override
    @Transactional
    public List<RecordConsumeStatistics> queryByDateLike(String yyyyMM) {
        return this.recordConsumeStatisticsMapper.queryByDateLike(yyyyMM);
    }

    @Override
    @Transactional
    public int deleteByDate(String day) {
        return this.recordConsumeStatisticsMapper.deleteByDate(day);
    }

}
