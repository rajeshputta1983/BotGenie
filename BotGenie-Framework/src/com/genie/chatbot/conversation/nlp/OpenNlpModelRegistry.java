package com.genie.chatbot.conversation.nlp;

import java.util.HashMap;
import java.util.Map;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.core.FrameworkConfiguration;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

/*
 * @author Rajesh Putta
 */ 
public class OpenNlpModelRegistry {
	
	private Map<String, TokenNameFinderModel> nlpModels=new HashMap<String, TokenNameFinderModel>(1<<3);
	private Map<String, POSModel> posModels=new HashMap<String, POSModel>(1<<3);
	
	public OpenNlpModelRegistry()
	{
		String baseConfigPath=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG);
		ConversationUtility.loadOpenNlpModels(baseConfigPath, nlpModels, posModels);
	}
	
	public NameFinderME getNameFinder()
	{
	    String locale=ConversationUtility.LOCALE;
	    
	    TokenNameFinderModel nameFinderModel=nlpModels.get(locale);
	    
	    if(nameFinderModel!=null)
	    {
	        NameFinderME maxentNameFinder = new NameFinderME(nameFinderModel);
	        
	        return maxentNameFinder;
	    }
	    
	    return null;
	}
	
    public POSTaggerME getPOSTagger()
    {
        String locale=ConversationUtility.LOCALE;
        
        POSModel posModel=posModels.get(locale);
        
        if(posModel!=null)
        {
            POSTaggerME posTagger = new POSTaggerME(posModel);
            
            return posTagger;
        }
        
        return null;
    }	
	
}
