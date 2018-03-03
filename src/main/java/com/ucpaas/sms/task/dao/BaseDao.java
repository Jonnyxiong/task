package com.ucpaas.sms.task.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucpaas.sms.task.model.TaskInfo;

/**
 * dao类的基类
 * 
 * @author xiejiaan
 */
public abstract class BaseDao {
	private static final Logger logger = LoggerFactory.getLogger(BaseDao.class);
	protected SqlSessionTemplate sqlSessionTemplate;

	/**
	 * 设置SqlSessionTemplate
	 * 
	 * @param sqlSessionTemplate
	 */
	protected abstract void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate);

	public SqlSessionTemplate getSqlSessionTemplate() {
		return sqlSessionTemplate;
	}

	/**
	 * 查询List
	 * 
	 * @param queryStr
	 * @param sqlParams
	 * @return
	 */
	public <T> List<T> selectList(String queryStr, Object sqlParams) {
		return sqlSessionTemplate.selectList(queryStr, sqlParams);
	}

	/**
	 * 查询List
	 * 
	 * @param queryStr
	 * @param sqlParams
	 * @return
	 */
	public List<Map<String, Object>> getSearchList(String queryStr, Object sqlParams) {
		List<Map<String, Object>> list = sqlSessionTemplate.selectList(queryStr, sqlParams);
		return list;
	}

	/**
	 * 调用存储过程
	 * 
	 * @param taskInfo
	 * @return 是否成功
	 */
	public boolean callProcedure(TaskInfo taskInfo) {
		String procedureName = taskInfo.getProcedureName();
		String executePrev = taskInfo.getExecutePrev();
		if (executePrev == null) {
			executePrev = "0";
		}
		String sql = "{call " + procedureName + "(?, ?)}";
		logger.debug("调用存储过程【开始】：sql={}, executePrev={}", sql, executePrev);
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = sqlSessionTemplate.getSqlSessionFactory().openSession().getConnection();
			cs = conn.prepareCall(sql);
			cs.setLong(1, Long.parseLong(executePrev));
			cs.registerOutParameter(2, Types.INTEGER); // 注册出参
			cs.execute();
			int result = cs.getInt(2);
			logger.debug("调用存储过程【成功】：sql={}, executePrev={}, result={}", sql, executePrev, result);
			return true;

		} catch (Throwable e) {
			logger.error("调用存储过程【失败】：" + taskInfo, e);
			return false;
		} finally {
			try {
				if (cs != null) {
					cs.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("调用存储过程【关闭连接失败】：" + taskInfo, e);
			}
		}
	}
	/**
	 * 调用存储过程(建表)
	 * 
	 * @param taskInfo,建表标识
	 * @return 是否成功
	 */
	public boolean callProcedureCreateTable(TaskInfo taskInfo,int identify) {
//		String procedureName = taskInfo.getProcedureName();
		String executePrev = taskInfo.getExecutePrev();
		if (executePrev == null) {
			executePrev = "0";
		}
		String sql = "{call p_create_table_day(?,?,?)}";
		logger.debug("调用存储过程【开始】：sql={}, executePrev={}", sql, executePrev);
		Connection conn = null;
		CallableStatement cs = null;
		try {
			conn = sqlSessionTemplate.getSqlSessionFactory().openSession().getConnection();
			cs = conn.prepareCall(sql);
			cs.setLong(1, Long.parseLong(executePrev));
			cs.setInt(2, identify);
			cs.registerOutParameter(3, Types.INTEGER); // 注册出参
			cs.execute();
			int result = cs.getInt(3);
			logger.debug("调用存储过程【成功】：sql={}, executePrev={}, result={}", sql, executePrev, result);
			return true;
			
		} catch (Throwable e) {
			logger.error("调用存储过程【失败】：" + taskInfo, e);
			return false;
		} finally {
			try {
				if (cs != null) {
					cs.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("调用存储过程【关闭连接失败】：" + taskInfo, e);
			}
		}
	}

	/**
	 * 计算列表总数
	 * 
	 * @param countStr
	 * @param sqlParams
	 * @return
	 */
	public int getSearchSize(String countStr, Map<String, Object> sqlParams) {
		int totalCount = 0;
		List<Map<String, Object>> list = sqlSessionTemplate.selectList(countStr, sqlParams);
		if (list.size() > 0) {
			totalCount = Integer.parseInt(list.get(0).get("totalCount").toString());
		}
		return totalCount;
	}

	/**
	 * 插入SQL执行
	 * 
	 * @param insertStr
	 * @param sqlParams
	 * @return
	 */
	public int insert(String insertStr, Object sqlParams) {
		return sqlSessionTemplate.insert(insertStr, sqlParams);
	}

	/**
	 * 更新SQL执行
	 * 
	 * @param updateStr
	 * @param sqlParams
	 * @return
	 */
	public int update(String updateStr, Object sqlParams) {
		return sqlSessionTemplate.update(updateStr, sqlParams);
	}

	/**
	 * 删除SQL执行
	 * 
	 * @param updateStr
	 * @param sqlParams
	 * @return
	 */
	public int delete(String updateStr, Object sqlParams) {
		return sqlSessionTemplate.delete(updateStr, sqlParams);
	}

	/**
	 * 根据条件返回单个对象
	 * 
	 * @param queryStr
	 * @param sqlParams
	 * @return
	 */
	public <T> T getOneInfo(String queryStr, Object sqlParams) {
		return sqlSessionTemplate.selectOne(queryStr, sqlParams);
	}

	
	/**
	 * 查询单个对象
	 * 
	 * @param sqlId
	 * @param sqlParams
	 * @return
	 */
	public <T> T selectOne(String sqlId, Object sqlParams) {
		return sqlSessionTemplate.selectOne(sqlId, sqlParams);
	}
	
	/**
	 * 查询List，查询全部，以对象的形式
	 * 
	 * @param queryStr
	 * @param sqlParams
	 * @return
	 */
	public <T> List<T> queryAll(String queryStr, Map sqlParams) {
		List<T> list = sqlSessionTemplate.selectList(queryStr, sqlParams);
		return list;
	}


}
