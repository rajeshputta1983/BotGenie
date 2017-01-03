package com.genie.chatbot.conversation.smartmatch;

import java.util.Map;

import com.genie.chatbot.solr.StandardSolrClient;

/**
 * @author Rajesh Putta
 */
public class QuestionTopicMapper {
	
	private static QuestionTopicMapper instance=new QuestionTopicMapper();
	
	private SmartMatcher smartMatcher=SmartMatcher.getInstance();
	
	private Map<String, QuestionContent> questionTopicMap=null;
	
	private boolean useSolrForSmartMatchEnabled=true;
	
	private QuestionTopicMapper() {
	}
	
	public static QuestionTopicMapper getInstance() {
		return instance;
	}
	
	public void initialize(boolean useSolrForSmartMatchEnabled) {
		// pull all trained questions from Solr and cache
		this.useSolrForSmartMatchEnabled=useSolrForSmartMatchEnabled;
		
		if(this.useSolrForSmartMatchEnabled) {
			StandardSolrClient.getInstance().initialize();
		}
		else {
			StandardSolrClient.getInstance().initialize();
			this.questionTopicMap=StandardSolrClient.getInstance().pullTrainedQuestions();
		}
	}
	
	public String getTopicIdForQuestion(String question) {
		
		QuestionContent content=this.questionTopicMap.get(question);
		
		if(content!=null) {
			return content.getTopicId();
		}
		
		return null;
	}
	
	public String getTopicIdForQuestion(String question, Map<String, QuestionContent> questionTopicMapLocal) {
		
		if(questionTopicMapLocal==null) {
			return this.getTopicIdForQuestion(question);
		}
		
		QuestionContent content=questionTopicMapLocal.get(question);
		
		if(content!=null) {
			return content.getTopicId();
		}
		
		return null;
	}	
	
	public String getDocIdForQuestion(String question) {
		
		QuestionContent content=this.questionTopicMap.get(question);
		
		if(content!=null) {
			return content.getDocId();
		}
		
		return null;
	}
	
	public String getHelpContentIdForQuestion(String question) {
		
		QuestionContent content=this.questionTopicMap.get(question);
		
		if(content!=null) {
			return content.getHelpContentId();
		}
		
		return null;
	}
	
	public String getHelpContentIdForQuestion(String question, Map<String, QuestionContent> questionTopicMapLocal) {
		
		if(questionTopicMapLocal==null) {
			return this.getHelpContentIdForQuestion(question);
		}
		
		QuestionContent content=questionTopicMapLocal.get(question);
		
		if(content!=null) {
			return content.getHelpContentId();
		}
		
		return null;
	}
	
	public synchronized void addQuestionTopic(String question, String docId, String topicId, String helpContentId) {
		this.questionTopicMap.put(question, new QuestionContent(docId, topicId, helpContentId));
		this.smartMatcher.addQuestion(question);
	}
	
	public static class QuestionContent {
		
		private String docId=null;
		private String topicId=null;
		private String helpContentId=null;
		
		public QuestionContent(String docId, String topicId, String helpContentId) {
			this.docId=docId;
			this.topicId=topicId;
			this.helpContentId=helpContentId;
		}
		
		public String getTopicId() {
			return topicId;
		}
		
		public String getHelpContentId() {
			return helpContentId;
		}
		
		public String getDocId() {
			return docId;
		}
	}
}
