package com.es.manager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.BossLogData;
import com.es.bean.EsResultBean;
import com.es.bean.PageResp;

import com.es.util.StringUtils;



public class logFormatManager {
	/**
	 * 获取boss原生数据格式化
	 * 
	 */
	public List<JSONObject> getLogData(int num) {
	
		// 配置es搜索条件
		Map<String, String> params = new HashMap<String, String>();
//		params.put("es_index", "boss_log");
		params.put("es_index", "boss_logdata");
		// 获取文件总数，分页
		PageResp page = queryCrmTradeLogInfo(0, 2, params);
//		int size = 0;
//		if (page.getTotal() == 0)
//			return null;
//		size = (int) (page.getTotal() % 2000 == 0 ? page.getTotal() / 2000 : page.getTotal() / 2000 + 1);
		
	
		
		// 储存对象指标名称，判断是否重复
		List<JSONObject> list = null ;
		// 储存对象基本属性
//		System.out.println("总页数为==="+size);
		// //每页设置为50个，分页 查询，
//		if(size >= 500)size = 500;
//		for (int i = 0; i < size; i++) {
			System.out.println("读取到第"+num+"页");
			page = queryCrmTradeLogInfo(num, 2000, params);
			if (page.getDataList().size() == 0)
				return null;
			List<EsResultBean> value = (List<EsResultBean>) page.getDataList();
			list = new ArrayList<JSONObject>();
			// 遍历当前页数据
			for (int j = 0; j < value.size(); j++) {
				JSONObject object = JSONObject.parseObject(value.get(j).getContent());
				
				list.add(object);
			}
			

//		}
		
		
		
		return list;
		
	}
	
	
	
	/**
	 * 获取es查询数据的总页数
	 * */
	public int getDataTotal(){
		// 配置es搜索条件
		Map<String, String> params = new HashMap<String, String>();
		params.put("es_index", "boss_log");
		// 获取文件总数，分页
		PageResp page = queryCrmTradeLogInfo(0, 2, params);
		int size = 0;
		if (page.getTotal() == 0)
			return 0;
		size = (int) (page.getTotal() % 2000 == 0 ? page.getTotal() / 2000 : page.getTotal() / 2000 + 1);
		return size;
	}
	
	/**
	 * 获取格式化后的boss日志做展示
	 * */
	public List<BossLogData> getFormatData(String startTime,String endTime,String errorCode){
		// 配置es搜索条件
		Map<String, String> params = new HashMap<String, String>();
//		params.put("kpi_type", "计算资源集群与资源池独有字段属性");
		params.put("es_index", "boss_log");
		params.put("message.errorCode", errorCode);
		if(startTime != null)params.put("starttime", startTime);
		if(endTime != null)params.put("endtime", endTime);
		// 获取文件总数，分页
		List<BossLogData> list = new ArrayList<BossLogData>();
		PageResp page = queryCrmTradeLogInfo(0, 2, params);
		int size = 0;
		if (page.getTotal() == 0)
			return list;
		size = (int) (page.getTotal() % 2000 == 0 ? page.getTotal() / 2000 : page.getTotal() / 2000 + 1);
		
	
		
		// 储存对象指标名称，判断是否重复
		// 储存对象基本属性
		System.out.println("总页数为==="+size);
		BossLogData logData = null;
		// //每页设置为50个，分页 查询，
//		if(size >= 500)size = 500;
		for (int i = 0; i < size; i++) {
			System.out.println("计算到第"+i+"页");
			page = queryCrmTradeLogInfo(i, 2000, params);
			if (page.getDataList().size() == 0)
				continue;
			List<EsResultBean> value = (List<EsResultBean>) page.getDataList();
			
			// 遍历每页数据，判断对象基础属性是否存在，判断属性值是否重复
			for (int j = 0; j < value.size(); j++) {
				JSONObject object = JSONObject.parseObject(value.get(j).getContent());
//				System.out.println(JSON.toString(object));
//				SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				logData = new BossLogData();
				logData.setErrorDate(utc2Local(object.getString("@timestamp")));
				JSONObject message = object.getJSONObject("message");
				logData.setHostId(message.getString("hostId"));
				logData.setHostName(message.getString("hostName"));
				logData.setErrorDescribe(message.getString("errorDescribe"));
				logData.setErrorCode(message.getString("errorCode"));
				list.add(logData);
				
				
			}
		}
		return list;
	}
	
