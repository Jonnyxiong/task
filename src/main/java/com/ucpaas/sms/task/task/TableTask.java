package com.ucpaas.sms.task.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ucpaas.sms.task.constant.TaskConstant.TaskStatus;
import com.ucpaas.sms.task.model.TaskInfo;
import com.ucpaas.sms.task.service.common.TaskRunService;
import com.ucpaas.sms.task.thread.ProcessThread;
import com.ucpaas.sms.task.util.TaskUtils;

/**
 * task表任务
 * 
 * @author xiejiaan
 */
@Component
public class TableTask {
	private static final Logger logger = LoggerFactory.getLogger(TableTask.class);
	@Autowired
	private TaskRunService taskService;
	/**
	 * 任务线程池大小
	 */
	private static final int task_thread_pool_size = 50;
	/**
	 * 正在执行的任务
	 */
	public static final Set<Integer> running_task_set = new CopyOnWriteArraySet<Integer>();

	/**
	 * 执行任务
	 */
	public void execute() {
		String time = DateTime.now().toString("【yyyy-MM-dd HH:mm:ss】");
		logger.debug("\n\n-------------------------定时任务开始{}", time);
		logger.debug("正在执行的任务：{}", running_task_set);

		List<Map<String, Object>> taskList = taskService.queryTask();
		logger.debug("查询需要执行的任务：{}", taskList);
		if (taskList.size() < 1) {
			logger.debug("没有定时任务需要执行{}", time);
			return;
		}
		Set<Integer> taskSet = new HashSet<Integer>();
		taskSet.addAll(running_task_set);
		for (Map<String, Object> map : taskList) {
			taskSet.add(Integer.valueOf(map.get("task_id").toString()));
		}

		Integer taskId;
		String dependency;
		Iterator<Map<String, Object>> it = taskList.iterator();
		Map<String, Object> map;
		while (it.hasNext()) {
			map = it.next();
			dependency = Objects.toString(map.get("dependency"), null);
			if (StringUtils.isNotBlank(dependency)) {
				for (String s : dependency.split(",")) {
					taskId = Integer.valueOf(s);
					if (taskSet.contains(taskId)) {
						it.remove();// 本任务依赖的任务正在执行或需要执行，则现在不执行
					}
				}
			}
		}

		List<List<TaskInfo>> groupList = new ArrayList<List<TaskInfo>>();
		List<TaskInfo> tList = new ArrayList<TaskInfo>();
		TaskStatus status = null;
		String group = null;
		for (Map<String, Object> taskMap : taskList) {
			taskId = Integer.valueOf(taskMap.get("task_id").toString());
			status = TaskStatus.getInstance(Integer.parseInt(taskMap.get("status").toString()));
			if (running_task_set.contains(taskId) && status == TaskStatus.running) {
				logger.debug("任务结束【正在执行】：taskMap={}", taskMap);
				continue;
			}
			running_task_set.add(taskId);
			taskService.updateStatus(taskId, TaskStatus.running);

			if (group == null || !group.equals(taskMap.get("group").toString())) {
				if (tList.size() > 0) {
					groupList.add(tList);
				}
				group = taskMap.get("group").toString();
				tList = new ArrayList<TaskInfo>();
			}
			tList.add(TaskUtils.getTaskInfo(taskMap));
		}
		if (tList.size() > 0) {
			groupList.add(tList);
		}
		logger.debug("任务分组完成：{}", groupList);
		if (groupList.size() < 1) {
			logger.debug("任务分组为空，定时任务结束{}", time);
			return;
		}

		int poolSize = Math.min(groupList.size(), task_thread_pool_size);// 配置的定时任务线程池大小
		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
		for (List<TaskInfo> list : groupList) {
			pool.submit(new ProcessThread(list));// 启动任务处理线程
		}
		pool.shutdown();
		logger.debug("定时任务结束{}", time);
	}

}
