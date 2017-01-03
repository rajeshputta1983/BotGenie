package com.genie.chatbot.conversation.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.nlp.OpenNlpMapper;
import com.genie.chatbot.conversation.nlp.POSEnum;
import com.genie.chatbot.wordnet.jwnl.JwnlHelper;

import edu.mit.jwi.item.POS;

/**
 * @author Rajesh Putta
 */
public class WordStemTransformer {
	
	private static Map<POSEnum, POS> posStemmerMap=new HashMap<POSEnum, POS>();
	
	private OpenNlpMapper mapper=new OpenNlpMapper();
	
	static {
		posStemmerMap.put(POSEnum.VERB, POS.VERB);
		posStemmerMap.put(POSEnum.ADVERB, POS.ADVERB);
		posStemmerMap.put(POSEnum.NOUN, POS.NOUN);
		posStemmerMap.put(POSEnum.ADJECTIVE, POS.ADJECTIVE);
	}
	
	public String[] transform(String[] words, String[] posTags, boolean justGetStems) {
		
		String[] stemArray=new String[words.length];
		
		int index=0;
		for(String word: words) {
			
			String posTag=posTags[index];
			POSEnum posCategory=mapper.lookup(posTag);
			
			String stemValue=word;
			
			if(posCategory!=null) {
				
				Set<String> result=JwnlHelper.getInstance().findStems(posStemmerMap.get(posCategory), word);

				if(justGetStems)
				{
					if(!result.isEmpty()) {
						stemValue=result.iterator().next();
					}
				}
				else
				{
					stemValue=ConversationUtility.stringify(result, "|");
				}
			}
			
			stemArray[index]=StringUtils.isBlank(stemValue)?word:stemValue;
					
			index++;		
		}
		
		return stemArray;
	}
}
