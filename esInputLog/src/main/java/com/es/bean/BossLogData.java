package com.es.bean;

public class BossLogData {

	private String hostName;
	private String hostId;
	private String errorDescribe;
	private String errorCode;
	private String errorDate;
	public String getErrorDate() {
		return errorDate;
	}
	public void setErrorDate(String errorDate) {
		this.errorDate = errorDate;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getHostId() {
		return hostId;
	}
	public void setHostId(String hostId) {
		this.hostId = hostId;
	}
	public String getErrorDescribe() {
		return errorDescribe;
	}
	public void setErrorDescribe(String errorDescribe) {
		this.errorDescribe = errorDescribe;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	@Override
	public String toString() {
		return "BossLogData [hostName=" + hostName + ", hostId=" + hostId + ", errorDescribe=" + errorDescribe
				+ ", errorCode=" + errorCode + ", errorDate=" + errorDate + "]";
	}
	
	
	
}
