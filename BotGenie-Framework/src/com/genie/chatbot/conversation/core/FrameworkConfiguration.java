package com.genie.chatbot.conversation.core;

import java.util.Map;

import com.genie.chatbot.conversation.components.ConversationUtility;

/**
 * @author Rajesh Putta
 */
public class FrameworkConfiguration {
	
	private static final FrameworkConfiguration instance=new FrameworkConfiguration();
	
	private Map<String, String> configuration=null; 
	
	public static final String CONVERSATION_CONFIG="conversation.config.base.path";
	public static final String CONVERSATION_FLOWS="conversation.flows";
	public static final String RANDOM_QUESTIONS_MODE="random.questions.mode";
	public static final String STOPWORDS_CONFIG_PATH="stopwords.config.path";
	public static final String GRAPHDB_IP_ADDRESS="graphdb.ipaddress";
	public static final String GRAPHDB_USER="graphdb.user";
	public static final String GRAPHDB_PASSWORD="graphdb.password";
	public static final String WORDNET_JWNL_CONFIG="wordnet.jwnl.config";
	public static final String WORDNET_DICTIONARY_PATH="wordnet.dictionary.path";
	public static final String SOLR_IP_ADDRESS="solr.ipaddress";
	public static final String SOLR_TRAINED_QUESTIONS_COLLECTION_NAME="solr.trained.questions.collection.name";
	public static final String SOLR_USER_QUESTIONS_COLLECTION_NAME="solr.user.questions.collection.name";
	public static final String SOLR_HELP_CONTENT_COLLECTION_NAME="solr.helpcontent.collection.name";
	public static final String QUESTIONS_TRAINING_SET_CONFIG="questions.trainingset.config";
	public static final String HELPCONTENT_TRAINING_SET_CONFIG="helpcontent.trainingset.config";
	public static final String WORLDDATA_TRAINING_SET_CONFIG="worlddata.trainingset.config";
	public static final String CONTEXT_RESOLVERS_SOLR_IP_ADDRESS="contextresolvers.solr.ipaddress";
	public static final String PATTERN_MATCH_ALGORITHM_ENABLED="pattern.match.algorithm.enabled";
	public static final String SMART_MATCH_ALGORITHM_ENABLED="smart.match.algorithm.enabled";
	public static final String SMART_MATCH_USE_SOLR="smart.match.use.solr";
	public static final String SMART_MATCH_USE_SOLR_RESULTSET_SIZE="smart.match.use.solr.resultset.size";

	
	private FrameworkConfiguration() {
	}
	
	public static FrameworkConfiguration getInstance() {
		return instance;
	}
	
	public void loadConfiguration(String configPath) {
		this.configuration=ConversationUtility.loadFlatFileContent(configPath);
	}
	
	public Map<String, String> getConfiguration() {
		return configuration;
	}
	
	public String getConfiguration(String key) {
		return this.configuration.get(key);
	}
}
