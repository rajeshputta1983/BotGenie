package com.genie.chatbot.context.resolvers;

import java.util.Map;

/**
 * @author Rajesh Putta
 */
public interface IContextResolver {
	
	void setContext(Object context);
	void setContext(Object context, Map<String, String> globalContext);
	String resolveField(String field, boolean isIterableType);
	void removeContext(String variable);
}
