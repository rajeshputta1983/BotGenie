package com.genie.chatbot.context.resolvers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Rajesh Putta
 */
public class MapContextResolver implements IContextResolver {

	private Map<String, String> context=null;
	private Map<String, String> globalContext=null;
	
	@SuppressWarnings("unchecked")
	public void setContext(Object context) {
		this.context = (Map<String, String>)context;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setContext(Object context, Map<String, String> globalContext) {
		this.context = (Map<String, String>)context;
		this.globalContext = globalContext;
	}	
	
	public void setContext(String variable, Object data) {
	}

	public void removeContext(String variable) {
		this.context.remove(variable);
	}	
	
	@Override
	public String resolveField(String field, boolean isIterableType) {
		
		String value=this.context.get(field);
		
		if(StringUtils.isBlank(value)){
			value=this.globalContext.get(field);
		}
		
		return value;
	}

}
