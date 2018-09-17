package com.es.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.ErrorInfo;
import com.es.bean.ErrorStatistic;
import com.es.bean.EsResultBean;
import com.es.service.ESService;
import com.es.util.CommonUntil;
import com.es.util.DBUtil;
import com.es.util.MysqlDBUtil;
import com.es.util.OracleDBUtil;

/*
 * 对数据收集，并统计处理
 */
public class DataStatisticManager {
	private static Logger logger = Logger.getLogger(DataStatisticManager.class.getName());
	// public static String indexBoss = "boss_log"+"_" +
	// CommonUntil.getIndexFixed(0);

	public static String indexBoss = "";
	public static String indexCrm = "";
	public static String typeCrm = "";
	public static String typeBoss = "";
	
	
	public void initial(){
		ResourceBundle rb = ResourceBundle.getBundle("esSetting"); // 对应config-core.properties
		if (rb != null) {
			
			// 考虑凌晨时分 。。
			 indexBoss = rb.getString("boss.index") +"_" + CommonUntil.getIndexFixed();
		//	indexBoss = rb.getString("boss.index") ;
			indexCrm = rb.getString("crm.index") +"_" + CommonUntil.getIndexFixed();
			typeBoss = rb.getString("boss.type");
			typeCrm = rb.getString("crm.type");

		}
	}
	
	
	
	public static String aggreFieldOfBoss = "message.errorClass.keyword";
	public static String aggreFieldOfCrm = "ErrorServiceName.keyword";
	// 数据库选择 true -oracle 
	boolean dbflag = false;  

	/**
	 * 聚类并入库
	 * 
	 * @param index
	 * @param type
	 */
	public void errorInfoStatistic() {
		initial();
		
		ESService es = new ESService();
		Map<String, Long> errorCountBoss = null;
		Map<String, Long> errorCountCrm = null;
		logger.info("************ step 1 ************");
	
		
		Map<String, String> params = new HashMap<String, String>();
		// 获取当前时间 long
		Calendar calendar = Calendar.getInstance();
		// SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd
		// hh:mm:ss");
		// String currentTime = sdf.format(calendar.getTime());
		String endtime = String.valueOf(calendar.getTime().getTime());
		calendar.add(calendar.MINUTE, -5);
		String starttime = String.valueOf(calendar.getTime().getTime());
		
		params.put("starttime", starttime);
		params.put("endtime", endtime);
		
		errorCountBoss = es.getFiveMinuteErrorCount(indexBoss, typeBoss, aggreFieldOfBoss,params);
		errorCountCrm = es.getFiveMinuteErrorCount(indexCrm, typeCrm, aggreFieldOfCrm,params);
		
		logger.info("************ aggregation search  end ************");
		
		logger.info("************ step 2 ************");
		Map<String, Long> mapBoss = aggregationMapToDBMap(indexBoss, errorCountBoss);
		Map<String, Long> mapCrm = aggregationMapToDBMap(indexCrm, errorCountCrm);
		
		String errid = "" ;
		Long num = 0L ; 
		
		logger.info("************ step 3 ************");
		boolean insertFlag = statisticInfoDB(mapBoss);
		insertFlag = statisticInfoDB(mapCrm);
		
		logger.info("statistic result : " + insertFlag);
	}

