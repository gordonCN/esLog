package com.log.db.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.log.bean.LogInfo;
import com.log.util.DateUtil;
import com.log.util.PropertiesUtils;

public class DBUtil {

	public static PropertiesUtils dbPro = new PropertiesUtils("conf");

	public static final String url = dbPro.getKeyValue("jdbc.url");
	public static final String driver = dbPro.getKeyValue("jdbc.driver");
	public static final String username = dbPro.getKeyValue("jdbc.username");
	public static final String password = dbPro.getKeyValue("jdbc.password");

	String updateSql = "insert into tl_pp_log(SRC_URL,SRC_FILENAME,SRC_TYPE,SRC_NUM,DES_HOST,DES_NUM,DES_ERROR,DES_BEGINTIME,DES_ENDTIME,DES_URL,DES_FILENAME,DES_PP,MONTH) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public Connection conn = null;
	public PreparedStatement pst = null;
	public ResultSet rs = null;

	private static Logger logger = Logger.getLogger(DBUtil.class.getName());

	public void initConnect() {
		if (this.conn == null) {
			try {
				// PropertyConfigurator.configure(pro.getKeyValue("log4j.setting.file.path"));
			//	System.out.println("start connect database..");
				Class.forName(driver);
			//	System.out.println();
				this.conn = DriverManager.getConnection(url, username, password);
				System.out.println("db username :" + username+ ". connect success.");

			} catch (ClassNotFoundException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertLogInfo(File file) throws Exception {
		int updateNum = 0;

		if (file == null || !file.exists())
			return;
		ArrayList<String[]> recordlist = new ArrayList<String[]>();
		
		InputStreamReader is = null;
		BufferedReader br = null;
		try {
			is = new InputStreamReader(new FileInputStream(file), "utf-8");
			br = new BufferedReader(is);
			String lineTxt = null;
			while ((lineTxt = br.readLine()) != null) {
				// 此处应该使用空白符 分割
				recordlist.add(lineTxt.split(",", -1));
			}
		} finally {
			try {
				if (br != null)
					br.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
	//	System.out.println("parse file  : " + file.getName());
		// 入表
		long begin = System.currentTimeMillis();
		try {

		
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(updateSql);
			LogInfo li = null;
			int casetype = 0;
			int temp = 0;
			Timestamp ts = null;
			Date dateTemp = null;
			int month = 0;
	//		System.out.println("list size " + recordlist.size());

			for (int i = 0; i < recordlist.size(); i++) {
				String[] txt = recordlist.get(i); // 获取为一个数组

				if (null != txt && txt.length > 0) {
					synchronized (this) {
						for (int j = 0; j < txt.length; j++) {
							temp = j + 1;

							if (temp == 4 || temp == 6 || temp == 7) {
								casetype = 1; // 数字型
							}else if (temp == 8 || temp == 9) {
								casetype = 2; // 日期型
							}
						//	System.out.println("temp " + temp + "casetype " + casetype);

							switch (casetype) {
							case 1:
								pst.setInt(j + 1, Integer.parseInt(txt[j]));
								break;
							case 2:
//								String a = "20180904110258";
							//	20180906162907
								
								if(null !=txt[j] && !"".equals(txt[j]) ) {
								
									dateTemp = DateUtil.getFormatDate(txt[j], "yyyyMMddHHmmss");
									ts = new Timestamp(dateTemp.getTime());
									pst.setTimestamp(j + 1, ts );

								}else {
									pst.setTimestamp(j + 1, null );
								}
									
									break;
							default:
								pst.setString(j + 1, txt[j].trim().equals("") ? "null" : txt[j]);
								break;
							}

							casetype = 0;
						}
					}
			//		System.out.println("temp vvvvv");

					if(null == dateTemp ) {
						pst.setInt(13, month);
					}else {
						month = DateUtil.getSpecialMonth(dateTemp);
						pst.setInt(13, month);
					}
					
					
					pst.addBatch();
				}

				if (i % 1000 == 0) {
					pst.executeBatch();
					conn.commit();
					pst.clearBatch();
				}
			}
			pst.executeBatch();
			conn.commit();
			conn.setAutoCommit(true);
		} finally {
			
		}
	//	System.out.println(" insert log info  ok " + recordlist.size() + " expend: " + (System.currentTimeMillis() - begin));

	}

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
			System.out.println("db connection broke ");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
