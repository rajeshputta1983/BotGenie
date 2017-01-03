package com.genie.chatbot.context.resolvers;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.solr.StandardSolrClient;


/**
 * @author Rajesh Putta
 */
public class SolrContextResolver implements IContextResolver {

	private static final Pattern SOLR_QUERY_PATTERN=Pattern.compile("(?<fieldName>[a-zA-Z0-9_-]+)\\[(?<docId>[a-zA-Z0-9_-]+)\\]@(?<collectionName>[a-zA-Z0-9_-]+)");
	private static final StandardSolrClient solrClient=StandardSolrClient.getInstance();
	
	@Override
	public void setContext(Object context) {
	}
	
	public void setContext(Object context, Map<String, String> globalContext) {
	}
	
	public void setContext(String variable, Object data) {
	}

	public void removeContext(String variable) {
	}	
	
	@Override
	public String resolveField(String field, boolean isIterableType) {
		
		field=field.trim();
		
		Matcher matcher=SOLR_QUERY_PATTERN.matcher(field);
		
		if(matcher.matches()) {
			
			String fieldName=matcher.group("fieldName");
			String solrDocumentId=matcher.group("docId");
			String solrCollectionName=matcher.group("collectionName");
			
			return solrClient.getFieldBySolrDocumentId(fieldName, solrDocumentId, solrCollectionName);
		}
		else {
			throw new ChatbotFrameworkException("invalid syntax of solr context...expected format is...'fieldName[solrDocId]@solrCollectionName'");
		}
	}

}
