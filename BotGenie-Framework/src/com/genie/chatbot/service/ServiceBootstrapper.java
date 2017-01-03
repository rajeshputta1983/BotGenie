package com.genie.chatbot.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

/**
 * @author Rajesh Putta
 */
@SuppressWarnings("restriction")
public class ServiceBootstrapper {

	private static final String REST_URI = "http://localhost:";

	private static final String PORT = "port";
	private static final String CONFIG = "config";
	
	public static void main(String[] args) throws IllegalArgumentException,
			IOException {

		ServiceBootstrapper bootStrapper=new ServiceBootstrapper();
		Map<String, String> arguments=bootStrapper.readCommandLineArguments(args);
		
		if(arguments.isEmpty()) {
			bootStrapper.printUsage();
			return;
		}
		
		int port=10000;
		
		try {
			port=Integer.parseInt(arguments.get(PORT));
		} catch(NumberFormatException nfe) {
			System.out.println("invalid port number ..."+arguments.get(PORT)+" starting chatbot server with default port 10000");
		}
		
		String configPath=arguments.get(CONFIG);
		
		if(StringUtils.isBlank(configPath)) {
			bootStrapper.printUsage();
			return;
		}
		
		StringBuilder uri=new StringBuilder();
		uri.append(REST_URI).append(port).append("/chatbot");
		
		// initializes all chatbot modules
		ChatbotRequestProcessor.getInstance().initializeChatbot(configPath);
		
		HttpServer server = HttpServerFactory.create(uri.toString());
		server.start();
		
		System.out.println("=========================================================================");
		System.out.println();
		System.out.println("Chatbot Service is up for business...@ "+uri+"/request");
	}
	
	private Map<String, String> readCommandLineArguments(String[] args) {
		
		Map<String, String> arguments=new HashMap<String, String>();
		
		if(args.length%2!=0) {
			printUsage();
		}
		
		for(int index=0;index<args.length-1;index+=2) {
			String argumentType=args[index];
			String argumentValue=args[index+1];
			
			if(argumentType.startsWith("--")) {
				argumentType=argumentType.substring(2).toLowerCase();
			}
			else
				throw new ChatbotFrameworkException("invalid syntax of command line arguments...");
			
			if(argumentValue.startsWith("\"") && argumentValue.endsWith("\"")) {
				argumentValue=argumentValue.substring(1, argumentValue.length()-1);
			}
			
			arguments.put(argumentType, argumentValue);
		}
		
		return arguments;
	}

	private void printUsage() {
		System.out.println("java -jar chatbot-framework.jar --port 10000 --config \"c:\framework-config.properties\"");
		System.exit(1);
	}
	
}



