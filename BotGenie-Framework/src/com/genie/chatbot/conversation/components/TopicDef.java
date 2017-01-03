package com.genie.chatbot.conversation.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.genie.chatbot.conversation.preprocessors.PatternInfo;
import com.genie.chatbot.conversation.preprocessors.PreprocessorFactory;

/**
 * @author Rajesh Putta
 */
public class TopicDef implements Serializable {

	private static final long serialVersionUID = 1291617265333119021L;

	private String id=null;
	private Pattern regex=null;
	private Pattern pattern=null;
	private String question=null;
	private boolean isJumpToTopicType=false;
	private Map<String, TopicRuleDef> children=new LinkedHashMap<String, TopicRuleDef>();
	private Map<String, String[]> keywordPatternMap=new LinkedHashMap<String, String[]>();	
	
	private Map<String, String> variableMap=new HashMap<String, String>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Pattern getRegex() {
		return regex;
	}
	public void setRegex(String pattern) {
		PatternInfo patternInfo=PreprocessorFactory.getInstance().getPreprocessor(PatternType.REGEX_PATTERN).preProcess(pattern, this.variableMap);
		this.regex = patternInfo.getPattern();
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public Map<String, String> getVariableMap() {
		return variableMap;
	}
	
	public void setVariableMap(Map<String, String> variableMap) {
		this.variableMap = variableMap;
	}
	
	public void addToVariableMap(Map<String, String> variableMap) {
		this.variableMap.putAll(variableMap);
	}
	public void setPattern(String pattern) {
		PatternInfo patternInfo=PreprocessorFactory.getInstance().getPreprocessor(PatternType.SIMPLE_PATTERN).preProcess(pattern, this.variableMap);
		this.pattern = patternInfo.getPattern(); 
		this.keywordPatternMap.putAll(patternInfo.getKeywordPatternMap());
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public void setChildren(Map<String, TopicRuleDef> children) {
		this.children = children;
	}
	public Map<String, TopicRuleDef> getChildren() {
		return children;
	}
	public void addChild(String id, TopicRuleDef child) {
		this.children.put(id, child);
	}
	
	public Map<String, String[]> getKeywordPatternMap() {
		return keywordPatternMap;
	}
	
	public void setJumpToTopicType(boolean isJumpToTopicType) {
		this.isJumpToTopicType = isJumpToTopicType;
	}
	
	public boolean isJumpToTopicType() {
		return isJumpToTopicType;
	}
}
