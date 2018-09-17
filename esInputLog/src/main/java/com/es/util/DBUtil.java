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


public abstract class DBUtil {

	public static PropertiesUtils dbPro = new PropertiesUtils("jdbc");

	public static final String url = dbPro.getKeyValue("jdbc.url");
	public static final String driver = dbPro.getKeyValue("jdbc.driver");
	public static final String username = dbPro.getKeyValue("jdbc.username");
	public static final String password = dbPro.getKeyValue("jdbc.password");
	
	
	public static  String statisticTatableBaseName = dbPro.getKeyValue("table.basename");

	public Connection conn = null;
	public PreparedStatement pst = null;
	public ResultSet rs = null;

	private static Logger logger = Logger.getLogger(DBUtil.class.getName());

		
	public  void initConnect(){
		if (this.conn == null) {
			try {
		//		PropertyConfigurator.configure(pro.getKeyValue("log4j.setting.file.path"));
				System.out.println("start connect database..");
				Class.forName(driver);
				System.out.println("username :" +  username );
				this.conn = DriverManager.getConnection(url, username, password);
		        System.out.println("connect success.");
				
			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public DBUtil() {
		initConnect();
	}

	public abstract  Map getSignalErrorInfo(String errorSystem, String errorClass, String errorCode);
	
	
	// public int preUpdateErrorCount(int errorId, int count) {
	public abstract int preUpdateErrorCount(List<ErrorStatistic> updateErrors) ;

	// public int preInsertStatisticInfo(ErrorInfo errorInfo) throws Exception {
	public abstract int preInsertStatisticInfo(List<ErrorStatistic> errorStatisticList);

	public abstract boolean checkTableExist(String tableName) ;

	public  abstract boolean createTable(String tableName) ;
	public abstract int preInsertErrorInfo(ErrorInfo errorInfo) throws Exception;
	public abstract List checkInfo(String sql) ;
	public void closePortable() {
		try {
			if (pst != null)
				pst.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeCompleted() {
		try {
			if (this.conn != null)
				this.conn.close();
			if (pst != null)
				pst.close();
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public abstract int preInsertHourStatisticInfo();
	
	public abstract int preInsertDayStatisticInfo();
}
