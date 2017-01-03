package com.genie.chatbot.conversation.components;

import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.conversation.nlp.OpenNlpModelRegistry;

/*
 * @author Rajesh Putta
 */ 
public class ChatbotRegistryBootstrap {
	
    private static final ChatbotRegistryBootstrap bootstrap=new ChatbotRegistryBootstrap();
    
	private OpenNlpModelRegistry openNlpRegistry=null;
    
    private ChatbotRegistryBootstrap(){}
    
    public static ChatbotRegistryBootstrap getInstance()
    {
        return bootstrap;
    }
    
    public void loadAllComponents() {

        try
        {
            this.openNlpRegistry=new OpenNlpModelRegistry();
        }
        catch(Exception e)
        {
        	throw new ChatbotFrameworkException(e);
        }
    }
    
    public OpenNlpModelRegistry getOpenNlpRegistry() {
		return this.openNlpRegistry;
	}
}
