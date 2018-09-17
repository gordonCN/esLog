package com.es.bean;

public class ErrorInfo {
	private int errorId ;
	private String errorSystem; 
	private String errorClass; 
	private String errorCode;
	private int errorSumCount;
	
	
	public int getErrorId() {
		return errorId;
	}
	public void setErrorId(int errorId) {
		this.errorId = errorId;
	}
	public String getErrorSystem() {
		return errorSystem;
	}
	public void setErrorSystem(String errorSystem) {
		this.errorSystem = errorSystem;
	}
	public String getErrorClass() {
		return errorClass;
	}
	public void setErrorClass(String errorClass) {
		this.errorClass = errorClass;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public int getErrorSumCount() {
		return errorSumCount;
	}
	public void setErrorSumCount(int errorSumCount) {
		this.errorSumCount = errorSumCount;
	}

}
