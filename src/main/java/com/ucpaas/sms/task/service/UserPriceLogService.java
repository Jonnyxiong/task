package com.ucpaas.sms.task.service;


import com.ucpaas.sms.task.entity.message.UserPriceLog;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

/**
 * @author 黄文杰
 * @description 后付费用户价格变更记录管理
 * @date 2017-07-27
 */
public interface UserPriceLogService {

    public int insert(UserPriceLog model);

    public int insertBatch(List<UserPriceLog> modelList);


    public int update(UserPriceLog model);

    public int updateSelective(UserPriceLog model);

    public UserPriceLog getById(Integer id);


    public int count(Map<String, Object> params);

    /**
     *
     * @param clientid  客户id
     * @param smstype  短信类型
     * @param statDay  数据日期
     * @return
     */
    UserPriceLog getByPrice(String clientid, Integer smstype, DateTime statDay);
}
