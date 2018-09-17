package com.es.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTimeZone;

import com.alibaba.fastjson.JSONObject;
import com.es.bean.EsResultBean;
import com.es.bean.PageResp;
import com.es.manager.DataStatisticManager;


/**
 * elasticsearch工具类
 *
 * @author: susen
 * @create: 2017-05-10 13:07
 */
public class ESUtils {
    private static int MAX_SIZE = 999; //最大记录数
    
	private static Logger logger = Logger.getLogger(DataStatisticManager.class.getName());

    
    
    //统计粒度（年月周日分）
    private static Map timeInterval = new HashMap<String, DateHistogramInterval>();

    static {
        timeInterval.put("minute", DateHistogramInterval.MINUTE);
        timeInterval.put("hour", DateHistogramInterval.HOUR);
        timeInterval.put("day", DateHistogramInterval.DAY);
        timeInterval.put("week", DateHistogramInterval.WEEK);
        timeInterval.put("month", DateHistogramInterval.MONTH);
    }

    private static String CLUSTER_NAME = "";
//    private static String CLUSTER_NAME = "migu-es2";
//    private static final String CLUSTER_NAME = "elasticsearch";
    /**
     * es服务器的host
     */
    private static String HOSTS = "";
//    private static final String HOST = "192.168.11.150";

    /**
     * es服务器暴露给client的port
     */
    private static int PORT = 0;
    private static TransportClient client;
//    private static List<TransportClient> clientList;

    static {
        ResourceBundle rb = ResourceBundle.getBundle("esSetting"); // 对应config-core.properties
        if (rb != null) {
            HOSTS = rb.getString("es.hosts");
            PORT = Integer.parseInt(rb.getString("es.port"));
            CLUSTER_NAME = rb.getString("es.cluster.name");
        }
    }

    /**
     * 获得连接
     *
     * @return
     * @throws UnknownHostException
     */
    public static Client getClient() {
        try {
            if (client == null) {
                Map<String, String> map = new HashMap();
                map.put("cluster.name", CLUSTER_NAME);
//                设置client.transport.sniff为true来使客户端去嗅探整个集群的状态
                map.put("client.transport.sniff", "true");
                String[] hosts = HOSTS.split(",");
//                Settings.Builder settings = Settings.builder().put(map);//2.2.1
                Settings settings = Settings.builder().put(map).build();//5.3.4
                client = new PreBuiltTransportClient(settings);
                for (String host : hosts) {
                    InetSocketTransportAddress address = new InetSocketTransportAddress(InetAddress.getByName(host), PORT);
                    client.addTransportAddress(address);
                }
            }
            return client;
        } catch (Exception e) {
            if (client != null) {
                client.close();
            }
            throw new RuntimeException("elasticsearch 获取链接异常");
        }
    }


    /**
     * 关闭连接
     *
     * @param client
     */
    public static void close(Client client) {
      /*  if (client != null)
            client.close();*/
    }

 
    /**
     * 查询修改
     *
     * @param index             索引
     * @param type              类型
     * @param rangeQuery        范围 参数比如价格   key为   field,from,to
     * @param mustQueryMaps     精确查询参数
     * @param sortMaps          排序参数  key为   field  value传大写的 ASC , DESC
     * @param fullTextQueryMaps 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索,构造全文或关系的查询
     * @param wildcardQueryMaps 模糊匹配
     * @return 返回id
     */
    public  PageResp getEslog(int from, int size, String interval, String index, String type, Map<String, String> rangeQuery, Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps, Map<String, Object> wildcardQueryMaps) {
    	
        PageResp resp = new PageResp();
        resp = getEslogAggregation(from, size, interval, index, type, "", rangeQuery,mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);      
        return resp;
    }

