package com.genie.chatbot.conversation.pipeline;

import com.genie.chatbot.conversation.components.ChatbotRegistryBootstrap;
import com.genie.chatbot.conversation.transform.WordStemTransformer;

import opennlp.tools.postag.POSTaggerME;

/**
 * @author Rajesh Putta
 */
public class UserQuestionPipeline {
	
	private POSTaggerME posTagger=null;
	
	public UserQuestionPipeline() {
		this.posTagger=ChatbotRegistryBootstrap.getInstance().getOpenNlpRegistry().getPOSTagger();
	}
	
	public String process(String userQuestion) {
		
		userQuestion=userQuestion.trim();
		
		String[] words=userQuestion.split("\\s+");
	    
        String[] tags=posTagger.tag(words);		
		
		WordStemTransformer wordStemTransformer=new WordStemTransformer();
		
		String[] stemArray=wordStemTransformer.transform(words, tags, true);
		
		StringBuilder transformedQuestion=new StringBuilder();
		
		for(String stem: stemArray) {
			transformedQuestion.append(stem).append(" ");
		}
		
		if(transformedQuestion.length()>0) {
			transformedQuestion.delete(transformedQuestion.length()-1, transformedQuestion.length());
		}
		
		return transformedQuestion.toString();
	}
}
