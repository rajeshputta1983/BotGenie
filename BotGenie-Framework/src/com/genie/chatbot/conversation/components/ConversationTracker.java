package com.genie.chatbot.conversation.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rajesh Putta
 */
public class ConversationTracker {
	
	private Map<String, ConversationNode> trackerMap=Collections.synchronizedMap(new HashMap<String, ConversationNode>());
	private Map<String, QuestionTracker> questionMap=Collections.synchronizedMap(new HashMap<String, QuestionTracker>());
	private Map<String, Map<String, String>> globalContext=Collections.synchronizedMap(new HashMap<String, Map<String, String>>());
	
	public ConversationNode getConversationNode(String guid) {
		return this.trackerMap.get(guid);
	}
	
	public void setConversationNode(String guid, ConversationNode conversation) {
		this.trackerMap.put(guid, conversation);
	}
	
	public QuestionTracker getQuestionTracker(String guid) {
		return this.questionMap.get(guid);
	}
	
	public void setQuestionTracker(String guid, QuestionTracker questionTracker) {
		this.questionMap.put(guid, questionTracker);
	}
	
	public void setGlobalContext(String guid, Map<String, String> context) {
		this.globalContext.put(guid, context);
	}
	
	public void updateGlobalContext(String guid, String key, String value) {
		this.globalContext.get(guid).put(key, value);
	}
	
	public Map<String, String> getGlobalContext(String guid) {
		return this.globalContext.get(guid);
	}
	
}
