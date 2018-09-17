package com.log.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.log.service.LogFileDB;

public class DataBaseTask implements Runnable{
	String loadUrl = null ;
	
	public DataBaseTask(String url){
		loadUrl =  url ;
	}
	
	public void run() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.out.println(" log info insert db " + sdf.format(new Date()) );
		
		// TODO Auto-generated method stub
		LogFileDB lfone = new LogFileDB();
		lfone.logINfoToDB(loadUrl);
	}
	
	
}
