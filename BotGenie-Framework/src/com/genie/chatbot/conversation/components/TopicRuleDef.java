package com.genie.chatbot.conversation.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.genie.chatbot.conversation.preprocessors.PatternInfo;
import com.genie.chatbot.conversation.preprocessors.PreprocessorFactory;

/**
 * @author Rajesh Putta
 */
public class TopicRuleDef implements Serializable {

	private static final long serialVersionUID = -8077303936810693603L;

	private String id=null;
	private Pattern regex=null;
	private Pattern pattern=null;
	private String jumpToTopicId=null;
	private boolean breakConversation=true;
	private boolean backTrack=true;
	private String content=null;
	private NodeType nodeType=NodeType.INTENT;
	private Map<String, TopicRuleDef> children=new LinkedHashMap<String, TopicRuleDef>();
	private List<TopicRuleDef> elseChildren=new ArrayList<TopicRuleDef>();
	private List<LearnFactDef> facts=new ArrayList<LearnFactDef>();
	private String expression=null;
	private Object parent=null;
	private boolean continueLoop=false;
	private Map<String, String[]> keywordPatternMap=new LinkedHashMap<String, String[]>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Pattern getRegex() {
		return regex;
	}
	public void setRegex(String pattern, TopicDef topic) {
		PatternInfo patternInfo=PreprocessorFactory.getInstance().getPreprocessor(PatternType.REGEX_PATTERN).preProcess(pattern, topic.getVariableMap());
		this.regex = patternInfo.getPattern();
	}
	public String getJumpToTopicId() {
		return jumpToTopicId;
	}
	public void setJumpToTopicId(String jumpToTopicId) {
		this.jumpToTopicId = jumpToTopicId;
	}
	public String getDialog() {
		return content;
	}
	public void setDialog(String content) {
		this.content = content;
	}
	
	public boolean isBreakConversation() {
		return breakConversation;
	}
	
	public void setBreakConversation(boolean breakConversation) {
		this.breakConversation = breakConversation;
	}
	
	public boolean isBackTrack() {
		return backTrack;
	}
	
	public void setBackTrack(boolean backTrack) {
		this.backTrack = backTrack;
	}
	
	public void setPattern(String pattern, TopicDef topic) {
		PatternInfo patternInfo=PreprocessorFactory.getInstance().getPreprocessor(PatternType.SIMPLE_PATTERN).preProcess(pattern, topic.getVariableMap());
		this.pattern = patternInfo.getPattern();
		this.keywordPatternMap.putAll(patternInfo.getKeywordPatternMap());
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	public NodeType getNodeType() {
		return nodeType;
	}
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public void setRegex(Pattern regex) {
		this.regex = regex;
	}
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
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
	public void setElseChildren(List<TopicRuleDef> elseChildren) {
		this.elseChildren = elseChildren;
	}
	public List<TopicRuleDef> getElseChildren() {
		return elseChildren;
	}
	public void addElseChild(TopicRuleDef elseChild) {
		this.elseChildren.add(elseChild);
	}
	
	public void setParent(Object parent) {
		this.parent = parent;
	}
	public Object getParent() {
		return parent;
	}
	
	public void setContinueLoop(boolean continueLoop) {
		this.continueLoop = continueLoop;
	}
	
	public boolean isContinueLoop() {
		return continueLoop;
	}
	
	public Map<String, String[]> getKeywordPatternMap() {
		return keywordPatternMap;
	}
	
	public void addFact(String fact) {
		LearnFactDef factDef=new LearnFactDef();
		factDef.setFact(fact);
		this.facts.add(factDef);
	}
	
	public List<LearnFactDef> getFacts() {
		return facts;
	}
}
