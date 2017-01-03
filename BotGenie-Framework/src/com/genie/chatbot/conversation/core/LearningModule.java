package com.genie.chatbot.conversation.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.components.LearnFactDef;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;

/**
 * @author Rajesh Putta
 */
public class LearningModule {

	private static final LearningModule instance=new LearningModule();
	
	private final String mergeOrCreateSingleNodeTemplate="MERGE (s:${subject}) RETURN s";
	
	private final String updateSingleNodePropertiesTemplate="MERGE (s:${subject}) SET ${updateset} RETURN s";
	
	private final String mergeOneNodeTemplate="MERGE (${nodeAlias}:${nodeType})";
	
	private final String mergeNodeTemplate="MATCH (s:${subject}), (o:${object}) MERGE (s)-[r:${relation}]->(o) ${updateset} RETURN o";
	private final String createNodeTemplate="CREATE (s:${subject})-[r:${relation}]->(o:${object})";
	
	private Driver driver=null;
	
	private LearningModule() {
	
		String ipAddress=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.GRAPHDB_IP_ADDRESS);
		String userName=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.GRAPHDB_USER);
		String password=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.GRAPHDB_PASSWORD);
		
		if(StringUtils.isBlank(ipAddress) || StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
			throw new ChatbotFrameworkException("Chatbot Server cannot be initialized because expected configuration keys graphdb.ipaddress, graphdb.user, graphdb.password not available..");
		}
	
		this.driver = GraphDatabase.driver( "bolt://"+ipAddress, AuthTokens.basic( userName, password) );
	}
	
	public static LearningModule getInstance() {
		return instance;
	}
	
	public void learnFacts(List<LearnFactDef> facts, Map<String, String> variableMap) {
		
		for(LearnFactDef factDef: facts) {
			String fact=ConversationUtility.postProcessPattern(factDef.getFact(), variableMap);

			this.learnFactSmartly(fact);
		}
	}
	
	public void learnFactSmartly(String fact) {
		String[] factParts=fact.split("\\s*\\|\\|\\s*");
		
		if(factParts.length % 2==0) {
			throw new ChatbotFrameworkException("learning fact should be a single node or combinatino of triples with subject, relation, object...");
		}
		
		Map<String, String> variableMap=new HashMap<String, String>();

		variableMap.put("subject", factParts[0]);
		
		 // process straight away if fact is single node expression
		 if(factParts.length==1) {
				
			String[] parts=factParts[0].split("\\s*->\\s*");
			
			String finalUpdateSet=createUpdateSet(parts, "s.", variableMap);
			
			if(finalUpdateSet!=null) {
				variableMap.put("subject", parts[0]);
				variableMap.put("updateset", finalUpdateSet.toString());
				
				executeCipherQuery(updateSingleNodePropertiesTemplate, variableMap);
			}
		}		
		else {
			// process relationship chain subject-relation->object-relation->object
			
			Session session=driver.session();
			Transaction transaction=session.beginTransaction();
			
			try
			{
				for(int index=0; index<factParts.length-1; index+=2) {
				
					String finalUpdateSet=""; 
				
					// see if any properties need to be updated for leaf node
					if(index+2==factParts.length-1) {
						String[] parts=factParts[index+2].split("\\s*->\\s*");
						
						finalUpdateSet=createUpdateSet(parts, "o.", null);
						
						if(!StringUtils.isBlank(finalUpdateSet)) {
							factParts[index+2]=parts[0];
							finalUpdateSet="SET "+finalUpdateSet;
						} else {
							finalUpdateSet="";
						}
					}
				
					learnFactTriple(factParts[index], factParts[index+1], factParts[index+2], finalUpdateSet, transaction);
				}
				
				transaction.success();
				
			} catch(Exception e) {
				transaction.failure();
				e.printStackTrace();
			} finally{
			
				if(transaction!=null) {
					transaction.close();
				}
				
				if(session!=null) {
					session.close();
				}
			}
		}
	}
	
	private String createUpdateSet(String[] parts, String prefix, Map<String, String> variableMap) {
	
		if(parts.length==1 && variableMap!=null) {
			executeCipherQuery(mergeOrCreateSingleNodeTemplate, variableMap);
		}
		else if(parts.length==2){
			String updateSet=parts[1].substring(1, parts[1].length()-1).trim();
			String[] updateParts=updateSet.split("\\s*,\\s*");
			
			StringBuilder finalUpdateSet=new StringBuilder();
			
			for(String updatePart: updateParts) {
				finalUpdateSet.append(prefix).append(updatePart.replace(":", "=")).append(",");
			}
			
			if(finalUpdateSet.length()>0) {
				finalUpdateSet.deleteCharAt(finalUpdateSet.length()-1);
			}	
			
			return finalUpdateSet.toString();			
		}
		
		return null;
	}
	
	private void learnFactTriple(String subject, String relation, String object, String updateSet, Transaction transaction) {
		
		Map<String, String> variableMap=new HashMap<String, String>();
		variableMap.put("subject", subject);
		variableMap.put("relation", relation);
		variableMap.put("object", object);
		variableMap.put("updateset", updateSet);
		
		variableMap.put("nodeAlias", "s");
		variableMap.put("nodeType", subject);
		createEachNodeInRelation(mergeOneNodeTemplate, variableMap, transaction);
		
		variableMap.put("nodeAlias", "o");
		variableMap.put("nodeType", object);
		createEachNodeInRelation(mergeOneNodeTemplate, variableMap, transaction);

		// final relationship query goes here		
		boolean hasResults=executeCipherQueryWithUnderTransaction(mergeNodeTemplate, variableMap, transaction);
		
		if(!hasResults) {
			executeCipherQueryWithUnderTransaction(createNodeTemplate, variableMap, transaction);
		}
	}
	
	private void createEachNodeInRelation(String queryTemplate, Map<String, String> variableMap, Transaction transaction) {
		String cipherQuery=ConversationUtility.processDelimitedPattern(queryTemplate, variableMap);
		transaction.run(cipherQuery);
	}
	
	private boolean executeCipherQueryWithUnderTransaction(String queryTemplate, Map<String, String> variableMap, Transaction transaction) {
		
		String cipherQuery=ConversationUtility.processDelimitedPattern(queryTemplate, variableMap);
		
		StatementResult result = transaction.run(cipherQuery);
		boolean returnResults=result.hasNext();
		
		return returnResults;
	}
	
	private boolean executeCipherQuery(String queryTemplate, Map<String, String> variableMap) {
		
		String cipherQuery=ConversationUtility.processDelimitedPattern(queryTemplate, variableMap);
		
		Session session = driver.session();
		
		StatementResult result = session.run(cipherQuery);
		boolean returnResults=result.hasNext();
		
		session.close();
		
		return returnResults;
	}
	
	
	public String queryFactSmartly(String fact) {
		
		String[] factParts=fact.split("\\s*\\|\\|\\s*");
		Map<String, String> variableMap=new HashMap<String, String>();
		
		if(factParts.length==0)
		{
			throw new ChatbotFrameworkException("at least single fact node is required for querying against fact database...");
		}
	
		//  match (u:user {name:'Rajesh Putta'})-[h:has]-(f:friend {name:'Ravi'})-[h1:has]-(f1:friend) return f1.name
		
		StringBuilder cipherQuery=new StringBuilder("MATCH ");
		
		int counter=1;
		
		boolean expectProperty=false;
		
		String returnPart=null;
		
		for(String factPart: factParts) {
		
			if(counter==factParts.length) {
			
				String[] parts=factPart.split("\\s*->\\s*");
			
				if(parts.length==2) {
					factPart=parts[0];
					expectProperty=true;
					returnPart=parts[1].trim();
				}
			}
		
			cipherQuery.append(counter%2!=0?"(":"[").append("node").append(counter).append(":").append(factPart).append(counter%2!=0?")":"]").append("-");

			if(counter==factParts.length) {
				
				cipherQuery.deleteCharAt(cipherQuery.length()-1);
			
				if(expectProperty) {
					cipherQuery.append(" RETURN node").append(counter).append(".").append(returnPart);
					
					Map<String, Object> dataMap=executeFactQuery(cipherQuery.toString(), variableMap);
					
					return (dataMap!=null)?String.valueOf(dataMap.get("node"+counter+"."+returnPart)):null;
				}
				else {
					cipherQuery.append(" RETURN labels(node").append(counter).append(") as label");
					
					return executeTripleFactQuery(cipherQuery.toString(), variableMap);
				}
			}			
			
			counter++;
		}
		
		return null;		
	}

	public Map<String, Object> executeFactQuery(String queryTemplate, Map<String, String> variableMap) {
		
		String cipherQuery=ConversationUtility.processDelimitedPattern(queryTemplate, variableMap);
		
		Session session = null;
		
		try {
			session = driver.session();
	
			StatementResult result = session.run(cipherQuery);
			if(result.hasNext())
			{
			    Record record = result.next();
			    
			    return record.asMap();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(session!=null) {
				session.close();
			}
		}
		
		return null;
	}
	
	public String executeTripleFactQuery(String queryTemplate, Map<String, String> variableMap) {
		
		String cipherQuery=ConversationUtility.processDelimitedPattern(queryTemplate, variableMap);
		
		Session session = null;
		
		try {
			session = driver.session();
	
			StatementResult result = session.run(cipherQuery);
			if(result.hasNext())
			{
			    Record record = result.next();
			    List<Object> labelsList=record.get( "label" ).asList();
			    
			    if(!labelsList.isEmpty()) {
			    	return (String)labelsList.get(0);
			    }
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(session!=null) {
				session.close();
			}
		}
		
		return null;
	}	
	
	public void releaseResources() {
		if(driver!=null) {
			driver.close();
		}
	}
}
