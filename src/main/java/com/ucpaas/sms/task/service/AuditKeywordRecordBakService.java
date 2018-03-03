package com.ucpaas.sms.task.service;

import com.ucpaas.sms.task.model.TaskInfo;

import java.util.Date;

public interface AuditKeywordRecordBakService {

    boolean bakAuditKeywordRecord(TaskInfo taskInfo);

    void backupAuditKeywordRecord(long id, Date createtime);



}
