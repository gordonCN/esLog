package com.es.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.EsResultBean;
import com.es.bean.PageResp;
import com.es.util.ESUtils;
import com.es.util.StringUtils;

public class ESService {

	public static void main(String[] args) {
		/*  String indexBoss = "boss_log";
		  String indexCrm = "crm_log";
		  String type = "log";
		  ESService es = new ESService();
		String aggreField = "message.errorCode.keyword";
		 Map<String,Long>  error_count  = es.getFiveMinuteErrorCount(indexBoss,type,aggreField);
		  String errorCodeName = "";
		  long errorCodeCont = 0;
		  for(Map.Entry<String, Long> entry : error_count.entrySet()){
				errorCodeName = entry.getKey();
				System.out.println("errorCodeName " +errorCodeName);
				errorCodeCont = entry.getValue();
				System.out.println("errorCodeCont " +errorCodeCont);
		  }*/
	}

	// 获取es数据--指定索引 搜索字段 index type 区分 ,默认获取近5分钟数据
	public Map<String, Long> getFiveMinuteErrorCount(String index, String type,	String aggregationField,Map<String, String> params) {
		ESUtils eu = new ESUtils();
		Map<String, Long> errorCount = null;
			

		// 是否必要 ？
		int pageIndex = 0 ;
		int pageSize = 0 ; 
		PageResp page = new PageResp();
		Map<String, Object> mustQueryMaps = new HashMap<String, Object>();
		Map<String, Object> fullTextQueryMaps = new HashMap<String, Object>();
		Map<String, Object> wildcardQueryMaps = new HashMap<String, Object>();
		Map<String, Object> sortMaps = new HashMap<String, Object>();
		Map<String, String> rangeQuery = new HashMap<String, String>();
		
		
		
		if (StringUtils.isNotBlank(params.get("starttime")) && !"*".equals(params.get("starttime"))) {
			rangeQuery.put("gte", params.get("starttime"));
		}
		if (StringUtils.isNotBlank(params.get("endtime")) && !"*".equals(params.get("endtime"))) {
			rangeQuery.put("lte", params.get("endtime"));
		}
				
		sortMaps.put("@timestamp", "DESC");
		
	/*	long totalNum = eu.getDataTotal(index,type);
		int pageLimit = 10000;
        long modNum = totalNum % 10000;
        long pageNum =  modNum == 0 ? modNum : modNum +1 ;
        for(long i =0 ;i <pageNum ; i ++ ){
        	eu.getEslogAggregationMap(i * pageLimit, pageSize, "minute", index, type, aggregationField,
        			rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);
        }*/
		
		
		errorCount = eu.getEslogAggregationMap(pageIndex, pageSize, "minute", index, type, aggregationField,
				rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);
		 
		return errorCount;
	}

	// public List getESData(Map<String, String> params) {
	public List<EsResultBean> getSpecialLogList(String index, String type, int pageIndex, int pageSize) {
		// 根据索引获取数据
		Map<String, String> params = new HashMap<String, String>();
		ESUtils eu = new ESUtils();
		List<JSONObject> logList = null;
		List<EsResultBean> resultList = null;
		try {
			// 配置es搜索条件
			// 设定 es_index
			params.put("es_index", index);
			// 设定 es_index_type
			params.put("es_index_type", type);

			/*
			ESUtils eu = new ESUtils();
			int totalCount = eu.getDataTotal(index, type);

			// 获取文件总数，分页, 暂时无用
			int size = 0;
			size = (int) (totalCount % 2000 == 0 ? totalCount / 2000 : totalCount / 2000 + 1);
*/
			// 间隔
			String interval = "minute";

			PageResp page = new PageResp();
			Map<String, Object> mustQueryMaps = new HashMap<String, Object>();
			Map<String, Object> fullTextQueryMaps = new HashMap<String, Object>();
			Map<String, Object> wildcardQueryMaps = new HashMap<String, Object>();
			Map<String, Object> sortMaps = new HashMap<String, Object>();
			Map<String, String> rangeQuery = new HashMap<String, String>();

			// 范围指定 时间参数类型 long new Date().getTime()
			if (StringUtils.isNotBlank(params.get("starttime")) && !"*".equals(params.get("starttime"))) {
				rangeQuery.put("gte", params.get("starttime"));
			}
			if (StringUtils.isNotBlank(params.get("endtime")) && !"*".equals(params.get("endtime"))) {
				rangeQuery.put("lte", params.get("endtime"));
			}
			// 精确查询
			// 映射字段
			if (StringUtils.isNotBlank(params.get("message.errorClass"))) {
				mustQueryMaps.put("message.errorCode", params.get("message.errorClass").trim());
			}

			sortMaps.put("@timestamp", "DESC");

			page = eu.getEslog(pageIndex, pageSize, "minute", params.get("es_index"), params.get("es_index_type"),
					rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);

			resultList = (List<EsResultBean>) page.getDataList();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return resultList;
	}

}
