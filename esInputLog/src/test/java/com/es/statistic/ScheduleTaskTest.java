package com.es.statistic;

import java.util.Map;

import org.apache.log4j.Logger;

import com.es.bean.ErrorInfo;
import com.es.util.MysqlDBUtil;

public class ScheduleTaskTest {
	private static Logger logger = Logger.getLogger(ScheduleTaskTest.class.getName());

	public static void main(String[] args) throws Exception {

		// 获取每条记录的 各字段 值 ，验证放入 error_class ,
		// 根据 错误分类，错误代码，找到对应的id，如果没有则新增
		String update_sql = null ; 

		MysqlDBUtil mdb = new MysqlDBUtil();
		// 统计结果数据 
		int error_statistic_num =1 ; 
		
		String errorSystem = "crm";
		String errorClass = "t";
		String errorCode = "testerror"; 
		Map<String, Integer> error_info_map = mdb.getSignalErrorInfo("loan_error_info", errorClass, errorCode);
		// 1条结果 --对应错误 则 count+x
		if (error_info_map.get("search_result") == 1) {
			int error_sum_count = error_info_map.get("error_sum_count");
			int error_id =  error_info_map.get("error_id");
			System.out.println("error_sum_count : " + error_sum_count);
			
			//count + x
			
			error_sum_count = error_sum_count + error_statistic_num ;
	//		int updata_num =  mdb.preUpdateErrorCount(error_id,error_sum_count);
			 
			
		} else if (error_info_map.get("search_result") == 0) {
			ErrorInfo ei = new ErrorInfo();
			ei.setErrorSystem(errorSystem);
			ei.setErrorClass(errorClass);
			ei.setErrorCode(errorCode);
			try {
				mdb.preInsertErrorInfo(ei);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.warn("存在重复的 错误类型 :"+errorClass +" 与错误代码 ： "+errorCode);
			//count ++ 
			
		}

	}

}
