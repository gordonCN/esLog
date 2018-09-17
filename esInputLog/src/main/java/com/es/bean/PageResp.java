package com.es.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回
 *
 * @author: susen
 * @create: 2017-05-19 15:57
 */
public class PageResp implements Serializable {

	private static final long serialVersionUID = 1640668745383816130L;
	private List<?> dataList; //返回数据
    private long total;//总记录数
    private Object data; //数据


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    
    public List<?> getDataList() {
        return dataList;
    }

    public void setDataList(List<?> dataList) {
        this.dataList = dataList;
    }

	@Override
	public String toString() {
		return "PageResp [dataList=" + dataList + ", total=" + total + ", data=" + data + "]";
	}
}
