package com.log.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Async;

import com.log.bean.DataBaseTask;
import com.log.bean.Ftp;
import com.log.bean.FtpTask;
import com.log.service.FtpLog;
import com.log.service.LogFileDB;
import com.log.util.PropertiesUtils;

public class TimerTask2 {
	// 取了ftp 文件后，删除  （.log 结尾文件 ） toptea / 
	
	
		PropertiesUtils proConfig = new PropertiesUtils("conf");

		String url = proConfig.getKeyValue("ftp.ip");
		String username = proConfig.getKeyValue("ftp.user");
		String password = proConfig.getKeyValue("ftp.password");

		
		String filePath1 = proConfig.getKeyValue("ftp.filepath1");
		String localUrl1 = proConfig.getKeyValue("local.file.path1");

		String filePath2 = proConfig.getKeyValue("ftp.filepath2");
		String localUrl2 = proConfig.getKeyValue("local.file.path2");

		String filePath3 = proConfig.getKeyValue("ftp.filepath3");
		String localUrl3 = proConfig.getKeyValue("local.file.path3");

		String filePath4 = proConfig.getKeyValue("ftp.filepath4");
		String localUrl4 = proConfig.getKeyValue("local.file.path4");
		
		String filePath5 = proConfig.getKeyValue("ftp.filepath5");
		String localUrl5 = proConfig.getKeyValue("local.file.path5");
		
		String filePath6 = proConfig.getKeyValue("ftp.filepath6");
		String localUrl6 = proConfig.getKeyValue("local.file.path6");

		String filePath7 = proConfig.getKeyValue("ftp.filepath7");
		String localUrl7 = proConfig.getKeyValue("local.file.path7");
		String filePath8 = proConfig.getKeyValue("ftp.filepath8");
		String localUrl8 = proConfig.getKeyValue("local.file.path8");
		String filePath9 = proConfig.getKeyValue("ftp.filepath9");
		String localUrl9 = proConfig.getKeyValue("local.file.path9");
		String filePath10 = proConfig.getKeyValue("ftp.filepath10");
		String localUrl10= proConfig.getKeyValue("local.file.path10");
		String filePath11 = proConfig.getKeyValue("ftp.filepath11");
		String localUrl11 = proConfig.getKeyValue("local.file.path11");
		String filePath12 = proConfig.getKeyValue("ftp.filepath12");
		String localUrl12 = proConfig.getKeyValue("local.file.path12");

		

		
		
		
		public static void main(String[] args) {
			System.out.println("application start....");
			ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");

		}
		
		@Async("threadPoolTaskExecutor")
		public void logInfoDownload() {
			
			
			Ftp fone = new Ftp();
			fone.setIpAddr(url);
			fone.setUserName(username);
			fone.setPwd(password);
			fone.setPath(filePath1);
			
			Ftp ftwo = new Ftp();
			ftwo.setIpAddr(url);
			ftwo.setUserName(username);
			ftwo.setPwd(password);
			ftwo.setPath(filePath2);
			
			Ftp fthree = new Ftp();
			fthree.setIpAddr(url);
			fthree.setUserName(username);
			fthree.setPwd(password);
			fthree.setPath(filePath3);

			Ftp ffour = new Ftp();
			ffour.setIpAddr(url);
			ffour.setUserName(username);
			ffour.setPwd(password);
			ffour.setPath(filePath4);


			Ftp ffive = new Ftp();
			ffive.setIpAddr(url);
			ffive.setUserName(username);
			ffive.setPwd(password);
			ffive.setPath(filePath5);

			Ftp fsix = new Ftp();
			fsix.setIpAddr(url);
			fsix.setUserName(username);
			fsix.setPwd(password);
			fsix.setPath(filePath6);
			

			Ftp fsev = new Ftp();
			fsev.setIpAddr(url);
			fsev.setUserName(username);
			fsev.setPwd(password);
			fsev.setPath(filePath7);

			Ftp feig = new Ftp();
			feig.setIpAddr(url);
			feig.setUserName(username);
			feig.setPwd(password);
			feig.setPath(filePath8);

			Ftp fnine = new Ftp();
			fnine.setIpAddr(url);
			fnine.setUserName(username);
			fnine.setPwd(password);
			fnine.setPath(filePath9);

			Ftp ften = new Ftp();
			ften.setIpAddr(url);
			ften.setUserName(username);
			ften.setPwd(password);
			ften.setPath(filePath10);

			Ftp feven = new Ftp();
			feven.setIpAddr(url);
			feven.setUserName(username);
			feven.setPwd(password);
			feven.setPath(filePath11);

			Ftp ftwele = new Ftp();
			ftwele.setIpAddr(url);
			ftwele.setUserName(username);
			ftwele.setPwd(password);
			ftwele.setPath(filePath12);

			
			ExecutorService threadpool = Executors.newFixedThreadPool(15);
			threadpool.execute(new FtpTask(fone,localUrl1));
			threadpool.execute(new FtpTask(ftwo,localUrl2));
			threadpool.execute(new FtpTask(fthree,localUrl3));
			threadpool.execute(new FtpTask(ffour,localUrl4));
			threadpool.execute(new FtpTask(ffive,localUrl5));
			threadpool.execute(new FtpTask(fsix,localUrl6));
			threadpool.execute(new FtpTask(fsev,localUrl7));
			threadpool.execute(new FtpTask(feig,localUrl8));
			threadpool.execute(new FtpTask(fnine,localUrl9));
			threadpool.execute(new FtpTask(ften,localUrl10));
			threadpool.execute(new FtpTask(feven,localUrl11));
			threadpool.execute(new FtpTask(ftwele,localUrl12));
			
			
			threadpool.shutdown();
		}

		@Async("threadPoolTaskExecutor")
		public void fileINfoToDB() {
			

			ExecutorService threadpool2 = Executors.newFixedThreadPool(15);
			threadpool2.execute(new DataBaseTask(localUrl1));
			threadpool2.execute(new DataBaseTask(localUrl2));
			threadpool2.execute(new DataBaseTask(localUrl3));

			threadpool2.execute(new DataBaseTask(localUrl4));
			threadpool2.execute(new DataBaseTask(localUrl5));
			threadpool2.execute(new DataBaseTask(localUrl6));
			threadpool2.execute(new DataBaseTask(localUrl7));
			threadpool2.execute(new DataBaseTask(localUrl8));
			threadpool2.execute(new DataBaseTask(localUrl9));
			threadpool2.execute(new DataBaseTask(localUrl10));
			threadpool2.execute(new DataBaseTask(localUrl11));
			threadpool2.execute(new DataBaseTask(localUrl12));

			threadpool2.shutdown();
			
		}

}
