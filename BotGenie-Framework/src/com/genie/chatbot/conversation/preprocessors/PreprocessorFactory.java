package com.genie.chatbot.conversation.preprocessors;

import com.genie.chatbot.conversation.components.PatternType;

/**
 * @author Rajesh Putta
 */
public class PreprocessorFactory {
	private static final PreprocessorFactory factory=new PreprocessorFactory();
	
	private PreprocessorFactory(){
	}
	
	public static PreprocessorFactory getInstance() {
		return factory;
	}
	
	public IPatternPreprocessor getPreprocessor(PatternType patternType){
		
		IPatternPreprocessor preProcessor=null;
		
		switch(patternType) {
		case REGEX_PATTERN: preProcessor = new RegexPatternPreprocessor();
							break;
		case SIMPLE_PATTERN: preProcessor = new SimplePatternPreprocessor();
							break;
		}
		
		return preProcessor;
	}
}
