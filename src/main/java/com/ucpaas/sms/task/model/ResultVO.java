package com.ucpaas.sms.task.model;

/**
 * 结果传递类
 *
 * @author huangwenjie
 * @create 2016-12-31-16:48
 */
public class ResultVO {
	private boolean success;
	private boolean fail;
	private Integer code;
	/**
	 * 携带的数据，可能是列表、分页对象等
	 */
	private Object data;
	/**
	 * 携带的消息，包括提示、日志之类的
	 */
	private String msg;

	/**
	 * 初始化时，默认是失败
	 */
	public ResultVO() {
		setFail(true);
	}

	public String getMsg() {
		return msg;
	}

	public ResultVO setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public boolean isSuccess() {
		return success;
	}

	public ResultVO setSuccess(boolean success) {
		this.success = success;
		if (success)
			fail = false;
		return this;
	}

	public boolean isFail() {
		return fail;
	}

	public void setFail(boolean fail) {
		this.fail = fail;
		if (fail)
			success = false;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public static ResultVO successDefault(Object data) {
		ResultVO resultVO = new ResultVO();
		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		resultVO.setData(data);
		return resultVO;
	}

	public static ResultVO failure() {
		ResultVO resultVO = new ResultVO();
		resultVO.setSuccess(false);
		resultVO.setMsg("操作失败！");
		return resultVO;
	}

	public boolean isFailure() {
		return fail;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public static ResultVO successDefault() {
		ResultVO resultVO = new ResultVO();
		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		return resultVO;
	}

}
