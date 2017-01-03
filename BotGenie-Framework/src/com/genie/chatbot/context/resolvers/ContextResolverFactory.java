package com.genie.chatbot.context.resolvers;

import java.util.HashMap;
import java.util.Map;

import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;

/**
 * @author Rajesh Putta
 */
public class ContextResolverFactory {

	private static ContextResolverFactory instance=new ContextResolverFactory();
	
	private Map<String, IContextResolver> resolverMap=new HashMap<String, IContextResolver>();
	
	private ContextResolverFactory(){
	}
	
	public static ContextResolverFactory getInstance() {
		return instance;
	}
	
	public void registerResolver(String resolverType, IContextResolver resolver){
		
		if(this.resolverMap.containsKey(resolverType)) {
			throw new ChatbotFrameworkException("Context Resolver is already registered with the same name ..."+resolverType);
		}
		
		this.resolverMap.put(resolverType, resolver);
	}
	
	public IContextResolver getResolver(String type){

		return this.resolverMap.get(type);
	}

	public Object resolveField(String name, boolean isIterable) {
		
		if(name.startsWith("${") && name.endsWith("}")) {
			name=name.substring(2, name.length()-1).trim();
		}
		
		String resolverType=ResolverType.MAP;
		
		int firstIndex=name.indexOf("#");
		
		if(firstIndex>0){
			resolverType=name.substring(0, firstIndex+1);
			name=name.substring(firstIndex+1);
		}
		
		return this.getResolver(resolverType).resolveField(name, isIterable);
	}
}
