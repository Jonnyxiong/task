package com.ucpaas.sms.task.mapper.message;

import com.ucpaas.sms.task.entity.message.AccountgrRefAccount;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author 黄文杰
 * @description 客户组客户关联管理
 * @date 2017-07-27
 */
@Repository
public interface AccountgrRefAccountMapper {

    int insert(AccountgrRefAccount model);

    int insertBatch(List<AccountgrRefAccount> modelList);

    ;

    int update(AccountgrRefAccount model);

    int updateSelective(AccountgrRefAccount model);

    AccountgrRefAccount getByAccountGid(Integer accountGid);



    AccountgrRefAccount getByClientid(@Param("clientid") String clientid);


    int count(Map<String, Object> params);

}