	public static void main(String[] args){
		/*
		logFormatManager manager = new logFormatManager();
		String errorCode = "0";
		String startTime = "2018-07-13 18:45:00";
		String endTime = "2018-07-13 18:47:00";
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<BossLogData> list = new ArrayList<BossLogData>();
		try {
			if(startTime != null)startTime =  sdFormat.parse(startTime).getTime()+"";
			if(endTime != null)endTime = sdFormat.parse(endTime).getTime()+"";
			list = manager.getFormatData(startTime,endTime,errorCode);
			System.out.println("aaa"+list.size());
		} catch (Exception e) {
			// TODO: handle exception
		}
		for (BossLogData bossLogData : list) {
			System.out.println(bossLogData);
		}
		System.out.println(list.size());
    	logFormatManager logman = new logFormatManager();
        List<String> logList = logman.getLogData();
		InputDataService inService = new InputDataService();
		inService.getLogData();
		logFormatManager lo = new logFormatManager();
		lo.getLogData(0);
	*/
		
	}
    /**
     * 函数功能描述:UTC时间转本地时间格式
     * @param utcTime UTC时间
     * @param utcTimePatten UTC时间格式
     * @param localTimePatten   本地时间格式
     * @return 本地时间格式的时间
     * eg:utc2Local("2017-06-14 09:37:50.788+08:00", "yyyy-MM-dd HH:mm:ss.SSSXXX", "yyyy-MM-dd HH:mm:ss.SSS")
     */
    public static String utc2Local(String utcTime) {
    	String localTimePatten = "yyyy-MM-dd HH:mm:ss";
    	String utcTimePatten = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));//时区定义并进行时间获取
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return utcTime;
        }
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }
    
    
	public static PageResp queryCrmTradeLogInfo(Integer pageIndex, Integer pageSize, Map<String, String> params) {
		PageResp page = new PageResp();
		Map<String, Object> mustQueryMaps = new HashMap<String, Object>();
		Map<String, Object> fullTextQueryMaps = new HashMap<String, Object>();
		Map<String, Object> wildcardQueryMaps = new HashMap<String, Object>();
		Map<String, Object> sortMaps = new HashMap<String, Object>();
		Map<String, String> rangeQuery = new HashMap<String, String>();
		try {
			if (StringUtils.isNotBlank(params.get("starttime")) && !"*".equals(params.get("starttime"))) {
//				Calendar calendar = Calendar.getInstance();
//				calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 19);
//				Date cal = calendar.getTime();
//				Long stime = cal.getTime();
//				rangeQuery.put("gte", stime + "");
//				System.out.println(params.get("starttime"));
				rangeQuery.put("gte", params.get("starttime"));
			}
			if (StringUtils.isNotBlank(params.get("endtime")) && !"*".equals(params.get("endtime"))) {
//				Long etime = new Date().getTime();
//				rangeQuery.put("lte", etime + "");
//				System.out.println(params.get("endtime"));
				rangeQuery.put("lte", params.get("endtime"));
			}

			// 精确查询
			if (StringUtils.isNotBlank(params.get("resClass"))) {
				mustQueryMaps.put("resClass", params.get("resClass").trim());
			}
			if (StringUtils.isNotBlank(params.get("kpi_type"))) {
				mustQueryMaps.put("kpi_type", params.get("kpi_type").trim());
			}
			if (StringUtils.isNotBlank(params.get("classname"))) {
				mustQueryMaps.put("classname", params.get("classname").trim());
			}
			if (StringUtils.isNotBlank(params.get("message.errorCode"))) {
				mustQueryMaps.put("message.errorCode", params.get("message.errorCode").trim());
			}
			if (null != params.get("perkey") && !"".equals(params.get("perkey"))) {
				mustQueryMaps.put("perkey", params.get("perkey"));
			}
			sortMaps.put("@timestamp", "DESC");
//			long s = System.currentTimeMillis();
//			if ("ORACLE".equals(params.get("resClass"))) {
//				page = ESUtils.getEslog(pageIndex, pageSize, "minute", params.get("es_index"), "log",
//						rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);
//			} else if ((params.get("type")) != null) {
//				page = ESUtils.getEslog(pageIndex, pageSize, "minute", params.get("es_index"), "log",
//						rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);
//			} else {
	//			page = ESUtils.getEslog(pageIndex, pageSize, "minute", params.get("es_index"), "log", rangeQuery,
//						mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);
//			}
//			long e = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return page;
	}
}
