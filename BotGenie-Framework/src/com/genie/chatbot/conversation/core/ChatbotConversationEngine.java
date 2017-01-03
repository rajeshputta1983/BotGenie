package com.genie.chatbot.conversation.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.context.resolvers.ContextResolverFactory;
import com.genie.chatbot.context.resolvers.FactContextResolver;
import com.genie.chatbot.context.resolvers.IContextResolver;
import com.genie.chatbot.context.resolvers.MapContextResolver;
import com.genie.chatbot.context.resolvers.ResolverType;
import com.genie.chatbot.context.resolvers.SolrContextResolver;
import com.genie.chatbot.conversation.components.ChatbotConversation;
import com.genie.chatbot.conversation.components.ChatbotRegistryBootstrap;
import com.genie.chatbot.conversation.components.ConversationNode;
import com.genie.chatbot.conversation.components.ConversationTracker;
import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.components.QuestionTracker;
import com.genie.chatbot.conversation.components.TopicDef;
import com.genie.chatbot.conversation.components.TopicRuleDef;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.conversation.pipeline.UserQuestionPipeline;
import com.genie.chatbot.conversation.smartmatch.QuestionTopicMapper;
import com.genie.chatbot.conversation.smartmatch.SmartMatcher;
import com.genie.chatbot.solr.StandardSolrClient;

/**
 * @author Rajesh Putta
 */
public class ChatbotConversationEngine {
	
	private ConversationRulesConfigParser parser=null;
	private ConversationTracker tracker=null;
	private boolean throwRandomQuestions=false;
	private boolean smartMatchingEnabled=true;
	private boolean patternMatchingEnabled=true;
	private ExpressionEvaluator evaluator=null;
	private ContextResolverFactory resolverFactory=null;
	private SmartMatcher smartMatcher=null;
	private LearningModule learningModule=null;
	private StandardSolrClient solrClient=null;
	
	private String noAnswerResponse="Sorry !! didn't get you !!";
	
	public void configureRules(String basePath, String... configFiles) {
		this.parser.parseConfigurations(basePath, configFiles);
	}
	
	public void setRandomQuestionMode(boolean throwRandomQuestions) {
		this.throwRandomQuestions=throwRandomQuestions;
	}
	
	public void setNoAnswerResponse(String noAnswerResponse) {
		this.noAnswerResponse = noAnswerResponse;
	}
	
	public boolean isSmartMatchingEnabled() {
		return smartMatchingEnabled;
	}

	public void setSmartSearchEnabled(boolean smartMatchingEnabled) {
		this.smartMatchingEnabled = smartMatchingEnabled;
		if(!smartMatchingEnabled) {
			this.patternMatchingEnabled=true;
		}
	}

	public boolean isPatternMatchingEnabled() {
		return patternMatchingEnabled;
	}

	public void setPatternMatchingEnabled(boolean patternMatchingEnabled) {
		this.patternMatchingEnabled = patternMatchingEnabled;
		if(!patternMatchingEnabled){
			this.smartMatchingEnabled=true;
		}
	}

	public void initialize() {
		this.parser=new ConversationRulesConfigParser();
		this.tracker=new ConversationTracker();
		ChatbotRegistryBootstrap.getInstance().loadAllComponents();
		this.evaluator=new ExpressionEvaluator();
		this.resolverFactory=ContextResolverFactory.getInstance();
		this.learningModule=LearningModule.getInstance();
		
		IContextResolver resolver=new MapContextResolver();
		Map<String, String> context=new HashMap<String, String>();
		resolver.setContext(context, context);
		this.resolverFactory.registerResolver(ResolverType.MAP, resolver);
		
		resolver=new FactContextResolver();
		resolver.setContext(this.learningModule);
		this.resolverFactory.registerResolver(ResolverType.FACT, resolver);
		
		resolver=new SolrContextResolver();
		this.resolverFactory.registerResolver(ResolverType.SOLR, resolver);
		
		
		String patternMatchAlgorithm=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.PATTERN_MATCH_ALGORITHM_ENABLED);
		
