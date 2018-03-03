package com.ucpaas.sms.task.mapper.message;

import com.ucpaas.sms.task.entity.message.UserPriceLog;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author 黄文杰
 * @description 后付费用户价格变更记录管理
 * @date 2017-07-27
 */
@Repository
public interface UserPriceLogMapper {

    int insert(UserPriceLog model);

    int insertBatch(List<UserPriceLog> modelList);


    int update(UserPriceLog model);

    int updateSelective(UserPriceLog model);

    UserPriceLog getById(Integer id);

    int count(Map<String, Object> params);

    List<UserPriceLog> queryAll(Map params);
}