package com.es.util;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.EsResultBean;
import com.es.service.ESService;

public class DateDealUntil {
	public static void main(String[] args) {
	
	}

	// 从es获取的数据 做统计
	public Map errorCodeStatistic(List<EsResultBean> list) {

		JSONObject jsonInitObject = null;
		List<JSONObject> destList = new ArrayList<JSONObject>();
		Set<String> errorSet = new HashSet();

		Map<String, Integer> countMap = new HashMap();
		String errorCode = "" ;
		int count = 0 ; 
		for (EsResultBean esBean : list) {
			// jsonInitObject = new JSONObject();
			jsonInitObject = JSONObject.parseObject(esBean.getContent());
			errorCode = jsonInitObject.getJSONObject("message").getString("errorCode");

			if (null != errorCode && !"".equals(errorCode)) {
				if (null != countMap.get(errorCode)) {
					countMap.put(errorCode, countMap.get(errorCode) + 1);
				}else{
					countMap.put(errorCode, 1);
				}
			}

		}
		// 需要 错误归类，错误代码，错误系统，发生次数，当前时间

		return countMap; 
	}
}
