package com.genie.chatbot.solr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.core.FrameworkConfiguration;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.conversation.smartmatch.QuestionTopicMapper;
import com.genie.chatbot.conversation.smartmatch.SmartMatcher;
import com.genie.chatbot.conversation.smartmatch.TrainedQuestionsBase;
import com.genie.chatbot.conversation.smartmatch.QuestionTopicMapper.QuestionContent;

/**
 * @author Rajesh Putta
 */
public class StandardSolrClient {
	
	private static StandardSolrClient instance=new StandardSolrClient();
	
	private String ipAddress=null;
	private String trainedQuestionsCollectionName=null;
	private String userQuestionsCollectionName=null;
	private String helpContentCollectionName=null;
	private String contextResolverSolrIpAddress=null;
	private SmartMatcher smartMatcher=null;
	
	public static StandardSolrClient getInstance() {
		return instance;
	}
	
	private StandardSolrClient() {
	}
	
	public void initialize() {
		
		this.ipAddress=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SOLR_IP_ADDRESS);
		this.trainedQuestionsCollectionName=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SOLR_TRAINED_QUESTIONS_COLLECTION_NAME);
		this.userQuestionsCollectionName=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SOLR_USER_QUESTIONS_COLLECTION_NAME);
		this.helpContentCollectionName=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.SOLR_HELP_CONTENT_COLLECTION_NAME);
		this.contextResolverSolrIpAddress=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.CONTEXT_RESOLVERS_SOLR_IP_ADDRESS);
		
		if(StringUtils.isBlank(this.contextResolverSolrIpAddress)) {
			this.contextResolverSolrIpAddress=this.ipAddress;
		}
		
		this.smartMatcher=SmartMatcher.getInstance();
	}

	public Map<String, QuestionTopicMapper.QuestionContent> pullTrainedQuestions() {

		Map<String, QuestionTopicMapper.QuestionContent> questionTopicMap=new HashMap<String, QuestionTopicMapper.QuestionContent>();
		
		SolrClient solr=null;
		
		try {
			solr = new HttpSolrClient("http://"+this.ipAddress+"/solr/"+this.trainedQuestionsCollectionName+"/");
			SolrQuery query = new SolrQuery();
			query.setQuery("*:*");
			query.setFields("id", "question", "topicId", "helpContentId");
			query.setStart(0);
			query.setRows(1000);

			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();
			
			for(SolrDocument solrDocument: results) {
				String docId=(String)solrDocument.getFieldValue("id");
				String question=(String)solrDocument.getFirstValue("question");
				String topicId=(String)solrDocument.getFirstValue("topicId");
				String helpContentId=(String)solrDocument.getFirstValue("helpContentId");
				
				if(StringUtils.isBlank(helpContentId) && StringUtils.isBlank(topicId)) {
					throw new ChatbotFrameworkException("Either of topicId/helpContentId is mandatory in collection "+this.trainedQuestionsCollectionName+" for question.."+question);
				}
				
				if(!questionTopicMap.containsKey(question)) {
					questionTopicMap.put(question, new QuestionTopicMapper.QuestionContent(docId, topicId, helpContentId));
				
					this.smartMatcher.addQuestion(question);
				}
			}
		}
		catch(Exception e) {
			throw new ChatbotFrameworkException("Exception Occurred during SmartMatcher initialization..."+e.getMessage());
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return questionTopicMap;
	}
	
	public void indexQuestion(String question, String topicId, String helpContentId) {
		
		if(StringUtils.isBlank(topicId) && StringUtils.isBlank(helpContentId)) {
			throw new ChatbotFrameworkException("either topicId or helpContentId is mandatory to train question..."+question);
		}
		
		SolrClient solr=null;
		
		try {
			
			String docId=QuestionTopicMapper.getInstance().getDocIdForQuestion(question);
			
			solr = new HttpSolrClient("http://"+this.ipAddress+"/solr/"+this.trainedQuestionsCollectionName+"/");
	
			if(StringUtils.isBlank(docId)) {
				docId=UUID.randomUUID().toString();
			}
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", docId);
			doc.addField("question", question);
			
			if(StringUtils.isNotEmpty(topicId)){
				doc.addField("topicId", topicId);
			}
			
			if(StringUtils.isNotEmpty(helpContentId)){
				doc.addField("helpContentId", helpContentId);
			}
			
			solr.add(doc);
			solr.commit();
			
			QuestionTopicMapper.getInstance().addQuestionTopic(question, docId, topicId, helpContentId);
			
		}catch(Exception e){
			throw new ChatbotFrameworkException("Exception Occurred during training process..."+e.getMessage());
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void indexUserQuestion(String question, String topicId, String intentId) {
		
		SolrClient solr=null;
		
		try {
			
			solr = new HttpSolrClient("http://"+this.ipAddress+"/solr/"+this.userQuestionsCollectionName+"/");
	
			String docId=UUID.randomUUID().toString();
			
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", docId);
			doc.addField("question", question);
			doc.addField("topicId", topicId!=null?topicId.replaceAll("-_", " "):"");
			doc.addField("intentId", intentId!=null?intentId:"");
			
			solr.add(doc);
			solr.commit();
			
		}catch(Exception e){
			throw new ChatbotFrameworkException("Exception Occurred during ingestion of user un-answered question..."+e.getMessage());
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void indexHelpContent(String docId, String helpContent) {
		
		if(StringUtils.isBlank(docId) || StringUtils.isBlank(helpContent)) {
			throw new ChatbotFrameworkException("id/help_content both are mandatory for training...");
		}
		
		SolrClient solr=null;
		
		try {
			solr = new HttpSolrClient("http://"+this.ipAddress+"/solr/"+this.helpContentCollectionName+"/");

			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", docId);
			doc.addField("help_content", helpContent);
			
			solr.add(doc);
			solr.commit();
		}
		catch(Exception e) {
			throw new ChatbotFrameworkException("Exception Occurred while training on help content..."+e.getMessage());
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}		
	
	public String getHelpDocumentById(String docId) {
		
		SolrClient solr=null;
		
		try {
			solr = new HttpSolrClient("http://"+this.ipAddress+"/solr/"+this.helpContentCollectionName+"/");

			SolrQuery query = new SolrQuery();
			query.setQuery("id:"+docId);
			query.setFields("help_content", "id");
			
			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();

			if(results.size()>0)
			{
				SolrDocument document = results.get(0);
				return (String)document.getFieldValue("help_content");
			}
			
			return null;
		}
		catch(Exception e) {
			throw new ChatbotFrameworkException("Exception Occurred while trying to get help document by id..."+e.getMessage());
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public String getFieldBySolrDocumentId(String fieldName, String solrDocumentId, String solrCollectionName) {
		
		SolrClient solr=null;
		
		try {
			solr = new HttpSolrClient("http://"+this.contextResolverSolrIpAddress+"/solr/"+solrCollectionName+"/");

			SolrQuery query = new SolrQuery();
			query.setQuery("id:"+solrDocumentId);
			query.setFields(fieldName, "id");
			
			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();

			if(results.size()>0)
			{
				SolrDocument document = results.get(0);
				return (String)document.getFirstValue(fieldName);
			}
			
			return null;
		}
		catch(Exception e) {
			throw new ChatbotFrameworkException("Exception Occurred while trying to load field:'"+fieldName+"' of document id:'"+solrDocumentId+"' of solr collection name:'"+solrCollectionName+"'");
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public Map<String, QuestionContent> getTopRelevantMatchesFromSolr(String userQuery, TrainedQuestionsBase questionBase) {

		Map<String, QuestionContent> questionTopicMap=new HashMap<String, QuestionContent>();
		
		SolrClient solr=null;
		
		try {
			
			solr = new HttpSolrClient("http://"+this.ipAddress+"/solr/"+this.trainedQuestionsCollectionName+"/");
	
			SolrQuery query = new SolrQuery();
			
			String fuzzyQuery=ConversationUtility.getFuzzyQueryWithoutStopwords(userQuery);
			
			System.out.println("Fuzzy Query ::"+fuzzyQuery);
			
			query.setQuery("question: "+fuzzyQuery);
			query.setFields("id", "question", "topicId", "helpContentId");
			query.setStart(0);
			query.setRows(this.smartMatcher.getSolrMatchResultSize());

			QueryResponse response = solr.query(query);
			SolrDocumentList results = response.getResults();
			
			for(SolrDocument solrDocument: results) {
				String docId=(String)solrDocument.getFieldValue("id");
				String question=(String)solrDocument.getFirstValue("question");
				String topicId=(String)solrDocument.getFirstValue("topicId");
				String helpContentId=(String)solrDocument.getFirstValue("helpContentId");
				
				if(!questionTopicMap.containsKey(question)) {
					questionTopicMap.put(question, new QuestionTopicMapper.QuestionContent(docId, topicId, helpContentId));
					String finalQuestion=smartMatcher.preProcess(question);
					questionBase.addQuestion(finalQuestion, question);
				}
			}
		}
		catch(Exception e) {
			throw new ChatbotFrameworkException("Exception Occurred while searching user query on solr for matches..."+userQuery);
		} finally {
			if(solr!=null) {
				try {
					solr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
		
		return questionTopicMap;
	}
	
}