    /**
     * 聚合查询
     *
     * @param index             索引
     * @param type            
     * @param rangeQuery         范围 参数比如价格   key为   field,from,to
     * @param mustQueryMaps     精确查询参数
     * @param sortMaps          排序参数  key为   field  value传大写的 ASC , DESC
     * @param fullTextQueryMaps 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索,构造全文或关系的查询
     * @return 返回id
     */
    public  PageResp getEslogAggregation(int from,int size,String interval,String index,String type,String aggregationField, Map<String, String> rangeQuery, Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps,Map<String, Object> wildcardQueryMaps) {
        logger.info("=============index:" + index);
        Client client = getClient();
        PageResp resp = new PageResp();
        SearchRequestBuilder searchRequestBuilder = null ;
        //构造查询
        if (StringUtils.isNotBlank(type)) {
            searchRequestBuilder =
                    client.prepareSearch(index.toLowerCase()).setTypes(type.toLowerCase())
                            .setFrom(from).setSize(size == 0 ? MAX_SIZE : size)
                            .setExplain(true);
        } else {
            searchRequestBuilder =
                    client.prepareSearch(index.toLowerCase())
                            .setFrom(from).setSize(size == 0 ? MAX_SIZE : size)
                            .setExplain(true);
        }
        // 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索 *
        //构造全文关系 并且查询
        BoolQueryBuilder bb = QueryBuilders.boolQuery();
        if (mustQueryMaps != null) {
            for (Object key : mustQueryMaps.keySet()) {
                //term是代表完全匹配，即不进行分词器分析，文档中必须包含整个搜索的词汇, matchQuery
                bb.must(QueryBuilders.matchQuery((String) key, String.valueOf(mustQueryMaps.get(key)).toLowerCase()).operator(Operator.AND));
//                bb.must(QueryBuilders.matchQuery((String) key, String.valueOf(mustQueryMaps.get(key)).toLowerCase()));
            }
        }

        //构造query_String查询（查询）
        if (fullTextQueryMaps != null && fullTextQueryMaps.size() > 0) {
            for (Object key : fullTextQueryMaps.keySet()) {
                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)))).analyzeWildcard(true));
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)))));
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)).toLowerCase())));
            }
        }
        //构造模糊匹配
        if (wildcardQueryMaps != null && wildcardQueryMaps.size() > 0) {
            for (Object key : wildcardQueryMaps.keySet()) {
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)).toLowerCase())));
                bb.must(QueryBuilders.wildcardQuery(key.toString(),"*"+(String.valueOf(wildcardQueryMaps.get(key)).toLowerCase())+"*"));
            }
        }
        //构造排序参数
        if (sortMaps != null && sortMaps.size() > 0) {
            for (String key : sortMaps.keySet()) {
                SortBuilder sortBuilder = SortBuilders.fieldSort(key)
                        .order(StringUtils.trim(sortMaps.get(key).toString().toUpperCase())
                                .equals("ASC") ? SortOrder.ASC : SortOrder.DESC);
                searchRequestBuilder.addSort(sortBuilder);
            }
        }
        //时间范围查询
        if (rangeQuery != null) {
            RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("@timestamp");
            if(rangeQuery.get("gte") != null) {
                rangeQueryBuilder.gte(rangeQuery.get("gte") );
            }
            if(rangeQuery.get("lte") != null) {
                rangeQueryBuilder.lte(rangeQuery.get("lte") );
            }
            bb.must(rangeQueryBuilder); //
        }

      DateHistogramAggregationBuilder aggregationBuilder = AggregationBuilders.dateHistogram("time")
                .field("@timestamp").dateHistogramInterval((DateHistogramInterval) timeInterval.get(interval))
                .timeZone(DateTimeZone.getDefault()).format("yyyy-MM-dd HH:mm:ss");

      if(!"".equals(aggregationField)){
        AggregationBuilder aggregation = AggregationBuilders.terms("errorAgg").field(aggregationField);
        searchRequestBuilder.addAggregation(aggregation);
      }else{
    	  searchRequestBuilder.addAggregation(aggregationBuilder);
      }
        logger.info("查询语句：" +  searchRequestBuilder.toString());
     //   System.out.println(searchRequestBuilder.toString());
        //查询
        SearchResponse response = searchRequestBuilder.execute().actionGet();

        //聚合
        
        Terms aggreList = response.getAggregations().get("errorAgg");
