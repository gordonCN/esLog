package com.log.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.log.bean.DataBaseTask;

public class Test2 {
	public static void main(String[] args) {
		
		fileINfoToDB();
	}
	
	public static void fileINfoToDB() {
		

		ExecutorService threadpool2 = Executors.newFixedThreadPool(10);
		threadpool2.execute(new DataBaseTask("/Users/wangguodong/template/"));
	/*	threadpool2.execute(new DataBaseTask(localUrl2));
		threadpool2.execute(new DataBaseTask(localUrl4));
		threadpool2.execute(new DataBaseTask(localUrl5));
		threadpool2.execute(new DataBaseTask(localUrl6));
*/
		threadpool2.shutdown();
		
	}

}
