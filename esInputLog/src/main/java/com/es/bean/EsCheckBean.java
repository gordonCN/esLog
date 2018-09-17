package com.es.bean;

/**
 * Created by Administrator on 2017/7/12 0012.
 * 日志检索参数
 */
public class EsCheckBean extends PageReq {
    private String compType;    //类型
    private String starttime;   //开始时间
    private String endtime;     //结束时间
    private String size;        //记录数
    private String interval;    //统计粒度
    private String queryString;    //关键字
    private String index;           //日志头

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setCompType(String compType) {
        this.compType = compType;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getCompType() {
        return compType;
    }

    public String getStarttime() {
        return starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public String getSize() {
        return size;
    }

    public String getInterval() {
        return interval;
    }

    public String getQueryString() {
        return queryString;
    }

}
