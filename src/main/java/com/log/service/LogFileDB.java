package com.log.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.log.db.util.DBUtil;
import com.log.util.PropertiesUtils;

public class LogFileDB {
	// ^njppapp.*\.log$
// 	String filter = "^njppapp.*\\.log$";

	// static String destPath = "/Users/wangguodong/template";
	PropertiesUtils proConfig = new PropertiesUtils("conf");

	String filter = proConfig.getKeyValue("ftp.filter");
	Pattern fileNameFilter = Pattern.compile(filter);

	public void logINfoToDB(String destPath) {
		// destPath = "/Users/wangguodong/template";

		writeDestDirFiles(destPath);

	}

	/**
	 * @param destPath
	 */
	public void writeDestDirFiles(String destPath) {
		File entryDir = new File(destPath);
		// 如果文件夹路径不存在，则创建文件夹
		if (!entryDir.exists() || !entryDir.isDirectory()) {
			System.out.println("目录不存在");
			return;
		}
	//	System.out.println("write  log info  of " + entryDir.getPath());

		if (entryDir.isDirectory()) {
			File[] filelist = entryDir.listFiles();

		//	System.out.println("filelist length  ...." + filelist.length);
			DBUtil db = new DBUtil();
			File fileb  = null ; 
			try {

				for (int i = 0; i < filelist.length; i++) {
					if(filelist[i].isFile()) {
					if (fileNameFilter.matcher(filelist[i].getName()).matches()) {
						try {
						fileb = new File(destPath + "_" + filelist[i].getName());
						if (!fileCheck(filelist[i], fileb)) {
							continue;
						}
						db.initConnect();
						db.insertLogInfo(fileb);
						fileb.delete();
							
						} catch (Exception e) {
						//	System.out.printl);
							// 转移文件 目录 
							
						 fileb.renameTo( new File(destPath + "bak/_" + filelist[i].getName()));
						System.out.println( "read file "+ fileb.getName() +" failure ");
						e.printStackTrace();
						 continue;
					
						 
						} 
					}
					}
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
				
				System.out.println("insert info to db end " + sdf.format(new Date()));
				
			} catch (Exception e) {
			// 转移文件 目录 
				
				e.printStackTrace();

			} finally {
				db.closeCompleted();
			}
		}

	}

	private static synchronized boolean fileCheck(File from, File to) {
		return from.exists() && from.renameTo(to);
	}

}
