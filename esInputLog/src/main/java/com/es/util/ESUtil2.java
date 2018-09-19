package com.es.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTimeZone;

import com.es.bean.EsResultBean;
import com.es.bean.PageResp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * elasticsearch工具类
 *
 * @author: susen
 * @create: 2017-05-10 13:07
 */
public class ESUtil2 {
    private static int MAX_SIZE = 100; //最大记录数
    //统计粒度（年月周日分）
    private static Map timeInterval = new HashMap<String,DateHistogramInterval>();
    static {
        timeInterval.put("minute",DateHistogramInterval.MINUTE);
        timeInterval.put("hour",DateHistogramInterval.HOUR);
        timeInterval.put("day",DateHistogramInterval.DAY);
        timeInterval.put("week",DateHistogramInterval.WEEK);
        timeInterval.put("month",DateHistogramInterval.MONTH);
    }
    private static Log log = LogFactory.getLog(ESUtils.class);
    private static String CLUSTER_NAME = "elasticsearch";
//    private static String CLUSTER_NAME = "migu-es2";
//    private static final String CLUSTER_NAME = "elasticsearch";
    /**
     * es服务器的host
     */
    private static String HOSTS = "192.168.146.128";
//    private static final String HOST = "192.168.11.150";

    /**
     * es服务器暴露给client的port
     */
    private static int PORT = 9300;
    private static TransportClient client;
//    private static List<TransportClient> clientList;

   /* static {
        ResourceBundle rb = ResourceBundle.getBundle("config-core"); //对应config-core.properties
        if (rb != null) {
            HOSTS = rb.getString("es.hosts");
            PORT = Integer.parseInt(rb.getString("es.port"));
            CLUSTER_NAME = rb.getString("es.cluster.name");
        }
    }*/