//      InternalDateHistogram aggreList = response.getAggregations().get("time");
        
       
        //取值
        SearchHits hits = response.getHits();
        long totalCount = hits.getTotalHits();// 总数
        logger.info("search total number is :" + totalCount + ", and return size is :" + hits.getHits().length);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        EsResultBean esResultBean = null;

        //转化hits
        List<EsResultBean> hitsResult = new ArrayList();
        if (totalCount > 0) {
            int count = 1;
            for (SearchHit hit : hits) {
                esResultBean = new EsResultBean();
                esResultBean.setId(count+"");
                esResultBean.setContent(hit.getSourceAsString());
                //获取对应的高亮域
                Map<String, HighlightField> result = hit.getHighlightFields();
                if(result != null && result.keySet().size() > 0) {
                    Map<String,String> hightFields = new HashMap<>();
                    for(Map.Entry<String, HighlightField> entry : result.entrySet()) {
                        String key = entry.getKey();
                        HighlightField value = entry.getValue();
                        Text[] fragments = value.getFragments();
                        StringBuilder textBuilder = new StringBuilder();
                        for(Text text : fragments) {
                            textBuilder.append(text.toString());
                        }
                        hightFields.put(key, textBuilder.toString());
                    }
                    esResultBean.setHightFields(hightFields);
                }
                hitsResult.add(esResultBean);
                count++;
            }
        }
         
      //转化聚合
        Map<String,Long> currentBuckets = new LinkedHashMap<>();
        for (Terms.Bucket b : aggreList.getBuckets()) {
            currentBuckets.put((String) b.getKey(), b.getDocCount());
        }
/*
        for (InternalDateHistogram.Bucket b : aggreList.getBuckets()) {
            currentBuckets.put(b.getKeyAsString(), b.getDocCount());
        }
*/
        resp.setData(currentBuckets);
        resp.setTotal(totalCount);
        resp.setDataList(hitsResult);
        return resp;
    }

      

    /**
     * 聚合查询
     *
     * @param index             索引
     * @param type            
     * @param rangeQuery         范围 参数比如价格   key为   field,from,to
     * @param mustQueryMaps     精确查询参数
     * @param sortMaps          排序参数  key为   field  value传大写的 ASC , DESC
     * @param fullTextQueryMaps 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索,构造全文或关系的查询
     * @return 返回id
     */
    public   Map<String,Long> getEslogAggregationMap(long from,int size,String interval,String index,String type,String aggregationField, Map<String, String> rangeQuery, Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps,Map<String, Object> wildcardQueryMaps) {
        logger.info("=============index:" + index);
        Client client = getClient();
        PageResp resp = new PageResp();
        SearchRequestBuilder searchRequestBuilder = null ;
        
        //构造查询
        if (StringUtils.isNotBlank(type)) {
            searchRequestBuilder =
                    client.prepareSearch(index.toLowerCase()).setFrom((int) from).setSize(size == 0 ? MAX_SIZE : size).setTypes(type.toLowerCase()).setExplain(true);
        } else {
            searchRequestBuilder =
                    client.prepareSearch(index.toLowerCase())
                    .setFrom((int) from).setSize(size == 0 ? MAX_SIZE : size)
                    .setExplain(true);
        }
        // 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索 *
        //构造全文关系 并且查询
        BoolQueryBuilder bb = QueryBuilders.boolQuery();
        if (mustQueryMaps != null) {
            for (Object key : mustQueryMaps.keySet()) {
                //term是代表完全匹配，即不进行分词器分析，文档中必须包含整个搜索的词汇, matchQuery
                bb.must(QueryBuilders.matchQuery((String) key, String.valueOf(mustQueryMaps.get(key)).toLowerCase()).operator(Operator.AND));
//                bb.must(QueryBuilders.matchQuery((String) key, String.valueOf(mustQueryMaps.get(key)).toLowerCase()));
            }
        }

        //构造query_String查询（查询）
        if (fullTextQueryMaps != null && fullTextQueryMaps.size() > 0) {
            for (Object key : fullTextQueryMaps.keySet()) {
                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)))).analyzeWildcard(true));
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)))));
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)).toLowerCase())));
            }
        }
        //构造模糊匹配
        if (wildcardQueryMaps != null && wildcardQueryMaps.size() > 0) {
            for (Object key : wildcardQueryMaps.keySet()) {
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)).toLowerCase())));
                bb.must(QueryBuilders.wildcardQuery(key.toString(),"*"+(String.valueOf(wildcardQueryMaps.get(key)).toLowerCase())+"*"));
            }
        }
        //构造排序参数
        if (sortMaps != null && sortMaps.size() > 0) {
            for (String key : sortMaps.keySet()) {
                SortBuilder sortBuilder = SortBuilders.fieldSort(key)
                        .order(StringUtils.trim(sortMaps.get(key).toString().toUpperCase())
                                .equals("ASC") ? SortOrder.ASC : SortOrder.DESC);
                searchRequestBuilder.addSort(sortBuilder);
            }
        }
        //时间范围查询
        if (rangeQuery != null) {
            RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("@timestamp");
            if(rangeQuery.get("gte") != null) {
            	System.out.println("the  time from  : " + rangeQuery.get("gte") );
                rangeQueryBuilder.gte(rangeQuery.get("gte") );
            }
            if(rangeQuery.get("lte") != null) {
            	System.out.println("the  time to  : " + rangeQuery.get("lte") );
                rangeQueryBuilder.lte(rangeQuery.get("lte") );
            }
            bb.must(rangeQueryBuilder); //
        }
        
        
      searchRequestBuilder.setQuery(bb);
      DateHistogramAggregationBuilder aggregationBuilder = AggregationBuilders.dateHistogram("time")
                .field("@timestamp").dateHistogramInterval((DateHistogramInterval) timeInterval.get(interval))
                .timeZone(DateTimeZone.getDefault()).format("yyyy-MM-dd HH:mm:ss");

      if(!"".equals(aggregationField)){
        AggregationBuilder aggregation = AggregationBuilders.terms("errorAgg").field(aggregationField);
        searchRequestBuilder.addAggregation(aggregation);
      }else{
    	  searchRequestBuilder.addAggregation(aggregationBuilder);
      }
        logger.info(" sql is :" +  searchRequestBuilder.toString());
     //   System.out.println(searchRequestBuilder.toString());
        //查询
        SearchResponse response = searchRequestBuilder.execute().actionGet();

        //聚合
        
        Terms aggreList = response.getAggregations().get("errorAgg");
