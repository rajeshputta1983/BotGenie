package com.genie.chatbot.conversation.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.data.POS;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.nlp.OpenNlpMapper;
import com.genie.chatbot.conversation.nlp.POSEnum;
import com.genie.chatbot.wordnet.jwnl.JwnlHelper;

/**
 * @author Rajesh Putta
 */
public class WordNetTransformer {
	
	private static Map<POSEnum, POS> posStemmerMap=new HashMap<POSEnum, POS>();
	
	private OpenNlpMapper mapper=new OpenNlpMapper();
	
	static {
		posStemmerMap.put(POSEnum.VERB, POS.VERB);
		posStemmerMap.put(POSEnum.ADVERB, POS.ADVERB);
		posStemmerMap.put(POSEnum.NOUN, POS.NOUN);
		posStemmerMap.put(POSEnum.ADJECTIVE, POS.ADJECTIVE);
	}
	
	public String[] transform(String[] words, String[] posTags) {
		
		String[] synonymArray=new String[words.length];
		
		int index=0;
		for(String word: words) {
			
			String posTag=posTags[index];
			POSEnum posCategory=mapper.lookup(posTag);
			
			String synonyms=word;
			
			if(posCategory!=null) {
				Set<String> result=JwnlHelper.getInstance().findSynonyms(posStemmerMap.get(posCategory), word);
				synonyms=ConversationUtility.stringify(result, "|");
			}
			
			synonymArray[index]=synonyms;
					
			index++;		
		}
		
		return synonymArray;
	}
	
}
