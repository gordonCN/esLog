package com.es.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUntil {
	
	public static void main(String[] args) {
		System.out.println(getTableFixed(0));
	}
	
	
	public static String  getDay(int num ){
		Calendar calendar =  Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, num);
		Date zero = calendar.getTime();
		SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(zero);
	}

	public static String  getTableFixed(int num ){
		Calendar calendar =  Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, num);
		Date zero = calendar.getTime();
		SimpleDateFormat  format = new SimpleDateFormat("yyyyMM");
		return format.format(zero);
	}
	// es index 后缀
	public static String  getIndexFixed(){
		Calendar calendar =  Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE,-5);
		Date zero = calendar.getTime();
		SimpleDateFormat  format = new SimpleDateFormat("yyyyMMdd");
		return format.format(zero);
	}
}
