package com.log.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.log.service.FtpLog;
import com.log.util.FtpUtil;

public class FtpTask implements Runnable{
	
	Ftp f = null ;
	
	String downloadUrl = null ;
	
	public Ftp getF() {
		return f;
	}

	public void setF(Ftp f) {
		this.f = f;
	}

	public FtpTask(Ftp ftp,String downloadurl){
		f =ftp ;
		downloadUrl = downloadurl; 
	}
	
	public void run() {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.out.println("ftp download  " + sdf.format(new Date()) );
		
		try {
			// 从FTP中下载对账文件 参数 （ftp对象，本地地址，文件名）
			FtpUtil fu = new FtpUtil() ;
			fu.startDown(f, downloadUrl, f.getPath());//下载ftp文件测试
			
		} catch (Exception e) {
			e.printStackTrace();
		
		}
	}
	

}
