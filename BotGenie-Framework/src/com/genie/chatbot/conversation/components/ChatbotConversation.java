package com.genie.chatbot.conversation.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author Rajesh Putta
 */
public class ChatbotConversation implements Serializable {

	private static final long serialVersionUID = -6923091174338948648L;
	
	private String conversationGuid=null;
	private String question=null;
	
	private List<String> responses=new ArrayList<String>();
	
	public String getConversationGuid() {
		return conversationGuid;
	}
	public void setConversationGuid(String conversationGuid) {
		this.conversationGuid = conversationGuid;
	}
	
	public void clearResponse(){
		this.responses.clear();
	}
	
	public String getResponse() {
		
		StringBuilder response=new StringBuilder();
		
		for(String content: responses){
			response.append(content).append("\r\n");
		}
		
		return response.toString();
	}
	
	public boolean isThereAnyResponse() {
		return responses.size()>0;
	}
	
	public void setResponse(String response, ConversationNode conversationNode) {
		
		if(StringUtils.isBlank(response))
			return;
		
		response=ConversationUtility.postProcessPattern(response, conversationNode!=null?conversationNode.getVariableMap():new HashMap<String, String>());
		
		this.responses.add(response);
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
}
