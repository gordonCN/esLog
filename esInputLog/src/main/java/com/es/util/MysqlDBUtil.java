package com.es.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.es.bean.ErrorInfo;
import com.es.bean.ErrorStatistic;

public class MysqlDBUtil extends DBUtil {

	
	private static Logger logger = Logger.getLogger(MysqlDBUtil.class.getName());

	public static  String tableSchema = dbPro.getKeyValue("jdbc.tableschema");


	public Map getSignalErrorInfo(String errorSystem, String errorClass, String errorCode) {

		Map signalErrorMap = new HashMap<String, Integer>();

		int errorId = 0;
		int errorSumCount = 0;
		String sqlInfo = null;
		int insertNum = 0;
		sqlInfo = " select error_id,error_sum_count from loan_error_info be  " + "where be.error_class = '" + errorClass
				+ "' " + " and be.error_code = '" + errorCode + "'";
		List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
		try {
			if (this.conn == null) {
				initConnect();
			}
			pst = this.conn.prepareStatement(sqlInfo);
			rs = pst.executeQuery();
			while (rs.next()) {

				Map<String, Integer> result = new HashMap<String, Integer>();
				ResultSetMetaData md = rs.getMetaData();

				for (int i = md.getColumnCount(); i > 0; i--) {
					result.put(md.getColumnLabel(i).toLowerCase(), rs.getInt(i));
				}
				list.add(result);
			}
			if (list.size() == 0) {
				logger.warn("*** 未检索到错误id值   ***  添加记录 ");
				ErrorInfo errorInfo = new ErrorInfo();
				errorInfo.setErrorSystem(errorSystem);
				errorInfo.setErrorClass(errorClass);
				errorInfo.setErrorCode(errorCode);

				insertNum = preInsertErrorInfo(errorInfo);

				signalErrorMap = getSignalErrorInfo(errorSystem, errorClass, errorCode) ;
			} else if (list.size() == 1) {

				errorId = list.get(0).get("error_id");
				errorSumCount = list.get(0).get("error_sum_count");
				signalErrorMap.put("search_result", 1);
				signalErrorMap.put("error_id", errorId);
				signalErrorMap.put("error_sum_count", errorSumCount);
				System.out.print(".errorId: '" + errorId + "' ");
				// logger.info("*** 检索id : " + errorId + " . error_sum_count : "
				// + errorSumCount + " ***");

			} else {
				logger.error("********************************");
				logger.info("execute sql: " + sqlInfo);
				logger.error("*** 检索到多个错误id值   ***");
				logger.error("********************************");
				// 存在多条记录时，仅返回首条记录数据
				errorId = list.get(0).get("error_id");
				errorSumCount = list.get(0).get("error_sum_count");
				signalErrorMap.put("error_id", errorId);
				signalErrorMap.put("error_sum_count", errorSumCount);
				signalErrorMap.put("search_result", 2);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block

			logger.error("**检索 错误id 失败。");
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			logger.error("**检索 错误id 失败。");
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closePortable();
		}

		return signalErrorMap;

	}

	// public int preUpdateErrorCount(int errorId, int count) {
	public int preUpdateErrorCount(List<ErrorStatistic> updateErrors) {
		String updateSql = " update loan_error_info lei set lei.error_sum_count= lei.error_sum_count + ? where lei.error_id = ? ;";
		int updateNum = 0;
		try {
			if (this.conn == null) {
				initConnect();
			}
			this.conn.setAutoCommit(false);
			pst = this.conn.prepareStatement(updateSql);

			for (ErrorStatistic ei : updateErrors) {
				pst.setLong(1, ei.getHappendNum());
				pst.setInt(2, ei.getErrorID());
				pst.addBatch();
				updateNum++;
			}
			pst.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);

			logger.info("更新errorInfo : count... update_num: " + updateNum);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("更新 错误 统计总数  错误：" + e.getMessage());
			e.printStackTrace();
		} finally {
			closeCompleted();
		}

		return updateNum;
	}

