package com.log.test;

import java.io.File;

import com.log.service.FtpLog;
import com.log.service.LogFileDB;
import com.log.util.PropertiesUtils;

public class Test {
	public static void main(String[] args) {

		File f = new File("/Users/wangguodong/template/hello.txt");
		File entryDir = new File("/Users/wangguodong/template/");
		if (entryDir.isDirectory()) {
			File[] filelist = entryDir.listFiles();
			for (int i = 0; i < filelist.length; i++) {
				f  = filelist[i];
				File fileb = new File("/Users/wangguodong/template/bak/hello.txt");
				
				f.renameTo(fileb);
			}
			
			
		}
//		Timestamp ts = Timestamp.valueOf(a);
	
	
	}
}
