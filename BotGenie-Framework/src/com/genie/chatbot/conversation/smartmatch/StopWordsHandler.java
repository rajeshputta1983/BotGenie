package com.genie.chatbot.conversation.smartmatch;

import java.util.Map;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.core.FrameworkConfiguration;

/**
 * @author Rajesh Putta
 */
public class StopWordsHandler {
	
	private static final StopWordsHandler instance=new StopWordsHandler();
	
	private Map<String, String> stopWordsMap=null;
	
	private FrameworkConfiguration frameworkConfiguration=FrameworkConfiguration.getInstance();
	
	private StopWordsHandler() {
		
		String stopWordsConfigPath=frameworkConfiguration.getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG)+frameworkConfiguration.getConfiguration(FrameworkConfiguration.STOPWORDS_CONFIG_PATH);
		
		this.stopWordsMap=ConversationUtility.loadFlatFileContent(stopWordsConfigPath);
	}
	
	public static StopWordsHandler getInstance() {
		return instance;
	}
	
	public String cleanUpText(String text) {
		
		StringBuilder cleanedUpText=new StringBuilder();
		
		String[] words=text.split("\\s+");
		
		for(String word: words) {
			
			if(!this.stopWordsMap.containsKey(word.toLowerCase()))
			{
				cleanedUpText.append(word).append(" ");
			}
		}
		
		if(cleanedUpText.length()>0){
			cleanedUpText.delete(cleanedUpText.length()-1, cleanedUpText.length());
		}
		
		return cleanedUpText.toString();
	}
	
	public Map<String, String> getStopWordsMap() {
		return stopWordsMap;
	}
}
