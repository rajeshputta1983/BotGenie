package com.genie.chatbot.conversation.smartmatch;

import opennlp.tools.postag.POSTaggerME;

import com.genie.chatbot.conversation.components.ChatbotRegistryBootstrap;
import com.genie.chatbot.conversation.nlp.OpenNlpModelRegistry;

/**
 * @author Rajesh Putta
 */
public class OpenNlpTagger {
	
	public String[] getTags(String[] words) {
		
		OpenNlpModelRegistry registry=ChatbotRegistryBootstrap.getInstance().getOpenNlpRegistry();
		POSTaggerME posTagger=registry.getPOSTagger();
		
		String[] tags=posTagger.tag(words);
		
		return tags;
	}
	
	public String[] tagPartOfSpeech(String[] words, String[] tags) {
		
		for(int index=0; index<words.length; index++) {
			words[index]=words[index]+"/"+tags[index];
		}
		
		return words;
	}
	
}