	public int preInsertErrorInfo(ErrorInfo errorInfo) throws Exception {
		String updateSql = "insert into loan_error_info(error_id,error_system,error_class,error_code) values(nextval('error_id'),?,?,?)";
		int updateNum = 0;
		try {

			if (!"".equals(errorInfo.getErrorSystem()) && null != errorInfo.getErrorSystem()
					&& !"".equals(errorInfo.getErrorCode()) && null != errorInfo.getErrorCode()) {

				pst = this.conn.prepareStatement(updateSql);
				pst.setString(1, errorInfo.getErrorSystem());
				pst.setString(2, errorInfo.getErrorClass());
				pst.setString(3, errorInfo.getErrorCode());
				updateNum = pst.executeUpdate();

				logger.info("插入errorInfo bean... update_num: " + updateNum);

			} else {
				logger.error("插入 数据 归属系统 或者 错误代码 为 空");
				throw new Exception("插入 数据 归属系统 或者 错误代码 为 空");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		return updateNum;
	}

	// public int preInsertStatisticInfo(ErrorInfo errorInfo) throws Exception {
	public int preInsertStatisticInfo(List<ErrorStatistic> errorStatisticList) {
		// 获取当前时间
		// 判断当前时间对应表格是否存在---否 则创建表格
		// 遍历插入数据

		// tableBaseName
		String tableFixed = CommonUntil.getTableFixed(0);
		String tableName = statisticTatableBaseName + "_" + tableFixed;
		int updateNum = 0;
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp ts = null;
		try {
			// 判断表是否存在
			// String check_sql ="SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
			// T WHERE T.TABLE_SCHEMA =? AND T.TABLE_NAME= ?";
			boolean isExist = false;
			
			System.out.println("tableName: " + tableName);

			isExist = checkTableExist(tableName);
			// 若是表不存在，新建
			if (!isExist) {
				isExist = createTable(tableName);
			}
			if (isExist) {
				// 插入统计结果
				
				String insertSql = "insert into " + tableName + "(error_id,happend_count,execute_time,statistic_type) values(?,?,?,?)";
				System.out.println("this.conn" + this.conn);
				if (this.conn == null) {
					initConnect();
				}
				this.conn.setAutoCommit(false);
				
				pst = this.conn.prepareStatement(insertSql);
				logger.info("insertSql " + insertSql);
				int batchNum = 0;
				for (ErrorStatistic errorStatisticInfo : errorStatisticList) {
					pst.setInt(1, errorStatisticInfo.getErrorID());
					pst.setLong(2, errorStatisticInfo.getHappendNum());
					// Timestamp ts = new
					// Timestamp(errorStatisticInfo.getErrorExecuteTime().getTime())
					// ;
					// ts =
					// DateFormat.parse(errorStatisticInfo.getErrorExecuteTime().toString());

					ts = Timestamp.valueOf(sdf.format(errorStatisticInfo.getErrorExecuteTime()));
					pst.setTimestamp(3, ts);
					pst.setString(4,errorStatisticInfo.getStatisticType());
					
					pst.addBatch();
					batchNum++;

					if (batchNum % 1400 == 0) {
						pst.executeBatch();
						conn.commit();
					}
				}
				pst.executeBatch();
				conn.commit();
				conn.setAutoCommit(true);
				logger.info("***************** error statistic info inserted ! *************");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closePortable();
		}

		return updateNum;
	}

	public boolean checkTableExist( String tableName) {
		
		boolean isExist = false;
		try {
			System.out.println("tableSchema: " + tableSchema);
			String check_sql = " SELECT t.table_name,t.table_schema FROM INFORMATION_SCHEMA.TABLES t"
					+ " where t.table_schema='" + tableSchema + "' and  t.TABLE_NAME = '" + tableName + "' ;";
			System.out.println("check_sql" + check_sql);
			if (this.conn == null) {
				initConnect();
			}
			pst = this.conn.prepareStatement(check_sql);
			rs = pst.executeQuery();

			while (rs.next()) {
				if (tableName.equals(rs.getString("table_name")) && tableSchema.equals(rs.getString("table_schema"))) {
					isExist = true;
					logger.info(tableName + " is Exist");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isExist;
	}

	public boolean createTable(String tableName) {

		boolean exist = false;

		try {
			String create_sql = "CREATE TABLE " + tableName + " (" + "OID int not null primary key auto_increment,"
					+ " EXECUTE_TIME DATETIME not null," + " error_id  int not null, " + "happend_count int not null )";

			logger.info("create_sql : " + create_sql);
			if (this.conn == null) {
				initConnect();
			}
			pst = this.conn.prepareStatement(create_sql);
			pst.executeUpdate();
			logger.info("执行创建表操作");
			exist = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exist;
	}

	public List checkInfo(String sql) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		try {
			if (this.conn == null) {
				initConnect();
			}
			pst = this.conn.prepareStatement(sql);
			rs = pst.executeQuery(sql);
			while (rs.next()) {
				Map<String, String> result = new HashMap<String, String>();
				ResultSetMetaData md = rs.getMetaData();
				for (int i = md.getColumnCount(); i > 0; i--) {
					result.put(md.getColumnLabel(i).toLowerCase(), rs.getString(i));
				}
				list.add(result);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closePortable();
		}
		return list;
	}

	@Override
	public int preInsertHourStatisticInfo() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int preInsertDayStatisticInfo() {
		
		// tableBaseName  考虑月底月初 
		String tableFixed = CommonUntil.getTableFixed(-1);
		String tableName = statisticTatableBaseName + "_" + tableFixed;
		int updateNum = 0;
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp ts = null;
		try {
			
			boolean isExist = false;
			logger.info("day info push : " + tableName);
			isExist = checkTableExist(tableName);
			 
			if (isExist) {
				// 插入统计结果
			String insertSql = "insert into " + tableName+ "(oid,error_id,happend_count,execute_time,statistic_type) "
				+ "select Seqitsm.Nextval ,c.error_id,c.count,c.execute_time,c.hour from" 
				+ "(select sysdate execute_time ,b.error_id ,sum(b.happend_count) count ,'hour' hour from "
				+ "( select a.hour24 ,a.error_id,a.happend_count  from "
				+ "( SELECT to_char(t.execute_time,'yyyy-MM-dd HH24') hour24 ,t.* from  "+ tableName +" t where t.statistic_type ='minute' ) a"
				+ "	where a.hour24 = to_char(sysdate-1/24,'yyyy-MM-dd HH24') )b group by b.error_id ) c" ;
				
				if (this.conn == null) {
					initConnect();
				}
				pst = this.conn.prepareStatement(insertSql);
				updateNum = pst.executeUpdate();
				logger.info(" insert hour statistic error info. update number is " + updateNum);
				
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closeCompleted();
		}
		return updateNum;
	}

}
