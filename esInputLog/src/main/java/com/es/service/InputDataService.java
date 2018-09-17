package com.es.service;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.Data2EsResultBean;
import com.es.manager.logFormatManager;
import com.es.util.CommonUntil;
import com.es.util.ESUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InputDataService {
    private static long dateNum = 60*60*8*1000;//一天的时间毫秒值

    public static void main(String[] args) throws Exception {
    /*    Data2EsResultBean resultBean = new Data2EsResultBean();
        resultBean.setStart(System.currentTimeMillis());

        File file = new File("D:\\eclipse\\workspace\\esInputData\\data.txt");
        // 开始取日志文件
        BufferedReader bufferedReader = null;
        List<String> logList = new ArrayList<String>();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logList.add(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
*/
        


    }
    
    
    //获取boss原生日志，格式化后存入es
    public void getLogData(){
    	logFormatManager logman = new logFormatManager();
    	int num = logman.getDataTotal();
    	
    	final Client client = ESUtils.getClient();
    	for(int i = 0; i < num;i++){
    		
    		List<JSONObject> logList = logman.getLogData(i);
    		if(logList == null)continue;
    		//将数据入es
    		Data2EsResultBean resultBean = new Data2EsResultBean();
    		resultBean.setStart(System.currentTimeMillis());
    		Boolean result = false;
    		try {
    			
    			final BulkRequestBuilder bulkRequest = client.prepareBulk();
    			Map<String,Object> map = new HashMap<String,Object>();
//    			Date date = new Date(new Date().getTime());
    			for (JSONObject object: logList) {
    				Map<String,Object> datamaps = new HashMap<String,Object>();
    				//TODO 传入时间
    				datamaps.put("@timestamp",object.getString("@timestamp"));
    				String string = object.getString("message");
    				datamaps.put("datatype","logs");
    				String[] arr = string.split("`");
    				map.put("hostName", arr[1]);
    				map.put("hostId", arr[2]);
    				map.put("errorCode", arr[12]);
    				map.put("errorDescribe", arr[13]);
    				datamaps.put("message",map);
    				String indexflag =  CommonUntil.getIndexFixed();
    				bulkRequest.add(client.prepareIndex("boss_formatlog"+"_" + indexflag, "logs").setSource(datamaps));
    			}
    			// 提交进es
    			System.out.println("开始提交es");
    			if (bulkRequest != null) {
    				bulkRequest.execute().actionGet();
    			}
    			
    			result = true;
    		} catch (Exception e) {
    			e.printStackTrace();
    		} finally {
    			resultBean.setEnd(System.currentTimeMillis());
    			resultBean.setResult(result);
    			resultBean.setCost((resultBean.getEnd() - resultBean.getStart()) / 1000);
    		}
    	}
    	client.close();
    }
        
        
        
	
    


}
