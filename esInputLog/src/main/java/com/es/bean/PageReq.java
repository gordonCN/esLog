package com.es.bean;

/**
 * 分页返回
 *
 * @author: susen
 * @create: 2017-05-19 15:57
 */
public class PageReq {

    private int start; //开始记录数从0开始
    private int length; //每页显示的行数

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
