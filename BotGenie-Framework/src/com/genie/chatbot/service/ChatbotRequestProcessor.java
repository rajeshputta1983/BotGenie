package com.genie.chatbot.service;
import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.components.ChatbotConversation;
import com.genie.chatbot.conversation.core.ChatbotConversationEngine;
import com.genie.chatbot.conversation.core.FrameworkConfiguration;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;

/**
 * @author Rajesh Putta
 */
public class ChatbotRequestProcessor {
	
	private static final ChatbotRequestProcessor instance=new ChatbotRequestProcessor();
	
	private static FrameworkConfiguration frameworkConfiguration=FrameworkConfiguration.getInstance();
	
	private ChatbotConversationEngine engine=null;
	
	private ChatbotRequestProcessor() {
	}
	
	public static ChatbotRequestProcessor getInstance() {
		return instance;
	}
	
	public void initializeChatbot(String configPath) {
		
		frameworkConfiguration.loadConfiguration(configPath);
		
		String conversationConfigPath=frameworkConfiguration.getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG);
		
		if(StringUtils.isBlank(conversationConfigPath)) {
			throw new ChatbotFrameworkException("'conversation.config.base.path' is required in "+configPath+" file to initialize chatbot engine...");
		}
		
		String conversationFlows=frameworkConfiguration.getConfiguration(FrameworkConfiguration.CONVERSATION_FLOWS);
		
		if(StringUtils.isBlank(conversationFlows)) {
			throw new ChatbotFrameworkException("'conversation.flows' is required in "+configPath+" file to initialize chatbot engine...");
		}
		
		conversationConfigPath=conversationConfigPath.trim();
		conversationFlows=conversationFlows.trim();
		
		engine=new ChatbotConversationEngine();
		engine.initialize();
		
		// if Random Question Mode is set to 'true', Chatbot automatically keep random questions to engage user
		// defaults to 'false'
		String randomQuestionsModeStr=frameworkConfiguration.getConfiguration(FrameworkConfiguration.RANDOM_QUESTIONS_MODE);
		
		boolean randomQuestionsMode=false;
		
		if(StringUtils.isNotBlank(randomQuestionsModeStr)) {
			randomQuestionsMode=Boolean.valueOf(randomQuestionsModeStr);
		}
		
		engine.setRandomQuestionMode(randomQuestionsMode);
		engine.configureRules(conversationConfigPath, conversationFlows.split("\\s*,\\s*"));
	}
	
	public ResponseWrapper process(String question, String guid) throws Exception {
		
		ChatbotConversation request=new ChatbotConversation();
		request.setConversationGuid(guid);
		request.setQuestion(question);
		request.setResponse(null, null);
		guid=engine.processRequest(request);
		
		ResponseWrapper wrapper=new ResponseWrapper();
		wrapper.setStatus("Success");
		wrapper.setStatusMessage("Chat Request Processed Successfully...");
		wrapper.setUserQuery(question);
		wrapper.setResult(request.getResponse());
		wrapper.setConversationGuid(guid);
		
		return wrapper;
	}
}
