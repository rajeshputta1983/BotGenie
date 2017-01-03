package com.genie.chatbot.conversation.nlp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rajesh Putta
 */
public class OpenNlpMapper {
	
	private static Map<String, POSEnum> posMap=new HashMap<String, POSEnum>();
	
	static {
		posMap.put("NN", POSEnum.NOUN);
		posMap.put("NNS", POSEnum.NOUN);
		posMap.put("NNP", POSEnum.NOUN);
		posMap.put("NNPS", POSEnum.NOUN);
		
		posMap.put("VB", POSEnum.VERB);
		posMap.put("VBD", POSEnum.VERB);
		posMap.put("VBG", POSEnum.VERB);
		posMap.put("VBN", POSEnum.VERB);
		posMap.put("VBP", POSEnum.VERB);
		posMap.put("VBZ", POSEnum.VERB);
		
		posMap.put("JJ", POSEnum.ADJECTIVE);
		posMap.put("JJR", POSEnum.ADJECTIVE);
		posMap.put("JJS", POSEnum.ADJECTIVE);
		
		posMap.put("RB", POSEnum.ADVERB);
		posMap.put("RBR", POSEnum.ADVERB);
		posMap.put("RBS", POSEnum.ADVERB);
	}
	
	public POSEnum lookup(String openNlpTag) {
		return posMap.get(openNlpTag);
	}
	
	public POSEnum[] lookup(String[] openNlpTags) {
		
		POSEnum[] posEnumArray=new POSEnum[openNlpTags.length];
		
		int index=0;
		for(String openNlpTag: openNlpTags) {
			posEnumArray[index]=posMap.get(openNlpTag);
			index++;
		}
		
		return posEnumArray;
	}
	
	
	
}
