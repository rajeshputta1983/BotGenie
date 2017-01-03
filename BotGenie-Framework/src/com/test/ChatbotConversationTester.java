package com.test;
import java.util.Scanner;

import com.genie.chatbot.conversation.components.ChatbotConversation;
import com.genie.chatbot.conversation.core.ChatbotConversationEngine;

/**
 * @author Rajesh Putta
 */
public class ChatbotConversationTester {
	public static void main(String[] args) throws Exception {
		
		String basePath="/com/genie/chatbot/conversation/config/";
	    
		ChatbotConversationEngine engine=new ChatbotConversationEngine();
		engine.initialize();
		
		// if Random Question Mode is set to 'true', Chatbot automatically keep random questions to engage user
		// defaults to 'false'
		engine.setRandomQuestionMode(false);
		engine.configureRules(basePath, new String[] {"general-conversation.xml", "stop-payment.xml"});
		
		ChatbotConversation request=new ChatbotConversation();

		Scanner scanner=new Scanner(System.in);
		
		while(true) {
			System.out.print("USER : ");
			String question=scanner.nextLine();
			request(question, engine, request);
		}
	}
	
	private static void request(String question, ChatbotConversationEngine engine, ChatbotConversation request) {
		
		request.setQuestion(question);
		request.setResponse(null, null);
		
		engine.processRequest(request);
		
		String response=request.getResponse();
		
		System.out.println("CHATBOT : "+response);
	}
}
