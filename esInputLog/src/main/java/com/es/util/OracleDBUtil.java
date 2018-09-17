package com.es.util;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.es.bean.ErrorInfo;
import com.es.bean.ErrorStatistic;

public class OracleDBUtil extends DBUtil {

	private static Logger logger = Logger.getLogger(OracleDBUtil.class.getName());

	public Map getSignalErrorInfo(String errorSystem, String errorClass,String errorCode) {

		Map signalErrorMap = new HashMap<String, Integer>();

		int errorId = 0;
		int errorSumCount = 0;
		String sqlInfo = null ;
		int insertNum = 0;
	/*	if (!"".equals(errorCode) && null != errorCode) {
			if (!"".equals(errorClass) && null != errorClass) {
				sqlInfo = " select error_id,error_sum_count from loan_error_info be  " + "where be.error_class = '"
						+ errorClass + "' " + " and be.error_code = '" + errorCode + "'";
			} else {
				sqlInfo = " select error_id,error_sum_count from loan_error_info be  " + "where be.error_code = '"
						+ errorCode + "'";
			}
		} else {
			logger.error("**************** errorCode is null ");
			
			return null ;
		}*/
		
		
			if (!"".equals(errorClass) && null != errorClass) {
				sqlInfo = " select error_id,error_sum_count from loan_error_info be  " + "where be.error_class = '"
						+ errorClass + "'";
			} else {
				logger.error("**************** errorCode is null ");
				
				return null ;
			}
		

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
				logger.warn("*** can not find id  *** insert order  ");
				ErrorInfo errorInfo = new ErrorInfo();
				errorInfo.setErrorSystem(errorSystem);
				errorInfo.setErrorClass(errorClass);
				errorInfo.setErrorCode(errorCode);

				insertNum = preInsertErrorInfo(errorInfo);

				signalErrorMap = getSignalErrorInfo(errorSystem, errorClass, errorCode);
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
				logger.error("*** find one more error id  ***");
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

			logger.error("**search error id failure");
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block

			logger.error("**search exception...");
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			closePortable();
		}

