package com.genie.chatbot.conversation.preprocessors;

import java.util.Map;

/**
 * @author Rajesh Putta
 */
public interface IPatternPreprocessor {
	
	PatternInfo preProcess(String pattern, Map<String, String> variableMap);
}
