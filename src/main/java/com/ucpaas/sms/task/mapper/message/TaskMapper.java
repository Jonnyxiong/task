package com.ucpaas.sms.task.mapper.message;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.task.entity.message.Task;


@Repository
public interface TaskMapper {

	Task getByTaskId(Integer taskId);

	Task getByTaskType(String taskType);
}