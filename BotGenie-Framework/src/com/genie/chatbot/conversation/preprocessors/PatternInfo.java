package com.genie.chatbot.conversation.preprocessors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Rajesh Putta
 */
public class PatternInfo {
	
	private Pattern pattern=null;
	private Map<String, String[]> keywordPatternMap=new LinkedHashMap<String, String[]>();
	
	public Pattern getPattern() {
		return pattern;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	public Map<String, String[]> getKeywordPatternMap() {
		return keywordPatternMap;
	}
	public void setKeywordPatternMap(Map<String, String[]> keywordPatternMap) {
		this.keywordPatternMap = keywordPatternMap;
	}
	public void addKeywordPattern(String key, String[] keywords) {
		this.keywordPatternMap.put(key, keywords);
	}
	public String[] getKeywordPattern(String key) {
		return this.keywordPatternMap.get(key);
	}
}
