package com.genie.chatbot.conversation.exceptions;

/**
 * @author Rajesh Putta
 */
public class ChatbotFrameworkException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ChatbotFrameworkException() {
		super();
	}
	
	public ChatbotFrameworkException(String msg) {
		super(msg);
	}
	
	public ChatbotFrameworkException(String msg, Throwable t) {
		super(msg, t);
	}

	public ChatbotFrameworkException(Throwable t) {
		super(t);
	}
	
}
