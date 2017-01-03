package com.genie.chatbot.conversation.transform;

import opennlp.tools.postag.POSTaggerME;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.components.ChatbotRegistryBootstrap;
import com.genie.chatbot.conversation.components.ConversationUtility;

/**
 * @author Rajesh Putta
 */
public class PatternTransformer {
	
	private static final PatternTransformer instance=new PatternTransformer();
	
	private POSTaggerME posTagger=null;
	
	private PatternTransformer() {
		this.posTagger=ChatbotRegistryBootstrap.getInstance().getOpenNlpRegistry().getPOSTagger();
	}
	
	public static PatternTransformer getInstance() {
		return instance;
	}
	
	public String transform(String pattern) {
		
		if(StringUtils.isBlank(pattern)) {
			return pattern;
		}
		
		pattern=pattern.toLowerCase();
		
		String[] words=pattern.split("\\s+");
	    
        String[] tags=posTagger.tag(words);		
		
		WordStemTransformer wordStemTransformer=new WordStemTransformer();
		
		String[] stemArray=wordStemTransformer.transform(words, tags, false);
		
		WordNetTransformer wordNetTransformer=new WordNetTransformer();
		
		String[] synonyms=wordNetTransformer.transform(words, tags);
		
		for(int index=0;index<words.length;index++) {
			
			if(stemArray[index].equals(synonyms[index]))
				words[index]=stemArray[index].replaceAll("_", "\\\\s+");
			else
				words[index]=stemArray[index].replaceAll("_", "\\\\s+")+"|"+synonyms[index].replaceAll("_", "\\\\s+");
		}

		String transformedPattern=ConversationUtility.stringify(words, ")\\s+(");
		
		if(transformedPattern.length()>0) {
			return "("+transformedPattern+")";
		}

		return pattern;
	}
	
	public String[] transformWords(String pattern) {
		
		if(StringUtils.isBlank(pattern)) {
			return new String[0];
		}
		
		pattern=pattern.toLowerCase();
		
		String[] words=pattern.split("\\s+");
	    
        String[] tags=posTagger.tag(words);		
		
		WordStemTransformer wordStemTransformer=new WordStemTransformer();
		
		String[] stemArray=wordStemTransformer.transform(words, tags, false);
		
		WordNetTransformer wordNetTransformer=new WordNetTransformer();
		
		String[] synonyms=wordNetTransformer.transform(words, tags);
		
		for(int index=0;index<words.length;index++) {
			
			if(stemArray[index].equals(synonyms[index]))
				words[index]=stemArray[index].replaceAll("_", "\\\\s+");
			else
				words[index]=stemArray[index].replaceAll("_", "\\\\s+")+"|"+synonyms[index].replaceAll("_", "\\\\s+");
		}
		
		return words;
	}
	
}
