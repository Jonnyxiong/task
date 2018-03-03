package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

/**修复代理商授信历史记录并生成对应销售授信账单
 * Created by Don on 2017/11/16.
 */
public interface FixAgentCreditHisService {

    /**
     *修复代理商授信历史数据并生成对应销售授信账单
     * @param taskInfo
     * @return
     */
    boolean fixAgentCreditHis(TaskInfo taskInfo);

}
