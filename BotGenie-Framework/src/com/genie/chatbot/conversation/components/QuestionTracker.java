package com.genie.chatbot.conversation.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Rajesh Putta
 */
public class QuestionTracker {
	
	// Question Topics Mapping - To Keep Random Questions
	private Map<String, Boolean> questions=new HashMap<String, Boolean>();
	
	public void addAllQuestions(Map<String, Boolean> allQuestionsMap) {
		this.questions.putAll(allQuestionsMap);
	}
	
	public Map<String, Boolean> getQuestions() {
		return questions;
	}
	
	public void addQuestionTopicId(String questionTopicId) {
		this.questions.put(questionTopicId, false);
	}
	
	public void markQuestionAsked(String questionTopicId) {
		this.questions.put(questionTopicId, true);
	}
	
	public String nextRandomQuestion() {
		
		Set<Entry<String, Boolean>> entrySet=questions.entrySet();
		
		for(Entry<String, Boolean> entry: entrySet) {
			
			if(!entry.getValue()) {
				return entry.getKey();
			}
		}
		
		return null;
	}
}