	/*
	 * Map -->dbMap
	 */
	public Map aggregationMapToDBMap(String index, Map<String, Long> aggregationMap) {
		Map<String, Long> countMap = new HashMap();
		Map<String, Integer> map = new HashMap();
		
		DBUtil dbu  ;
		if(dbflag){
			System.out.println("connecting  mysql   ");
			dbu = new MysqlDBUtil();
		}else {
			System.out.println("connecting  oracle  ");
			dbu = new OracleDBUtil();
		}
		
		int insertNum = 0;
		String errorId = "";
		int count = 0;
		String errorClass = "";
		String errorCodeName = "";
		long errorCodeCont = 0;

		String[] temp_array = index.split("_");
		String errorSystem = temp_array[0];

		System.out.println("errorSystem : " + errorSystem);

		try {
			for (Map.Entry<String, Long> entry : aggregationMap.entrySet()) {
			//	errorCodeName = entry.getKey();
				errorClass = entry.getKey();
				errorCodeCont = entry.getValue();
				// 此处若是有 class ，则需要对返回聚类 再分。
				if(!"".equals(errorClass) && null != errorClass ){
					
					map = dbu.getSignalErrorInfo(errorSystem, errorClass, errorCodeName);
					
					errorId = map.get("error_id").toString();

					countMap.put(errorId, errorCodeCont);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return countMap;
	}

	/**
	 * 聚类map 入库
	 * 
	 * @param countMap
	 * @return
	 */
	public boolean statisticInfoDB(Map<String, Long> countMap) {
		boolean update_flag = false;
		String errorId = "";
		long count = 0;
		List statisticList = new ArrayList();
		ErrorStatistic errorStatistic = null;
		// 错误id 查询 -- 封装bean
		for (Map.Entry<String, Long> entry : countMap.entrySet()) {

			errorId = entry.getKey();
			count = entry.getValue();
			System.out.println("errorID : " + errorId + ". count : " + count);

			// 错误id 查询，设置error bean 2
			errorStatistic = new ErrorStatistic();
			errorStatistic.setErrorID(Integer.valueOf(errorId));
			errorStatistic.setHappendNum(count);
			errorStatistic.setStatisticType("minute");
			errorStatistic.setErrorExecuteTime(new Date());
			statisticList.add(errorStatistic);
		}
		
		DBUtil mb  ;
		if(dbflag){
			 mb = new MysqlDBUtil();
		}else {
			 mb = new OracleDBUtil();
		}
		mb.preInsertStatisticInfo(statisticList);
		mb.preUpdateErrorCount(statisticList);
		// 需要 错误归类，错误代码，错误系统，发生次数，当前时间 ,
		update_flag = true;
		return update_flag;
	}

	/*
	 * 根据给定的list 记录 处理 ---> error_code : count 即每个错误的统计数 es 数据 统计 转换,
	 * 指定归属类型（即系统）--对应esBean 的index
	 */
	public Map getTransESDataMap(List<EsResultBean> list) {

		JSONObject jsonInitObject = null;

		Map<String, Integer> countMap = new HashMap();
		String errorCode = "";
		String errorClass = "";
		int count = 0;
		String esIndex = null;
		DBUtil mb  ;
		if(!dbflag){
			 mb = new MysqlDBUtil();
		}else {
			 mb = new OracleDBUtil();
		}
		int insertNum = 0;
		Map<String, Integer> map = new HashMap();
		String errorId = "";

		try {
			for (EsResultBean esBean : list) {

				esIndex = esBean.getIndex();

				jsonInitObject = JSONObject.parseObject(esBean.getContent());

				String[] temp_array = esIndex.split("_");
				String errorSystem = temp_array[0];
				System.out.println("errorSystem : " + errorSystem);

				if (esIndex.equals("boss_log")) {
					System.out.println(jsonInitObject.getJSONObject("message"));
					errorCode = jsonInitObject.getJSONObject("message").getString("errorCode");
					// errorClass =
					// jsonInitObject.getJSONObject("message").getString("errorClass");

				} else {

					errorCode = jsonInitObject.getString("errorServiceName");

				}

				map = mb.getSignalErrorInfo(errorSystem, errorClass, errorCode);

				// 根据系统，错误代码，错误分析 查找id -- 判断是否存在该错误，若是没有 则新建
				// 此处更加返回结果 决定是否增加 记录，后期优化 序列做错误id , 可直接获取id 后插入数据。 放到获取id方法中。

				int searchNum = map.get("search_result");
				while (searchNum == 0) {

					ErrorInfo errorInfo = new ErrorInfo();
					errorInfo.setErrorSystem(errorSystem);
					errorInfo.setErrorClass(errorClass);
					errorInfo.setErrorCode(errorCode);

					insertNum = mb.preInsertErrorInfo(errorInfo);

					map = mb.getSignalErrorInfo(errorSystem, errorClass, errorCode);
					searchNum = map.get("search_result");
				}

				errorId = map.get("error_id").toString();

				if (null != countMap.get(errorId)) {
					count = countMap.get(errorId) + 1;
					countMap.put(errorId, count);
				} else {
					countMap.put(errorId, 1);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return countMap;
	}
	
	
	public void statisticHourError(){
		DBUtil dbu  ;
		if(dbflag){
			dbu = new MysqlDBUtil();
		}else {
			dbu = new OracleDBUtil();
		}
		
		dbu.preInsertHourStatisticInfo();
		logger.info(" hour statistic error info updated");
	}
	

	public void statisticDayError(){
		DBUtil dbu  ;
		if(dbflag){
			dbu = new MysqlDBUtil();
		}else {
			dbu = new OracleDBUtil();
		}
		
		dbu.preInsertDayStatisticInfo();
		logger.info(" day statistic error info updated");
	}
	
	

}