		return signalErrorMap;

	}

	// public int preUpdateErrorCount(int errorId, int count) {
	public int preUpdateErrorCount(List<ErrorStatistic> updateErrors) {
		String updateSql = " update loan_error_info lei set lei.error_sum_count= lei.error_sum_count + ? where lei.error_id = ?";
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

			logger.info(" update errorInfo : count... update_num: " + updateNum);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("update error info statistic error...." + e.getMessage());
			e.printStackTrace();
		} finally {
			closeCompleted();
		}

		return updateNum;
	}

	public int preInsertErrorInfo(ErrorInfo errorInfo) throws Exception {
		String updateSql = "insert into loan_error_info(error_id,error_system,error_class,error_code) values(seqitsm.nextval,?,?,?)";
		int updateNum = 0;
		try {

		/*	if (!"".equals(errorInfo.getErrorSystem()) && null != errorInfo.getErrorSystem()
					&& !"".equals(errorInfo.getErrorCode()) && null != errorInfo.getErrorCode()) {
		*/		
			if (!"".equals(errorInfo.getErrorSystem()) && null != errorInfo.getErrorSystem()
						&& !"".equals(errorInfo.getErrorClass()) && null != errorInfo.getErrorClass()) {

				pst = this.conn.prepareStatement(updateSql);
				pst.setString(1, errorInfo.getErrorSystem());
				pst.setString(2, errorInfo.getErrorClass());
				pst.setString(3, errorInfo.getErrorCode());
				updateNum = pst.executeUpdate();

				logger.info(" insert errorInfo bean... update_num: " + updateNum);

			} else {
				logger.error("error system is nul or code is null ");
				throw new Exception("error system is nul or code is null ");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(" program error ... ");
			logger.error(e.getMessage());

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
			
			if (isExist) {
				// 插入统计结果

				String insertSql = "insert into " + tableName
						+ "(oid,error_id,happend_count,execute_time,statistic_type) values(seqitsm.nextval,?,?,?,?)";
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
	
	//对当前一小时的统计结果汇总
	public int preInsertHourStatisticInfo() {
		
		// tableBaseName
		String tableFixed = CommonUntil.getTableFixed(0);
		String tableName = statisticTatableBaseName + "_" + tableFixed;
		int updateNum = 0;
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp ts = null;
		try {
			
			boolean isExist = false;
			logger.info("hour info push : " + tableName);
			isExist = checkTableExist(tableName);
			 
			if (isExist) {
				// 插入统计结果
		/*	String insertSql = "insert into " + tableName+ "(oid,error_id,happend_count,execute_time,statistic_type) "
				+ "select Seqitsm.Nextval ,c.error_id,c.count,c.execute_time,c.hour from" 
				+ "(select sysdate execute_time ,b.error_id ,sum(b.happend_count) count ,'hour' hour from "
				+ "( select a.hour24 ,a.error_id,a.happend_count  from "
				+ "( SELECT to_char(t.execute_time,'yyyy-MM-dd HH24') hour24 ,t.* from  "+ tableName +" t where t.statistic_type ='minute' ) a"
				+ "	where a.hour24 = to_char(sysdate-1/24,'yyyy-MM-dd HH24') )b group by b.error_id ) c" ;
			*/
				String insertSql = "insert into " + tableName+ "(oid,error_id,happend_count,execute_time,statistic_type) "
						+ "select Seqitsm.Nextval ,c.error_id,c.count,c.exec_time,c.hour from" 
						+ "(select sysdate exec_time ,t.error_id ,sum(t.happend_count) count ,'hour' hour from  "+ tableName + " t "
						+ "where t.statistic_type ='hour' and to_char(t.execute_time,'yyyy-MM-dd HH24') = to_char(sysdate-1/24,'yyyy-MM-dd HH24') "
						+ "group by t.error_id ) c" ;
				
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

	//对前一天的统计结果汇总
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
							+ "select Seqitsm.Nextval ,b.error_id,b.count,b.execute_time,b.type from "  
							+ "(select sysdate execute_time ,t.error_id,sum(t.happend_count) count ,'day' type from "+ tableName + " t "
							+ "  where t.statistic_type='hour' and to_char(t.execute_time,'yyyyMMdd') = to_char(sysdate -1 ,'yyyyMMdd') group by t.error_id ) b "; 
					
					if (this.conn == null) {
						initConnect();
					}
					pst = this.conn.prepareStatement(insertSql);
					updateNum = pst.executeUpdate();
					logger.info(" insert day statistic error info. update number is " + updateNum);
					
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

		
	
	public boolean checkTableExist(String tableName) {
		boolean isExist = false;
		boolean createFlag = false;
		try {

			String check_sql = " select count(1) from  user_tables where table_name=upper('" + tableName + "')";
			System.out.println("check_sql" + check_sql);
			if (this.conn == null) {
				initConnect();
			}
			pst = this.conn.prepareStatement(check_sql);
			rs = pst.executeQuery();

			while (rs.next()) {
				if (1 == rs.getInt("count(1)")) {
					isExist = true;
					logger.info(tableName + " is Exist");
				}
				if (0 == rs.getInt("count(1)")) {
					createFlag = createTable(tableName);
					isExist = true;
					logger.info(tableName + " is created");
				}
			}
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return isExist;
	}

	public boolean createTable(String tableName) {

		boolean exist = false;

		try {
			String create_sql = "CREATE TABLE " + tableName + " (" + "OID int not null primary key ,"
					+ " EXECUTE_TIME DATE not null," + " error_id  int not null, " + "happend_count int not null,statistic_type varchar(50)  )";

			logger.info("create_sql : " + create_sql);
			if (this.conn == null) {
				initConnect();
			}
			pst = this.conn.prepareStatement(create_sql);
			pst.executeUpdate();
			logger.info("execute create table action ");
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

	
	
	
}
