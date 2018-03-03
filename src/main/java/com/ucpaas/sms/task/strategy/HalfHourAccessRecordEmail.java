package com.ucpaas.sms.task.strategy;

import com.ucpaas.sms.task.model.TaskInfo;


public interface HalfHourAccessRecordEmail {

    boolean doJob(TaskInfo taskInfo);
}
