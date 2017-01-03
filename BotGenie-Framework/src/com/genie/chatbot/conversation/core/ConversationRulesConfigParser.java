package com.genie.chatbot.conversation.core;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.components.NodeType;
import com.genie.chatbot.conversation.components.QuestionTracker;
import com.genie.chatbot.conversation.components.TopicDef;
import com.genie.chatbot.conversation.components.TopicRuleDef;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;

/**
 * @author Rajesh Putta
 */
public class ConversationRulesConfigParser extends DefaultHandler {
    
	private Map<String, String> definitionMap=new HashMap<String, String>();
	
    private Map<String, TopicDef> topicDefMap=new LinkedHashMap<String, TopicDef>();
    
    private QuestionTracker defaultQuestionTracker=new QuestionTracker();
    
    private TopicDef topic=null;
    
    private String currentConfigFile=null;
    
    private Stack<TopicRuleDef> trackerStack=new Stack<TopicRuleDef>(); 
    
    public ConversationRulesConfigParser() {
    }
    
    public void parseConfigurations(String basePath, String... configFiles) {
    	
    	if(configFiles==null || configFiles.length==0)
    	{
    		throw new ChatbotFrameworkException("config file paths to be passed...");
    	}
    	
    	for(String configFile:configFiles) {
	        try
	        {
	        	this.currentConfigFile=configFile;
	            setupParser(basePath, configFile);
	        }
	        catch(Exception e)
	        {
	            throw new ChatbotFrameworkException(e);
	        }
    	}
    }
    
    public Map<String, TopicDef> getTopicDefMap() {
		return topicDefMap;
	}
    
    public TopicDef getTopicDef(String id){
    	return this.topicDefMap.get(id);
    }
    
    public QuestionTracker getDefaultQuestionTracker() {
		return defaultQuestionTracker;
	}
    
    public void setupParser(String basePath, String configFile) throws Exception
    {
        InputStream stream=null;
        
        try
        {
            SAXParserFactory factory=SAXParserFactory.newInstance();
            SAXParser saxParser=factory.newSAXParser();
            
            stream=ConversationUtility.loadStream(basePath+File.separator+configFile, true);
            saxParser.parse(stream, this);
        }
        finally{
            if(stream!=null) {
            	stream.close();
            }
        }
    }
    
