package com.genie.chatbot.service;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Rajesh Putta
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonTypeName("response")
public class ResponseWrapper {
	
	private String status=null;
	private String statusMessage=null;
	private String userQuery=null;
	private String result=null;
	private String conversationGuid=null;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getUserQuery() {
		return userQuery;
	}
	public void setUserQuery(String userQuery) {
		this.userQuery = userQuery;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public void setConversationGuid(String conversationGuid) {
		this.conversationGuid = conversationGuid;
	}
	public String getConversationGuid() {
		return conversationGuid;
	}
}