    /**
     * 获得连接
     *
     * @return
     * @throws UnknownHostException
     */
    public static Client getClient() {
        try {
            if (client == null) {
                log.info("HOSTS:" + HOSTS);
                log.info("PORT:" + PORT);
                log.info("CLUSTER_NAME:" + CLUSTER_NAME);
                Map<String, String> map = new HashMap();
                map.put("cluster.name", CLUSTER_NAME);
//                设置client.transport.sniff为true来使客户端去嗅探整个集群的状态
                map.put("client.transport.sniff", "true");
                String[] hosts = HOSTS.split(",");
//                Settings.Builder settings = Settings.builder().put(map);//2.2.1
                Settings settings = Settings.builder().put(map).build();//5.3.4

                client = new PreBuiltTransportClient(settings);
                for (String host : hosts) {
                    InetSocketTransportAddress address =  new InetSocketTransportAddress(InetAddress.getByName(host),PORT);
                    client.addTransportAddress(address);
                }
            }
            return client;
        } catch (Exception e) {
            log.error(e.getMessage());
            if(client != null) {
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
     * 批量新增记录  注意 下面有个map.get(id) 也就是物理表的id
     *
     * @param index        索引
     * @param type         类型
     * @param uniqueIdName 主键字段名
     */
    public static void addDocuments(Map<String, Object> map, String index, String type, String uniqueIdName) {
        Client client = getClient();
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            //遍历map所有field,构造插入对象
            XContentBuilder xb = XContentFactory.jsonBuilder().startObject();
            for (String key : map.keySet()) {
                xb.field(key, map.get(key));
            }
            xb.endObject();
            //id尽量为物理表的主键
            bulkRequest.add(client.prepareIndex(index.toLowerCase(), type.toLowerCase(), StringUtils.trim(map.get(uniqueIdName).toString())).setSource(xb));
            BulkResponse bulkResponse = bulkRequest.get();
            // 处理错误信息
            checkError(bulkResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            close(client);
        }
    }

    private static void checkError(BulkResponse bulkResponse) {
        if (bulkResponse.hasFailures()) {
            log.error("====================批量创建索引过程中出现错误 下面是错误信息==========================");
            long count = 0L;
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                log.error("发生错误的 索引id为 : " + bulkItemResponse.getId() + " ，错误信息为：" + bulkItemResponse.getFailureMessage());
                count++;
            }
            log.error("====================批量创建索引过程中出现错误 上面是错误信息 共有: " + count + " 条记录==========================");
        }
    }

    /**
     * 批量新增记录  注意 下面有个map.get(id) 也就是物理表的id
     *
     * @param list         数据
     * @param index        索引
     * @param type         类型
     * @param uniqueIdName 主键字段名
     */
    public static void addDocuments(List<Map<String, Object>> list, String index, String type, String uniqueIdName) {
        Client client = getClient();
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            for (Map<String, Object> map : list) {
                //遍历map所有field,构造插入对象
                XContentBuilder xb = XContentFactory.jsonBuilder().startObject();
                for (String key : map.keySet()) {
                    xb.field(key, map.get(key));
                }
                xb.endObject();
                //id尽量为物理表的主键
                bulkRequest.add(client.prepareIndex(index.toLowerCase(), type.toLowerCase(), StringUtils.trim(map.get(uniqueIdName).toString())).setSource(xb));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            // 处理错误信息
            checkError(bulkResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            close(client);
        }
    }

    /**
     * 获取某条信息
     *
     * @param index 索引
     * @param type  类型
     * @param id    主键编码
     * @return
     */
    public static Map<String, Object> getDocument(String index, String type, String id) {
        Client client = getClient();
        GetResponse response = client.prepareGet(index, type, id).get();
        Map<String, Object> map = response.getSource();
        client.close();
        return map;
    }

    /**
     * 检索
     *
     * @param index             索引
     * @param type              类型
     * @param rangQuery         范围 参数比如价格   key为   field,from,to
     * @param mustQueryMaps     精确查询参数
     * @param sortMaps          排序参数  key为   field  value传大写的 ASC , DESC
     * @param fullTextQueryMaps 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索,构造全文或关系的查询
     * @return 返回检索的所有字段
     */
    public static List<Map<String, Object>> queryDocuments(int from, int size, String index, String type, RangeQueryBuilder rangQuery,
                                                           Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps) {
        Client client = getClient();
        try {
            SearchHits hits = getSearchHits(from, size, index, type, rangQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, client);

            List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
            List<String> idList = new ArrayList<String>();
            for (SearchHit hit : hits) {
                lists.add(hit.getSource());
                idList.add(hit.getId());
            }
            return lists;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(client);
        }
        return null;
    }

    /**
     * 检索
     *
     * @param index             索引
     * @param type              类型
     * @param rangQuery         范围 参数比如价格   key为   field,from,to
     * @param mustQueryMaps    精确查询参数
     * @param sortMaps          排序参数  key为   field  value传大写的 ASC , DESC
     * @param fullTextQueryMaps 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索,构造全文或关系的查询
     * @return 返回id
     */
    public static PageResp getIdList(int from, int size, String index, String type, RangeQueryBuilder rangQuery,
                                     Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps) {
        Client client = getClient();
        PageResp resp = new PageResp();
        List<String> idList = new ArrayList<String>();
        try {
            SearchHits hits = getSearchHits(from, size, index, type, rangQuery, mustQueryMaps, sortMaps, fullTextQueryMaps, client);
            for (SearchHit hit : hits) {
                idList.add(hit.getId());
                System.out.println("SS"+hit.getSource().get("REQUESTTIME"));
                System.out.println("ID"+hit.getId());
            }
            long totalHits = hits.getTotalHits();
            resp.setDataList(idList);
            resp.setTotal(totalHits);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(client);
        }
        return resp;
    }

    private static SearchHits getSearchHits(int from, int size, String index, String type, RangeQueryBuilder rangQuery, Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps, Client client) {
         log.info("=============index:" + index + ", type:" + type);
        //构造查询
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index.toLowerCase())
                .setTypes(type.toLowerCase()).setFrom(from).setSize(size == 0 ? MAX_SIZE : size)
                .setExplain(true);

//        QueryBuilders qb = null;

        // 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索 *
        //构造全文或关系的查询
        BoolQueryBuilder bb = QueryBuilders.boolQuery();;
        if (fullTextQueryMaps != null && fullTextQueryMaps.size() > 0) {
            for (Object key : fullTextQueryMaps.keySet()) {
                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)).toLowerCase())));
            }
        }
        //构造精确的并且查询
        if (mustQueryMaps != null && mustQueryMaps.size() > 0) {
            for (String key : mustQueryMaps.keySet()) {
//                bb.must(QueryBuilders.queryStringQuery(String.valueOf(mustQueryMaps.get(key)).toLowerCase()));
//                bb.must(QueryBuilders.termQuery((String) key, String.valueOf(mustQueryMaps.get(key)).toLowerCase()));
                bb.must(QueryBuilders.matchQuery((String) key, String.valueOf(mustQueryMaps.get(key)).toLowerCase()).operator(Operator.AND));
            }
        }
        searchRequestBuilder.setQuery(bb);
        //构造排序参数
        if (sortMaps != null && sortMaps.size() > 0) {
            for (String key : sortMaps.keySet()) {
                SortBuilder sortBuilder = SortBuilders.fieldSort(key).order(StringUtils.trim(sortMaps.get(key).toString().toUpperCase()).equals("ASC") ? SortOrder.ASC : SortOrder.DESC);
                searchRequestBuilder.addSort(sortBuilder);
            }
        }
        //范围查询
        if (rangQuery != null) {
            searchRequestBuilder.setPostFilter(rangQuery);
        }
        //查询
        SearchResponse response = searchRequestBuilder.execute().actionGet();
        //取值
        SearchHits hits = response.getHits();
        long totalCount = hits.getTotalHits();// 总数
        log.info("查询到的总记录数：" + totalCount + ", 显示记录数:" + hits.getHits().length);
        return hits;
    }

    /**
     * 查询修改
     *
     * @param index             索引
     * @param rangeQuery         范围 参数比如价格   key为   field,from,to
     * @param mustQueryMaps     精确查询参数
     * @param sortMaps          排序参数  key为   field  value传大写的 ASC , DESC
     * @param fullTextQueryMaps 下面这一段是构造bool嵌套，就是构造一个在满足精确查找的条件下，再去进行多字段的或者关系的全文检索,构造全文或关系的查询
     * @return 返回id
     */

    public static PageResp getEslog(int from,int size,String interval,String index, Map<String, String> rangeQuery, Map<String, Object> mustQueryMaps, Map<String, Object> sortMaps, Map<String, Object> fullTextQueryMaps) {
        log.info("=============index:" + index);
        Client client = getClient();
        PageResp resp = new PageResp();

        //构造查询
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index.toLowerCase())
//                .setFrom(from).setSize(size == 0 ? MAX_SIZE : size)
                .setExplain(true);
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

        //构造模糊查询（查询）
        if (fullTextQueryMaps != null && fullTextQueryMaps.size() > 0) {
            for (Object key : fullTextQueryMaps.keySet()) {
                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)))).analyzeWildcard(true));
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)))));
//                bb.must(QueryBuilders.queryStringQuery((String.valueOf(fullTextQueryMaps.get(key)).toLowerCase())));
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

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false).fragmentSize(2147483647);
        highlightBuilder.preTags("<mark>");//设置前缀
        highlightBuilder.postTags("</mark>");//设置后缀

        searchRequestBuilder.setQuery(bb).highlighter(highlightBuilder); //设置查询及高亮
        DateHistogramAggregationBuilder aggregationBuilder = AggregationBuilders.dateHistogram("time")
                .field("@timestamp").dateHistogramInterval((DateHistogramInterval) timeInterval.get(interval))
                .timeZone(DateTimeZone.getDefault()).format("yyyy-MM-dd HH:mm:ss");
        //aggs 参数
        searchRequestBuilder.addAggregation(aggregationBuilder);

        //TODO explain   111111111111111111111111
        searchRequestBuilder.setExplain(true);

        Date startdate = new Date();
        //TODO 。。。。。。。。。。。。
//        System.out.println(searchRequestBuilder.toString());
        System.out.println("startTime:"+startdate);
        //查询
        SearchResponse response = searchRequestBuilder.execute().actionGet();

        Date enddate = new Date();
        System.out.println("enddate"+enddate);
        long usetime =  enddate.getTime()-startdate.getTime();
        System.out.println("usedate:"+usetime/1000+"s");

        log.info("查询语句：" +  searchRequestBuilder.toString());
        //聚合
        InternalDateHistogram aggreList = response.getAggregations().get("time");

        //取值
        SearchHits hits = response.getHits();
        long totalCount = hits.getTotalHits();// 总数s
        log.info("查询到的总记录数：" + totalCount + ", 显示记录数:" + hits.getHits().length);
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
                    Map<String,String> hightFields = new HashMap<String,String>();
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
        Map<String,Long> currentBuckets = new LinkedHashMap<String,Long>();
        for (InternalDateHistogram.Bucket b : aggreList.getBuckets()) {
            currentBuckets.put(b.getKeyAsString(), b.getDocCount());
        }
        resp.setData(currentBuckets);
        resp.setTotal(totalCount);
        resp.setDataList(hitsResult);
        return resp;
    }

}