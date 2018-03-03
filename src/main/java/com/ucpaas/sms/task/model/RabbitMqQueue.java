package com.ucpaas.sms.task.model;

/**
 * Rabbit MQ 队列信息
 *
 */
public class RabbitMqQueue {
	
	private String name; // 队列名称
	
	private String messages; // 队列中的消息数量

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessages() {
		return messages;
	}

	public void setMessages(String messages) {
		this.messages = messages;
	}
	
}
