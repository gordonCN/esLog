package com.es.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/20 0020.
 */
public class EsResultBean {
    private String index;
    private String type;
    private String id;              //序号
    private String resId;
    private String time;            //时间
    private String content;         //内容
    Map<String, String> hightFields = new HashMap<String, String>();

    public Map<String, String> getHightFields() {
        return hightFields;
    }

    public void setHightFields(Map<String, String> hightFields) {
        this.hightFields = hightFields;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}
    
}
