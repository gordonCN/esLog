package com.log.bean;

import java.util.Date;

public class LogInfo {
	// 数据库对应字段 
	/*
	  SRC_URL       VARCHAR2(255) not null,
	  SRC_FILENAME  VARCHAR2(127) not null,
	  SRC_TYPE      VARCHAR2(24),
	  SRC_NUM       NUMBER(10) not null,
	  DES_HOST      VARCHAR2(24) not null,
	  DES_PP        VARCHAR2(24) not null,
	  DES_URL       VARCHAR2(255) not null,
	  DES_FILENAME  VARCHAR2(127) not null,
	  DES_NUM       NUMBER(10) not null,
	  DES_ERROR     NUMBER(10),
	  DES_BEGINTIME DATE not null,
	  DES_ENDTIME   DATE,
	  MONTH         NUMBER(2) not null
	  */
	  
	String src_url;
	String src_filename;
	String src_type;
	int src_num;
	String src_host;
	String des_pp;
	String des_url;
	String des_filename;
	
	int des_num;
	int des_error;
	Date des_begintime;  // YYYYMMDDHHMISS
	Date des_endtime;
	int month ; // begintime 内的月份值
	
	
	
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public String getSrc_url() {
		return src_url;
	}
	public void setSrc_url(String src_url) {
		this.src_url = src_url;
	}
	public String getSrc_filename() {
		return src_filename;
	}
	public void setSrc_filename(String src_filename) {
		this.src_filename = src_filename;
	}
	public String getSrc_type() {
		return src_type;
	}
	public void setSrc_type(String src_type) {
		this.src_type = src_type;
	}
	public int getSrc_num() {
		return src_num;
	}
	public void setSrc_num(int src_num) {
		this.src_num = src_num;
	}
	public String getSrc_host() {
		return src_host;
	}
	public void setSrc_host(String src_host) {
		this.src_host = src_host;
	}
	public int getDes_num() {
		return des_num;
	}
	public void setDes_num(int des_num) {
		this.des_num = des_num;
	}
	public int getDes_error() {
		return des_error;
	}
	public void setDes_error(int des_error) {
		this.des_error = des_error;
	}
	public Date getDes_begintime() {
		return des_begintime;
	}
	public void setDes_begintime(Date des_begintime) {
		this.des_begintime = des_begintime;
	}
	public Date getDes_endtime() {
		return des_endtime;
	}
	public void setDes_endtime(Date des_endtime) {
		this.des_endtime = des_endtime;
	}
	public String getDes_url() {
		return des_url;
	}
	public void setDes_url(String des_url) {
		this.des_url = des_url;
	}
	public String getDes_filename() {
		return des_filename;
	}
	public void setDes_filename(String des_filename) {
		this.des_filename = des_filename;
	}
	public String getDes_pp() {
		return des_pp;
	}
	public void setDes_pp(String des_pp) {
		this.des_pp = des_pp;
	}

}