		if(StringUtils.isNotBlank(patternMatchAlgorithm)) {
			this.setPatternMatchingEnabled(Boolean.valueOf(patternMatchAlgorithm));
		}
		
		String smartMatchAlgorithm=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SMART_MATCH_ALGORITHM_ENABLED);
		
		if(StringUtils.isNotBlank(smartMatchAlgorithm)) {
			this.setSmartSearchEnabled(Boolean.valueOf(smartMatchAlgorithm));
		}
		
		
		if(this.smartMatchingEnabled) {
			// initializes Chatbot engine with trained questions to enable smart match

			String solrSmartMatchResultsetSize=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SMART_MATCH_USE_SOLR_RESULTSET_SIZE);

			int solrSmartMatchResultsetMaxSize=50;
			
			if(StringUtils.isNotBlank(solrSmartMatchResultsetSize)) {
				
				try
				{
					solrSmartMatchResultsetMaxSize=Integer.parseInt(solrSmartMatchResultsetSize);
					
				} catch(NumberFormatException nfe) {
				}
			}
			
			this.smartMatcher=SmartMatcher.getInstance();
			this.smartMatcher.setSolrMatchResultSize(solrSmartMatchResultsetMaxSize);
			
			boolean useSolrForSmartMatchEnabled=true;
			
			String useSolrForSmartMatch=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SMART_MATCH_USE_SOLR);
			
			if(StringUtils.isNotBlank(useSolrForSmartMatch)) {
				useSolrForSmartMatchEnabled=Boolean.valueOf(useSolrForSmartMatch);
			}
			
			this.smartMatcher.setUseSolrForSmartMatch(useSolrForSmartMatchEnabled);
			QuestionTopicMapper.getInstance().initialize(useSolrForSmartMatchEnabled);
			UserQuestionPipeline userQuestionPipeline=new UserQuestionPipeline();
			this.smartMatcher.setUserQuestionPipeline(userQuestionPipeline);
		}
		
		this.solrClient=StandardSolrClient.getInstance();
	}
	
	public String processRequest(ChatbotConversation request) {
		
		request.clearResponse();
		
		request.setQuestion(preProcessQuestion(request.getQuestion()));

		String guid=request.getConversationGuid();
		
		if(StringUtils.isBlank(guid)) {
			// generate random guid
			guid=ConversationUtility.generateRandomId();
			request.setConversationGuid(guid);
			
			this.tracker.setGlobalContext(guid, new HashMap<String, String>());
			
			if(this.throwRandomQuestions) {
				// maintain random questions tracker
				QuestionTracker questionTracker=new QuestionTracker();
				questionTracker.addAllQuestions(this.parser.getDefaultQuestionTracker().getQuestions());
				this.tracker.setQuestionTracker(guid, questionTracker);
			}
			
			scanThroughRules(request, false);
		}
		else {
			resumeConversation(request);
		}
		
		if(!request.isThereAnyResponse()) {
			// chatbot didn't find answer to user question...keep track of question, topic, intent details for future training
			ConversationNode conversationNode=this.tracker.getConversationNode(guid);
			
			String topicId=null;
			String intentId=null;
			
			if(conversationNode!=null) {
				topicId=conversationNode.getTopicId();
				
				TopicRuleDef ruleDef=conversationNode.getRule();
				
				if(ruleDef!=null) {
					intentId=ruleDef.getId();
				}
			}
			
			this.solrClient.indexUserQuestion(request.getQuestion(), topicId, intentId);
		}
		
		if(this.throwRandomQuestions && !request.isThereAnyResponse()) {
			QuestionTracker questionTracker=this.tracker.getQuestionTracker(guid);
			String randomQuestionTopicId=questionTracker.nextRandomQuestion();
			
			if(StringUtils.isNotBlank(randomQuestionTopicId)) {
				invokeTopic(randomQuestionTopicId, request, null, false, false, null);
				questionTracker.markQuestionAsked(randomQuestionTopicId);
			}
		}
		else if(!request.isThereAnyResponse()) {
			request.setResponse(noAnswerResponse, null);
		}
		
		return guid;
	}
	
	private String preProcessQuestion(String question) {
		return question.replaceAll("\\?|,|!", "");
	}
	
	private void resumeConversation(ChatbotConversation request){
		// lookup for ConversationNode
		// use the same to execute rules accordingly

		String guid=request.getConversationGuid();
		
		ConversationNode conversationNode=this.tracker.getConversationNode(guid);
		
		if(conversationNode==null) {
			scanThroughRules(request, false);
		} 
		else {
			processConversationRecursively(request, conversationNode, null);
		}
	}
	
	private void processConversationRecursively(ChatbotConversation request, ConversationNode conversationNode, ConversationNode parent) {

		TopicRuleDef rule=conversationNode.getRule();
		
		if(rule==null) {
			String topicId=conversationNode.getTopicId();
			
			TopicDef topicDef=this.parser.getTopicDef(topicId);
			
			processRulesRecursively(topicDef.getChildren().values(), request, conversationNode, parent);
			
			return;
		}
		
		// scan through rest of rules to see if any match and process
		if(!rule.getChildren().isEmpty())
		{
			processRulesRecursively(rule.getChildren().values(), request, conversationNode, parent);
		}
		else {
			Object ruleParent=rule.getParent();
			if(ruleParent!=null) {
				Collection<TopicRuleDef> rules=(ruleParent instanceof TopicRuleDef)?((TopicRuleDef)ruleParent).getChildren().values(): ((TopicDef)ruleParent).getChildren().values();
				Collection<TopicRuleDef> followingRuleSet=new ArrayList<TopicRuleDef>();
				
				int currentRule=Integer.parseInt(rule.getId());
				
				Iterator<TopicRuleDef> iterator=rules.iterator();
				
				for(int index=0;index<rules.size();index++) {
					TopicRuleDef currentRuleObj=iterator.next();
					
					if(index>currentRule){
						followingRuleSet.add(currentRuleObj);
					}
				}
				
				processRulesRecursively(followingRuleSet, request, conversationNode, parent);
			}
		}
		
		scanThroughRules(request, false);
	}
	
	private void scanThroughRules(ChatbotConversation request, boolean isQuestionMode) {
		
		// scan through conversation rules and execute accordingly
		Map<String, TopicDef> topicDefMap=this.parser.getTopicDefMap();
		Collection<TopicDef> topics=topicDefMap.values();
		
		// see if pattern matching is disabled ?
		if(!this.patternMatchingEnabled) {
			return;
		}
		
		for(TopicDef topic: topics) {
			
			if(topic.isJumpToTopicType()) {
				continue;
			}
			
			if(isQuestionMode && StringUtils.isNotBlank(topic.getQuestion())) {

				// question case, post the question to the user
				ConversationNode conversationNode=new ConversationNode();
				conversationNode.setTopicId(topic.getId());
				conversationNode.setVariableMap(topic.getVariableMap());
				
				request.setResponse(topic.getQuestion(), conversationNode);
				conversationNode.setRule(null);
				this.tracker.setConversationNode(request.getConversationGuid(), conversationNode);
				
				// mark question as asked to avoid repetition
				QuestionTracker questionTracker=this.tracker.getQuestionTracker(request.getConversationGuid());
				questionTracker.markQuestionAsked(topic.getId());
				
				return;
			}
			
			invokeTopic(topic.getId(), request, null, true, false, null);
			
			if(StringUtils.isNotBlank(request.getResponse())){
				return;
			}
		}
		
		if(this.smartMatchingEnabled) {
			// see if smart matcher can find out the nearest matched topic
			Map<String, String> variableMap=new HashMap<String, String>();
			String topicId=this.smartMatcher.findNearestMatch(request, variableMap);
			
			if(request.isThereAnyResponse()) {
				return;
			}
			
			if(StringUtils.isBlank(topicId)) {
				return;
			}
			
			invokeTopic(topicId, request, null, true, true, variableMap);
		}
	}
	
	private void invokeTopic(String topicId, ChatbotConversation request, ConversationNode parent, boolean onlyProcessPatternTopics, boolean isSmartMatchingMode, Map<String, String> variableMap) {
		
		TopicDef topic=this.parser.getTopicDef(topicId);

		ConversationNode conversationNode=new ConversationNode();
		conversationNode.setTopicId(topic.getId());
		conversationNode.setParentTopic(parent);
		conversationNode.setVariableMap(topic.getVariableMap());
		
		// to be able to leverage dynamic data fetched through smart match
		if(isSmartMatchingMode && variableMap!=null && !variableMap.isEmpty()) {
			conversationNode.setVariableMap(variableMap);
			this.tracker.setGlobalContext(request.getConversationGuid(), variableMap);
		}
		
		if(!isSmartMatchingMode && !topic.isJumpToTopicType()) {
		
			Pattern regex=topic.getRegex();
			Pattern pattern=topic.getPattern();
			
			regex=(regex!=null)?regex:pattern;
			
			if(regex==null && StringUtils.isBlank(topic.getQuestion()))
			{
				return;
			}
			
			// load all variables
			if(regex!=null) {
				boolean isMatched=loadAllVariables(request, conversationNode, regex, topic.getKeywordPatternMap());
				
				if(!isMatched)
					return;
			}
			else if(!onlyProcessPatternTopics){
				// question case, post the question to the user
				request.setResponse(topic.getQuestion(), conversationNode);
				conversationNode.setRule(null);
				this.tracker.setConversationNode(request.getConversationGuid(), conversationNode);
				
				if(this.throwRandomQuestions) {
					// mark question as asked to avoid repetition
					QuestionTracker questionTracker=this.tracker.getQuestionTracker(request.getConversationGuid());
					questionTracker.markQuestionAsked(topic.getId());
				}
				
				return;
			}
			else {
				return;
			}
		}	
		
		// process rules recursively
		Map<String, TopicRuleDef> rulesMap=topic.getChildren();
		
		this.resolverFactory.getResolver(ResolverType.MAP).setContext(topic.getVariableMap(), this.tracker.getGlobalContext(request.getConversationGuid()));
		Collection<TopicRuleDef> rules=rulesMap.values();
		processRulesRecursively(rules, request, conversationNode, parent);
	}
	
	private void processRulesRecursively(Collection<TopicRuleDef> rules, ChatbotConversation request, ConversationNode conversationNode, ConversationNode parent) {
		
		for(TopicRuleDef rule: rules) {
			
			switch(rule.getNodeType()) {
			case INTENT:  Pattern ruleRegex=rule.getRegex();
						Pattern rulePattern=rule.getPattern();
						
						ruleRegex=(ruleRegex!=null)?ruleRegex:rulePattern;
			
						String invokeId=rule.getJumpToTopicId();
						
						boolean breakConversation=rule.isBreakConversation() && rule.getChildren().isEmpty();			
						
						if(ruleRegex==null && StringUtils.isBlank(invokeId)) {
							updateResponseAndTracker(rule, rule.getDialog(), breakConversation, request, conversationNode, parent);
							return;
						}
						
						boolean isRuleMatched=false;
						
						if(ruleRegex!=null) {
							isRuleMatched=loadAllVariables(request, conversationNode, ruleRegex, rule.getKeywordPatternMap());
							
							if(!isRuleMatched) {
								continue;
							}
							
							updateResponseAndTracker(rule, rule.getDialog(), breakConversation, request, conversationNode, parent);
						}
						
						if(StringUtils.isNotBlank(invokeId)) {
							invokeTopic(invokeId, request, conversationNode, false, false, null);
							
							if(request.isThereAnyResponse()){
								return;
							}
						}
						
						if(isRuleMatched) {
							return;
						}
						
						break;
						
			case ELSEIF:
			case IF:	String whenExpression=rule.getExpression();
			
						ruleRegex=rule.getRegex();
						rulePattern=rule.getPattern();
			
						ruleRegex=(ruleRegex!=null)?ruleRegex:rulePattern;

						if(ruleRegex==null && StringUtils.isBlank(whenExpression)) {
							throw new ChatbotFrameworkException("Illegal configuration...either pattern or regex or expr are required for if/elseif node...");
						}
			
						isRuleMatched=false;
			
						if(ruleRegex!=null) {
							isRuleMatched=loadAllVariables(request, conversationNode, ruleRegex, rule.getKeywordPatternMap());
							
							if(isRuleMatched) {
								processRulesRecursively(rule.getChildren().values(), request, conversationNode, parent);
								return;
							}
							else
							{
								processRulesRecursively(rule.getElseChildren(), request, conversationNode, parent);
							}
						}
						
						if(StringUtils.isNotBlank(whenExpression)) {
							boolean shouldExecute = evaluator.evaluateBooleanExpression(whenExpression, resolverFactory);
							
							if(shouldExecute)
							{
								processRulesRecursively(rule.getChildren().values(), request, conversationNode, parent);
							}
							else
							{
								processRulesRecursively(rule.getElseChildren(), request, conversationNode, parent);
							}
						}
						break;
						
			case ELSE:	processRulesRecursively(rule.getChildren().values(), request, conversationNode, parent);
						break;
			}
		}
	}
	
	private void updateResponseAndTracker(TopicRuleDef rule, String content, boolean breakConversation, ChatbotConversation request, ConversationNode conversationNode, ConversationNode parent) {
		
		if(!rule.getFacts().isEmpty()) {
			this.learningModule.learnFacts(rule.getFacts(), conversationNode.getVariableMap());
		}
		
		request.setResponse(content, conversationNode);
		conversationNode.setRule(rule);
		conversationNode.setParentTopic(parent);
		
		this.tracker.setConversationNode(request.getConversationGuid(), breakConversation?null:conversationNode);
	}
	
	private boolean loadAllVariables(ChatbotConversation request, ConversationNode conversationNode, Pattern regex, Map<String, String[]> keywordsMap) {
		
		if(regex!=null) {
			Matcher matcher=regex.matcher(request.getQuestion());
			
			if(matcher.matches()) {
				
				Set<String> namedGroups=ConversationUtility.getNamedGroupsForRegexp(regex.toString());
				
				// match against keywords configured in pattern
				if(!keywordsMap.isEmpty()) {
					
					boolean isMatched=true;
					
					Set<Entry<String, String[]>> entrySet=keywordsMap.entrySet();
					
					for(Entry<String, String[]> entry: entrySet) {
						String matcherGroupName=entry.getKey();
						String questionPart=matcher.group(matcherGroupName);
						
						namedGroups.remove(matcherGroupName);
						
						isMatched &= isKeywordsMatched(questionPart, entry.getValue());
					}
					
					if(!isMatched) {
						return false;
					}
				}
				
				for(String variable: namedGroups) {
					String value=matcher.group(variable);
					
					if(StringUtils.isNotBlank(value))
					{
						conversationNode.addVariable(variable, value);
						this.tracker.updateGlobalContext(request.getConversationGuid(), variable, value);
					}
				}
			
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isKeywordsMatched(String question, String[] keywords) {
		
		boolean isMatched=true;
		
		for(String keyword: keywords) {
			String[] keywordParts=keyword.split("\\|");
			
			boolean flag=false;
			for(String part: keywordParts) {
				flag|=question.contains(part);
			}
			
			isMatched&=flag;
			
			if(!isMatched)
				return false;
		}
		
		return isMatched;
	}
	
	public void releaseResources() {
		// release all the resources associated with current engine instance
		this.learningModule.releaseResources();
		
		this.parser=null;
		this.tracker=null;
		this.evaluator=null;
		this.resolverFactory=null;
	}
}
