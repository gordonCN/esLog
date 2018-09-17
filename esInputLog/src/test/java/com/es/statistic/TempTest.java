package com.es.statistic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.EsResultBean;
import com.es.bean.PageResp;
import com.es.service.ESService;
import com.es.util.CommonUntil;
import com.es.util.ESUtils;

public class TempTest {
	public static void main(String[] args) {

		Calendar calendar =  Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);
		Date zero = calendar.getTime();
		SimpleDateFormat  format = new SimpleDateFormat("yyyyMMdd");
		System.out.println(format.format(zero));
		
		
		
		/*	  String indexBoss = "boss_log";
		  String indexCrm = "crm_log";
		  ESService es = new ESService();
		  int a  = 2<<2;
		  System.out.println(a);*/
/*
			PageResp page = new PageResp();
			Map<String, Object> mustQueryMaps = new HashMap<String, Object>();
			Map<String, Object> fullTextQueryMaps = new HashMap<String, Object>();
			Map<String, Object> wildcardQueryMaps = new HashMap<String, Object>();
			Map<String, Object> sortMaps = new HashMap<String, Object>();
			Map<String, String> rangeQuery = new HashMap<String, String>();

			int pageIndex = 0;

			int pageSize = 1000;
		
		  page = ESUtils.getEslogAggregation(pageIndex, pageSize, "minute", indexBoss, "log","message.errorCode.keyword",
					rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);

		  
		  Map<String,Long>  error_count = ( Map<String,Long> ) page.getData();
		  String errorCodeName = "";
		  long errorCodeCont = 0;
		  for(Map.Entry<String, Long> entry : error_count.entrySet()){
				errorCodeName = entry.getKey();
				System.out.println("errorCodeName " +errorCodeName);
				errorCodeCont = entry.getValue();
				System.out.println("errorCodeCont " +errorCodeCont);
		  }
		  
		  */
		  
		/*
		SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");	 
		Calendar  calendar= Calendar.getInstance();
		String currentTime = sdf.format(calendar.getTime());
		System.out.println("currentTime : " + currentTime);
		calendar.add(calendar.MINUTE, -5);
		String beforeTime = sdf.format(calendar.getTime());
		System.out.println("beforeTime : " + beforeTime);
		
		String a = CommonUntil.getTableFixed(0);
		System.out.println(a);
		*/
	}
}
