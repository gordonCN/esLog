package com.es.bean;

import java.util.Date;

public class ErrorStatistic {
	private int errorID ;
	private Date errorExecuteTime  ;
	private Long happendNum ;
	private String statisticType;
	
	public String getStatisticType() {
		return statisticType;
	}
	public void setStatisticType(String statisticType) {
		this.statisticType = statisticType;
	}
	public int getErrorID() {
		return errorID;
	}
	public void setErrorID(int errorID) {
		this.errorID = errorID;
	}

	public Date getErrorExecuteTime() {
		return errorExecuteTime;
	}
	public void setErrorExecuteTime(Date errorExecuteTime) {
		this.errorExecuteTime = errorExecuteTime;
	}
	public Long getHappendNum() {
		return happendNum;
	}
	public void setHappendNum(Long happendNum) {
		this.happendNum = happendNum;
	}
	
	
	
}
