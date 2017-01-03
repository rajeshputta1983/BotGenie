package com.genie.chatbot.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.core.FrameworkConfiguration;
import com.genie.chatbot.conversation.core.LearningModule;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.solr.StandardSolrClient;

/**
 * @author Rajesh Putta
 */
@Path("/train")
public class ChatbotTrainerService {

	private static final String START_TOKEN="START:";
	
	public ChatbotTrainerService() {
	}

	@Path("/questions")
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response process() {

		String baseConfigPath=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG);
		String trainingSetConfig=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.QUESTIONS_TRAINING_SET_CONFIG);
		
		if(StringUtils.isBlank(trainingSetConfig)) {
			String entityJson=createFailureResponse(null, "'questions.trainingset.config' should be configured in framework-config.properties...");
			return Response.status(400).entity(entityJson).build();
		}
		
		Response response = Response.status(200).build();

		String path = null;
		
		InputStream stream=null;

		try {
			
			String configPath=baseConfigPath+trainingSetConfig;
			
			stream=ConversationUtility.loadStream(configPath, true);			

			if(stream==null) {
				String entityJson=createFailureResponse(null, "not able to load training set "+configPath+" configured in framework-config.properties");
				return Response.status(400).entity(entityJson).build();				
			}
			
			response=ingestToSolr(configPath, stream, 3);
			
			if(response==null) {
				
				ResponseWrapper trainerResponse=new ResponseWrapper();
				trainerResponse.setStatus("Success");
				trainerResponse.setStatusMessage("Chatbot is trained successfully...");
				
				ObjectMapper mapper = new ObjectMapper();
				String chatResp=mapper.writeValueAsString(trainerResponse);

				response = Response.status(200).entity(chatResp)
						.build();
			}
			else {
				return response;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			String entityJson=createFailureResponse(path, e.getMessage());
			response = Response.status(400).entity(entityJson).build();
			
		} finally {
			ConversationUtility.closeInputStream(stream);
		}

		return response;
	}
	
	@Path("/helpcontent")
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response processHelpContent() {

		String baseConfigPath=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG);
		String trainingSetConfig=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.HELPCONTENT_TRAINING_SET_CONFIG);
		
		if(StringUtils.isBlank(trainingSetConfig)) {
			String entityJson=createFailureResponse(null, "'helpcontent.trainingset.config' should be configured in framework-config.properties...");
			return Response.status(400).entity(entityJson).build();
		}
		
		Response response = Response.status(200).build();

		String path = null;
		
		InputStream stream=null;

		try {
			
			String configPath=baseConfigPath+trainingSetConfig;
			
			stream=ConversationUtility.loadStream(configPath, true);			

			if(stream==null) {
				String entityJson=createFailureResponse(null, "not able to load training set "+configPath+" configured in framework-config.properties");
				return Response.status(400).entity(entityJson).build();				
			}
			
			response=ingestToSolr(configPath, stream, 2);
			
			if(response==null) {
				
				ResponseWrapper trainerResponse=new ResponseWrapper();
				trainerResponse.setStatus("Success");
				trainerResponse.setStatusMessage("Chatbot is trained successfully...");
				
				ObjectMapper mapper = new ObjectMapper();
				String chatResp=mapper.writeValueAsString(trainerResponse);

				response = Response.status(200).entity(chatResp)
						.build();
			}
			else {
				return response;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			
			String entityJson=createFailureResponse(path, e.getMessage());
			response = Response.status(400).entity(entityJson).build();
			
		} finally {
			ConversationUtility.closeInputStream(stream);
		}

		return response;
	}
	
	
	@Path("/world/facts")
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response processWorldData() {

		String baseConfigPath=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.CONVERSATION_CONFIG);
		String trainingSetConfig=FrameworkConfiguration.getInstance().getConfiguration(FrameworkConfiguration.WORLDDATA_TRAINING_SET_CONFIG);
		
		if(StringUtils.isBlank(trainingSetConfig)) {
			String entityJson=createFailureResponse(null, "'worlddata.trainingset.config' should be configured in framework-config.properties...");
			return Response.status(400).entity(entityJson).build();
		}
		
		Response response = null;

		String path = null;
		
		InputStream stream=null;

		try {
			
			String configPath=baseConfigPath+trainingSetConfig;
			
			stream=ConversationUtility.loadStream(configPath, true);			

			if(stream==null) {
				String entityJson=createFailureResponse(null, "not able to load training set "+configPath+" configured in framework-config.properties");
				return Response.status(400).entity(entityJson).build();				
			}
			
			learnUniversalFacts(stream);
			
			ResponseWrapper trainerResponse=new ResponseWrapper();
			trainerResponse.setStatus("Success");
			trainerResponse.setStatusMessage("Chatbot is trained successfully...");
			
			ObjectMapper mapper = new ObjectMapper();
			String chatResp=mapper.writeValueAsString(trainerResponse);

			response = Response.status(200).entity(chatResp)
					.build();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			String entityJson=createFailureResponse(path, e.getMessage());
			response = Response.status(400).entity(entityJson).build();
			
		} finally {
			ConversationUtility.closeInputStream(stream);
		}

		return response;
	}	
	
	private void learnUniversalFacts(InputStream stream) {
		
		BufferedReader reader=null;
		
		try {
			reader=new BufferedReader(new InputStreamReader(stream, "UTF8"));
			
			String temp=null;
			
			String recentFactPattern=null;
			
			while((temp=reader.readLine())!=null) {
				
				temp=temp.trim();
				
				if(StringUtils.isBlank(temp) || temp.startsWith("#")) {
					continue;
				}

				if(temp.startsWith(START_TOKEN)) {
					recentFactPattern=temp.substring(START_TOKEN.length()).trim();
					continue;
				}
				
				if(StringUtils.isBlank(recentFactPattern)) {
					throw new ChatbotFrameworkException("Fact Pattern is expected before training data...");
				}
				
				String[] parts=temp.split("\\s+");
				
				String factWithData=replaceDynamicsOfFactPattern(recentFactPattern, parts);
				
				LearningModule.getInstance().learnFactSmartly(factWithData);
				
				System.out.println("chatbot learnt :: "+factWithData);
			}
			
		} catch (Exception e) {
			throw new ChatbotFrameworkException(e);
		} finally {
			ConversationUtility.closeReader(reader);
		}
	}

	private Response ingestToSolr(String configPath, InputStream stream, int entityCount) {
		
		StandardSolrClient solrClient=StandardSolrClient.getInstance();
		
		BufferedReader reader=null;
		
		try {
			reader=new BufferedReader(new InputStreamReader(stream, "UTF8"));
			
			String temp=null;
			
			while((temp=reader.readLine())!=null) {
				
				temp=temp.trim();
				
				if(StringUtils.isBlank(temp) || temp.startsWith("#")) {
					continue;
				}
				
				String[] parts=temp.split("\"\\s+\"");
				
				if(parts.length!=entityCount) {
					String entityJson=createFailureResponse(null, "invalid pattern found in training set "+configPath+" ...."+temp);
					return Response.status(400).entity(entityJson).build();							
				}
				
				parts[0]=parts[0].substring(1);
				parts[entityCount-1]=parts[entityCount-1].substring(0, parts[entityCount-1].length()-1);
				
				if(entityCount==3)
				{
					solrClient.indexQuestion(parts[0], parts[1], parts[2]);
				}
				else if(entityCount==2) 
				{
					solrClient.indexHelpContent(parts[0], parts[1]);
				}
			}
			
		} catch (Exception e) {
			throw new ChatbotFrameworkException(e);
		} finally {
			ConversationUtility.closeReader(reader);
		}
		
		return null;
	}
	
	private String createFailureResponse(String userQuery, String message) {
		
		ResponseWrapper response=new ResponseWrapper();
		response.setStatus("Failure");
		response.setStatusMessage(message);
		response.setUserQuery(userQuery);
		
		ObjectMapper mapper = new ObjectMapper();
		String entityJson = null;

		try {
			entityJson = mapper.writeValueAsString(response);
		} catch (Exception e2) {
			throw new RuntimeException(e2);
		}
		
		return entityJson;
	}
	
	private String replaceDynamicsOfFactPattern(String factPattern, String[] dynamics) {
		
		StringBuilder temp=new StringBuilder(factPattern);
		
		int offset=0;
		
		while(true){
			int startIndex=temp.indexOf("${", offset);
			
			if(startIndex==-1){
				break;
			}
			
			int endIndex=ConversationUtility.findMatch(temp.toString(), startIndex+2, '{', '}');
			
			if(endIndex==-1) {
				break;
			}
			
			String variable=temp.substring(startIndex+2, endIndex).trim();
			
			String result="";
			
			if(StringUtils.isNotBlank(variable)){
				int index=Integer.parseInt(variable);
				
				if(index>=dynamics.length) {
					throw new ChatbotFrameworkException("fact pattern '"+factPattern+"' dynamic variable indexes not matching with record length...");
				}
				
				result=dynamics[index].trim().replaceAll("_", " ");
			}
			
			temp.replace(startIndex, endIndex+1, result);
			
			offset=endIndex+1;
		}
		
		return temp.toString();
	}

}
