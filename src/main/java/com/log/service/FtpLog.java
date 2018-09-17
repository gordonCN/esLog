package com.log.service;

import java.util.regex.Pattern;

import com.log.bean.Ftp;
import com.log.util.FtpUtil;
import com.log.util.PropertiesUtils;

public class FtpLog {

	

	public void pullFtpLog(Ftp ftp ,String localurl) {
						// 匹配文件名
	//	String destName = "hello.txt";
		try {
			// 从FTP中下载对账文件 参数 （ftp对象，本地地址，文件名）
			FtpUtil fu = new FtpUtil() ;
			fu.startDown(ftp, localurl, ftp.getPath());//下载ftp文件测试
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
