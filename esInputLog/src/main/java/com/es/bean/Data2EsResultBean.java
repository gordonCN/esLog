package com.es.bean;

/**
 * Created by gc on 2017/12/28.
 */
public class Data2EsResultBean implements  java.io.Serializable {
    private String threadId;
    private long start;
    private long end;
    private boolean result;
    private long cost;
 String a = "ss "; 

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
