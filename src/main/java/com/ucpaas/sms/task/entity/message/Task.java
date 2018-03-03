package com.ucpaas.sms.task.entity.message;

import java.util.Date;

public class Task {
    
    // 任务id
    private Integer taskId;
    // 任务名称
    private String taskName;
    // 任务类型，1：调用存储过程；2：预警任务；3：同步数据；等具体参考配置t_sms_dict.param_type=task_type
    private String taskType;
    // 连接的数据库：1、ucpaas_message  2、ucpaas_message_statistics
    private String dbType;
    // 执行的存储过程名称
    private String procedureName;
    // 执行类型，0：空；1：分钟；2：小时；3：天；4：周；5：月；6：季度；7：年；配置t_sms_dict.param_type=task_execute_type
    private String executeType;
    // 下次执行时间，格式：yyyyMMddHHmm
    private Long executeNext;
    // 执行周期
    private Integer executePeriod;
    // 扫描类型，1：分钟；2：小时；3：天；4：周；5：月；6：季度；7：年；配置t_sms_dict.param_type=task_execute_type
    private String scanType;
    // 下次扫描时间
    private Date scanNext;
    // 扫描周期
    private Integer scanPeriod;
    // 是否每次扫描都执行，0否；1是；配置t_sms_dict.param_type=task_scan_execute
    private String scanExecute;
    // 依赖任务，多个值用,分割
    private String dependency;
    // 分组
    private Integer group;
    // 组内排序
    private Integer order;
    // 状态，0：关闭；1：启用；2：正在执行；3：删除；配置t_sms_dict.param_type=task_status
    private String status;
    // 创建时间
    private Date createDate;
    // 更新时间
    private Date updateDate;
    
    public Integer getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Integer taskId) {
        this.taskId = taskId ;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName ;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType ;
    }
    
    public String getDbType() {
        return dbType;
    }
    
    public void setDbType(String dbType) {
        this.dbType = dbType ;
    }
    
    public String getProcedureName() {
        return procedureName;
    }
    
    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName ;
    }
    
    public String getExecuteType() {
        return executeType;
    }
    
    public void setExecuteType(String executeType) {
        this.executeType = executeType ;
    }
    
    public Long getExecuteNext() {
        return executeNext;
    }
    
    public void setExecuteNext(Long executeNext) {
        this.executeNext = executeNext ;
    }
    
    public Integer getExecutePeriod() {
        return executePeriod;
    }
    
    public void setExecutePeriod(Integer executePeriod) {
        this.executePeriod = executePeriod ;
    }
    
    public String getScanType() {
        return scanType;
    }
    
    public void setScanType(String scanType) {
        this.scanType = scanType ;
    }
    
    public Date getScanNext() {
        return scanNext;
    }
    
    public void setScanNext(Date scanNext) {
        this.scanNext = scanNext ;
    }
    
    public Integer getScanPeriod() {
        return scanPeriod;
    }
    
    public void setScanPeriod(Integer scanPeriod) {
        this.scanPeriod = scanPeriod ;
    }
    
    public String getScanExecute() {
        return scanExecute;
    }
    
    public void setScanExecute(String scanExecute) {
        this.scanExecute = scanExecute ;
    }
    
    public String getDependency() {
        return dependency;
    }
    
    public void setDependency(String dependency) {
        this.dependency = dependency ;
    }
    
    public Integer getGroup() {
        return group;
    }
    
    public void setGroup(Integer group) {
        this.group = group ;
    }
    
    public Integer getOrder() {
        return order;
    }
    
    public void setOrder(Integer order) {
        this.order = order ;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
    
    public Date getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(Date createDate) {
        this.createDate = createDate ;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate ;
    }
    
}