    @Override
    public void startDocument() throws SAXException {
    	this.topic=null;
    	this.trackerStack.clear();
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	if(qName.equalsIgnoreCase("topic")){
    		this.topic=new TopicDef();
    		
    		String id=attributes.getValue("id");
    		
    		if(StringUtils.isBlank(id)){
    			throw new ChatbotFrameworkException(this.currentConfigFile+" config file is not valid...found Topic without id attribute !!");
    		}
    		
    		this.topic.setId(id);
    		
    		String regex=attributes.getValue("regex");
    		
    		if(StringUtils.isNotBlank(regex)) {
    			this.topic.setRegex(regex);
    		}
    		
    		String pattern=attributes.getValue("pattern");
    		
    		if(StringUtils.isNotBlank(pattern)) {
    			this.topic.setPattern(pattern);
    		}
    		
    		String question=attributes.getValue("question");
   			this.topic.setQuestion(question);
   			
   			if(StringUtils.isNotBlank(question)) {
   				this.defaultQuestionTracker.addQuestionTopicId(id);
   			}
   			
   			if(StringUtils.isBlank(regex) && StringUtils.isBlank(pattern) && StringUtils.isBlank(question)){
   				this.topic.setJumpToTopicType(true);
   			}
   			
   			this.trackerStack.clear();
    	}
    	else if(qName.equalsIgnoreCase("mapping")) {
    		
    		String name=attributes.getValue("name");
    		String regex=attributes.getValue("regex");
    		
    		if(StringUtils.isBlank(name) || StringUtils.isBlank(regex)) {
    			throw new ChatbotFrameworkException(this.currentConfigFile+" config file is not valid...found definitions/mappings without name/pattern...");
    		}
    		
    		this.definitionMap.put(name, regex);
    	}
    	else if(qName.equalsIgnoreCase("loop")){
    		TopicRuleDef loopDef=new TopicRuleDef();
    		loopDef.setNodeType(NodeType.LOOP);

    		this.trackerStack.add(loopDef);
    	}
    	else if(qName.equalsIgnoreCase("if")){
    		TopicRuleDef ifDef=new TopicRuleDef();
    		ifDef.setNodeType(NodeType.IF);

    		String regex=attributes.getValue("regex");
    		
    		if(StringUtils.isNotBlank(regex)) {
    			ifDef.setRegex(regex, this.topic);
    		}
    		
    		String pattern=attributes.getValue("pattern");
    		
    		if(StringUtils.isNotBlank(pattern)) {
    			ifDef.setPattern(pattern, this.topic);
    		}
    		
    		String expr=attributes.getValue("expr");
    		
    		if(StringUtils.isNotBlank(expr)) {
    			ifDef.setExpression(expr);
    		}
    		
    		this.trackerStack.add(ifDef);
    	}
    	else if(qName.equalsIgnoreCase("elseif")){
    		TopicRuleDef ifDef=new TopicRuleDef();
    		ifDef.setNodeType(NodeType.ELSEIF);

    		String regex=attributes.getValue("regex");
    		
    		if(StringUtils.isNotBlank(regex)) {
    			ifDef.setRegex(regex, this.topic);
    		}
    		
    		String pattern=attributes.getValue("pattern");
    		
    		if(StringUtils.isNotBlank(pattern)) {
    			ifDef.setPattern(pattern, this.topic);
    		}
    		
    		this.trackerStack.add(ifDef);
    	}
    	else if(qName.equalsIgnoreCase("else")){
    		TopicRuleDef elseDef=new TopicRuleDef();
    		elseDef.setNodeType(NodeType.ELSE);

    		this.trackerStack.add(elseDef);
    	}
    	else if(qName.equalsIgnoreCase("intent")){
	    		String dialog=attributes.getValue("dialog");

	    		TopicRuleDef ruleDef=new TopicRuleDef();
	    		ruleDef.setNodeType(NodeType.INTENT);
	    		
	    		if(StringUtils.isNotBlank(dialog)){
	    			ruleDef.setDialog(dialog.trim());
	    		}

	    		String regex=attributes.getValue("regex");
	    		
	    		if(StringUtils.isNotBlank(regex)) {
	    			ruleDef.setRegex(regex, this.topic);
	    		}
	    		
	    		String pattern=attributes.getValue("pattern");
	    		
	    		if(StringUtils.isNotBlank(pattern)) {
	    			ruleDef.setPattern(pattern, this.topic);
	    		}

	    		String continueLoop=attributes.getValue("continue");
	    		
	    		if(StringUtils.isNotBlank(continueLoop)) {
	    			ruleDef.setContinueLoop(Boolean.valueOf(continueLoop));
	    		}
	    		
	    		String jumpToTopicId=attributes.getValue("jumpToTopic");
	    		ruleDef.setJumpToTopicId(jumpToTopicId);
	    		
    			if(StringUtils.isBlank(dialog) && StringUtils.isBlank(jumpToTopicId)) {
	    			throw new ChatbotFrameworkException(this.currentConfigFile+" config file is not valid...found intent Definition without dialog and jumpToTopicId..."+qName);
	    		}
	    		
	    		this.trackerStack.add(ruleDef);
    	}
    	else if(qName.equalsIgnoreCase("learn")) {
    		String fact=attributes.getValue("fact");
    		
    		if(StringUtils.isNotBlank(fact)) {
				TopicRuleDef parent=this.trackerStack.peek();
				parent.addFact(fact);
    		}
    	}
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

    	if(qName.equalsIgnoreCase("elseif") || qName.equalsIgnoreCase("else")){
			
    		TopicRuleDef def=this.trackerStack.pop();
			
			if(this.trackerStack.isEmpty() && this.topic.getChildren().isEmpty()){
				throw new ChatbotFrameworkException("Invalid configuration...found elseif/else without parent if node...");
			}
			
			if(this.trackerStack.isEmpty()) {
				Map<String, TopicRuleDef> children=this.topic.getChildren();
				TopicRuleDef ifNode=children.get(String.valueOf(children.size()-1));
				ifNode.addElseChild(def);
				def.setParent(ifNode);
			}
			else{
				TopicRuleDef parent=this.trackerStack.peek();
				int id=parent.getChildren().size();
				parent.getChildren().get(String.valueOf(id-1)).addElseChild(def);
				def.setParent(parent);
			}
    	}
    	else if(qName.equalsIgnoreCase("topic")){
    		this.topicDefMap.put(this.topic.getId(), this.topic);
    	}
    	else if(qName.equalsIgnoreCase("if") || qName.equalsIgnoreCase("intent")){
    			TopicRuleDef def=this.trackerStack.pop();
    			if(this.trackerStack.isEmpty()){
    				int id=this.topic.getChildren().size();
    				this.topic.addChild(String.valueOf(id), def);
    				def.setParent(this.topic);
    			}
    			else{
    				TopicRuleDef parent=this.trackerStack.peek();
    				int id=parent.getChildren().size();
    				
    				parent.addChild(String.valueOf(id), def);
    				def.setParent(parent);
    			}
    	}
    }
}
