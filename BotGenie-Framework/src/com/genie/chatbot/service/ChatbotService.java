package com.genie.chatbot.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Rajesh Putta
 */
@Path("/request")
public class ChatbotService {

	public ChatbotService() {
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response process(@HeaderParam("chatmsg") String chatMessage, @HeaderParam("conversationGuid") String conversationGuid) {

		Response response = Response.status(200).build();

		String path = null;

		try {
			
			if(chatMessage==null || chatMessage.trim().equals("")){
				String entityJson=createFailureResponse(path, "chat message is not available in request header...");
				return Response.status(400).entity(entityJson).build();
			}
			
			// delegate request to chatbot smart engine
			ResponseWrapper chatResponse=ChatbotRequestProcessor.getInstance().process(chatMessage, conversationGuid);
			
			ObjectMapper mapper = new ObjectMapper();
			String chatResp=mapper.writeValueAsString(chatResponse);

			response = Response.status(200).entity(chatResp)
					.build();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			String entityJson=createFailureResponse(path, e.getMessage());
			response = Response.status(400).entity(entityJson).build();
		} finally {
		}

		return response;
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

}
