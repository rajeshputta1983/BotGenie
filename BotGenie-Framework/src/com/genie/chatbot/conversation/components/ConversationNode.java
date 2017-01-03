package com.genie.chatbot.conversation.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rajesh Putta
 */
public class ConversationNode implements Serializable {
	
	private static final long serialVersionUID = -2178923964286431014L;
	
	private String topicId=null;
	private TopicRuleDef rule=null;
	private Map<String, String> variableMap=new HashMap<String, String>();
	
	private ConversationNode subTopic=null;
	private ConversationNode parentTopic=null;
	
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	public void setRule(TopicRuleDef rule) {
		this.rule = rule;
	}
	public TopicRuleDef getRule() {
		return rule;
	}
	public ConversationNode getSubTopic() {
		return subTopic;
	}
	public void setSubTopic(ConversationNode subTopic) {
		this.subTopic = subTopic;
	}
	public ConversationNode getParentTopic() {
		return parentTopic;
	}
	public void setParentTopic(ConversationNode parentTopic) {
		this.parentTopic = parentTopic;
	}
	
	public void setVariableMap(Map<String, String> variableMap) {
		this.variableMap.putAll(variableMap);
	}
	
	public Map<String, String> getVariableMap() {
		return variableMap;
	}
	
	public void addVariable(String variable, String value) {
		this.variableMap.put(variable, value);
	}
}
