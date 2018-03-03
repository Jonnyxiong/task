package com.ucpaas.sms.task.mapper.onlinepay;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.finance.entity.JsmsOnlinePayment;
import com.ucpaas.sms.task.entity.message.AccountGroup;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * @description 在线支付
 * @author 方明智
 * @date 2017-07-27
 */
@Repository
public interface JsmsOnlinePayMapper {

	List<JsmsOnlinePayment> queryCancelList(JsmsPage<JsmsOnlinePayment> jsmsPage);

	List<JsmsOnlinePayment> queryPayFailList(JsmsPage<JsmsOnlinePayment> jsmsPage);

}