//      InternalDateHistogram aggreList = response.getAggregations().get("time");
        
       
        //取值
        SearchHits hits = response.getHits();
        long totalCount = hits.getTotalHits();// 总数
        logger.info("search total order number is :" + totalCount + ", and return size is :" + hits.getHits().length);
             
      //转化聚合
        Map<String,Long> currentBuckets = new LinkedHashMap<>();
        for (Terms.Bucket b : aggreList.getBuckets()) {
            currentBuckets.put((String) b.getKey(), b.getDocCount());
        }
/*
        for (InternalDateHistogram.Bucket b : aggreList.getBuckets()) {
            currentBuckets.put(b.getKeyAsString(), b.getDocCount());
        }
*/
        return currentBuckets;
    }

    
	
	/**
	 * 获取es查询数据的总数
	 * */
	public  long getDataTotal(String index ,String log){
		// 配置es搜索条件
		Map<String, String> params = new HashMap<String, String>();
		params.put("es_index", index);
		params.put("es_index_type", log);
		// 获取文件总数，分页
		PageResp page = getESData(0, 2, params);
		long totalSize = 0;
		if (page.getTotal() == 0){
			return 0;
		}else{
			 totalSize= (long) page.getTotal();
		}
		
	//	totalSize = (int) (page.getTotal() % 2000 == 0 ? page.getTotal() / 2000 : page.getTotal() / 2000 + 1);
		return totalSize;
	}

	
	public  PageResp getESData(Integer pageIndex, Integer pageSize, Map<String, String> params) {
		// 根据索引获取数据
		List<JSONObject> logList = null;
		List<EsResultBean> resultList = null;
		PageResp page = new PageResp();
		try {
			// 间隔
			String interval = "minute";
			// 获取文件总数，分页
			Map<String, Object> mustQueryMaps = new HashMap<String, Object>();
			Map<String, Object> fullTextQueryMaps = new HashMap<String, Object>();
			Map<String, Object> wildcardQueryMaps = new HashMap<String, Object>();
			Map<String, Object> sortMaps = new HashMap<String, Object>();
			Map<String, String> rangeQuery = new HashMap<String, String>();

			sortMaps.put("@timestamp", "DESC");

			page = getEslogAggregation(pageIndex, pageSize, interval, params.get("es_index"), params.get("es_index_type"),"",
					rangeQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, wildcardQueryMaps);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return page;
	}
	
}