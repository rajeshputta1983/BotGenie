package com.genie.chatbot.context.resolvers;

import java.util.Map;

import com.genie.chatbot.conversation.core.LearningModule;


/**
 * @author Rajesh Putta
 */
public class FactContextResolver implements IContextResolver {

	private LearningModule learningModule=null;
	
	@Override
	public void setContext(Object context) {
		this.learningModule=(LearningModule)context;
	}
	
	public void setContext(Object context, Map<String, String> globalContext) {
		this.learningModule=(LearningModule)context;
	}
	
	public void setContext(String variable, Object data) {
	}

	public void removeContext(String variable) {
	}	
	
	@Override
	public String resolveField(String field, boolean isIterableType) {
		return this.learningModule.queryFactSmartly(field);
	}

}
