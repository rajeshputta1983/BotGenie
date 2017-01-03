package com.genie.chatbot.conversation.smartmatch;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.components.ChatbotConversation;
import com.genie.chatbot.conversation.components.ConversationUtility;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.conversation.pipeline.UserQuestionPipeline;
import com.genie.chatbot.conversation.smartmatch.QuestionTopicMapper.QuestionContent;
import com.genie.chatbot.conversation.transform.WordStemTransformer;
import com.genie.chatbot.solr.StandardSolrClient;


/**
 * @author Rajesh Putta
 */
public class SmartMatcher {
	
	private static SmartMatcher instance=new SmartMatcher();
	
	private boolean cleanUpStopWords = true;
	
	private UserQuestionPipeline userQuestionPipeline = null;
	
	private Integer uptoGramSize=1;
	
	private TrainedQuestionsBase questionBase=new TrainedQuestionsBase(uptoGramSize);
	
	private StandardSolrClient solrClient=StandardSolrClient.getInstance();
	
	private int solrMatchResultSize=50;
	
	private boolean useSolrForSmartMatch=true;
	
	private static final String REGEX_PATTERN="regex(";
	
	private SmartMatcher() {
	}
	
	public static SmartMatcher getInstance() {
		return instance;
	}
	
	public void setUptoGramSize(Integer uptoGramSize) {
		this.uptoGramSize = uptoGramSize;
	}
	
	public Integer getUptoGramSize() {
		return uptoGramSize;
	}
	
	public void setCleanUpStopWords(boolean cleanUpStopWords) {
		this.cleanUpStopWords = cleanUpStopWords;
	}
	
	public boolean isCleanUpStopWords() {
		return cleanUpStopWords;
	}
	
	public void setUserQuestionPipeline(UserQuestionPipeline userQuestionPipeline) {
		this.userQuestionPipeline = userQuestionPipeline;
	}
	
	public void setSolrMatchResultSize(int solrMatchResultSize) {
		this.solrMatchResultSize = solrMatchResultSize;
	}
	
	public int getSolrMatchResultSize() {
		return solrMatchResultSize;
	}
	
	public void setUseSolrForSmartMatch(boolean useSolrForSmartMatch) {
		this.useSolrForSmartMatch = useSolrForSmartMatch;
	}
	
	public boolean isUseSolrForSmartMatch() {
		return useSolrForSmartMatch;
	}
	
	public String findNearestMatch(ChatbotConversation request, Map<String, String> variableMap) {
		
		String userQuestion=request.getQuestion();
		
		if(StringUtils.isNotBlank(userQuestion)) {
			userQuestion=this.userQuestionPipeline.process(userQuestion);
		}
		
		String nearestMatch=null;
		
		if(this.useSolrForSmartMatch) {
			// solr based smart match pipeline goes here
			return this.matchLiteSolr(request, variableMap);
		}
		else {
			nearestMatch=this.matchLite(userQuestion, variableMap);
		}
		
		return returnFinalContent(nearestMatch, request, null);
	}
	
	private String returnFinalContent(String nearestMatch, ChatbotConversation request, Map<String, QuestionContent> questionTopicMap) {
		
		if(nearestMatch==null) {
			return null;
		}
		
		String topicId=QuestionTopicMapper.getInstance().getTopicIdForQuestion(nearestMatch, questionTopicMap);
		
		if(StringUtils.isBlank(topicId)) {
			// check if helpContentId is configured
			
			String helpDocumentId=QuestionTopicMapper.getInstance().getHelpContentIdForQuestion(nearestMatch, questionTopicMap);
			
			String helpContent=solrClient.getHelpDocumentById(helpDocumentId);
			
			request.setResponse(helpContent, null);
		}
		
		return topicId;
	}
	
	
	/*
	 *  uses solr for basic search and then apply n-gram smart search on the results for better performance and relevancy
	 */
	public String matchLiteSolr(ChatbotConversation request, Map<String, String> variableMap) {
		
		String userQuestion=request.getQuestion();
		
		String finalQuestion=preProcess(userQuestion);
		
		TrainedQuestionsBase questionBase=new TrainedQuestionsBase(1);
		
		Map<String, QuestionContent> questionTopicMap=solrClient.getTopRelevantMatchesFromSolr(finalQuestion, questionBase);
		
		if(questionTopicMap.isEmpty()) {
			return null;
		}
		
		TrainedQuestion trainedQuestion=new TrainedQuestion(finalQuestion, userQuestion, this.uptoGramSize);
		
		List<TrainedQuestion> trainedQuestionList=questionBase.getTrainedQuestionList();
		
		String nearestMatch=this.matchLite(trainedQuestion, trainedQuestionList, variableMap);
		
		return returnFinalContent(nearestMatch, request, questionTopicMap);
	}
	
	
	public String matchLite(String userQuestion, Map<String, String> variableMap) {
		
		String finalQuestion=preProcess(userQuestion);
		TrainedQuestion trainedQuestion=new TrainedQuestion(finalQuestion, userQuestion, 1);
		
		List<TrainedQuestion> trainedQuestionList=this.questionBase.getTrainedQuestionList();
		
		return this.matchLite(trainedQuestion, trainedQuestionList, variableMap);
	}		
	
	private String matchLite(TrainedQuestion trainedQuestion, List<TrainedQuestion> trainedQuestionList, Map<String, String> variableMap) {
		
		SmartMatchTracker matchTracker=new SmartMatchTracker();
		
		// compare unigrams, bigrams, trigrams, four-grams, etc in incremental fashion and drop non-matching questions
		// compares in smarter way up to n-grams without generating grams upfront
		
		List<String> uniGrams=trainedQuestion.getNGrams(1);
		
		for(TrainedQuestion tQuestion: trainedQuestionList) {
			
			List<String> trainedUniGrams=tQuestion.getNGrams(1);
			
			if(trainedUniGrams.isEmpty())
				continue;
			
			Map<MatchInfo, String> matchInfoMap=new LinkedHashMap<MatchInfo, String>(); 
			
			for(String uniGram: uniGrams) {
				
				int trainedUniGramsSize=trainedUniGrams.size();
			
				for(Iterator<Map.Entry<MatchInfo, String>> it = matchInfoMap.entrySet().iterator(); it.hasNext(); ) {
				    Map.Entry<MatchInfo, String> entry = it.next();
				    MatchInfo matchInfo=entry.getKey();

				    int tmpIndex=matchInfo.getLastIndex();
					
					if(tmpIndex+1<trainedUniGramsSize) {
						
						String trainedUniGram=trainedUniGrams.get(tmpIndex+1);
						
						boolean localRegexMatched=false;

						String regex=null;
						
						if(trainedUniGram.startsWith(REGEX_PATTERN)) {
							regex=trainedUniGram.substring(REGEX_PATTERN.length(), trainedUniGram.length()-1).trim();
							
							int equalIndex=regex.indexOf("=");
							
							if(equalIndex!=-1) {
								String dynaVariable=regex.substring(0, equalIndex).trim();
								String dynaPattern=regex.substring(equalIndex+1).trim();
								
								if(uniGram.matches(dynaPattern)) {
									localRegexMatched=true;
									tQuestion.addToVariableMap(dynaVariable, uniGram);
								}
								else {
									tQuestion.addToVariableMap(dynaVariable, null);
								}
							}
							else
								throw new ChatbotFrameworkException("Invalid Regex Pattern identified in Trained Question ..."+tQuestion.getBaseQuestion());
						}
						
						if(localRegexMatched || trainedUniGram.equalsIgnoreCase(uniGram)) {
							matchInfo.setCounter(matchInfo.getCounter()+1);
							matchInfo.setLastIndex(tmpIndex+1);
							matchTracker.incrementMatcher(tQuestion, matchInfo.getCounter());
						}
						else
							it.remove();
					}
				}
				
				for(int index=0; index<trainedUniGramsSize; index++) {
					String targetUniGram=trainedUniGrams.get(index);
					
					boolean localRegexMatched=false;
					
					String regex=null;
					
					if(targetUniGram.startsWith(REGEX_PATTERN)) {
						regex=targetUniGram.substring(REGEX_PATTERN.length(), targetUniGram.length()-1).trim();
						
						int equalIndex=regex.indexOf("=");
						
						if(equalIndex!=-1) {
							String dynaVariable=regex.substring(0, equalIndex).trim();
							String dynaPattern=regex.substring(equalIndex+1).trim();
							
							if(uniGram.matches(dynaPattern)) {
								localRegexMatched=true;
								tQuestion.addToVariableMap(dynaVariable, uniGram);
							}
							else {
								tQuestion.addToVariableMap(dynaVariable, null);
							}
						}
						else
							throw new ChatbotFrameworkException("Invalid Regex Pattern identified in Trained Question ..."+tQuestion.getBaseQuestion());
					}
					
					if(localRegexMatched || targetUniGram.equalsIgnoreCase(uniGram)) {
						matchTracker.incrementMatcher(tQuestion, 1);
						
						MatchInfo matchInfo=new MatchInfo();
						matchInfo.setCounter(1);
						matchInfo.setLastIndex(index);
						
						matchInfoMap.put(matchInfo, "");
					}
				}
			}
		}		
		
		matchTracker.displayMatches();
		
		System.out.println();
		
		System.out.println("trying to find out the best match considering dynamic patterns in the questions...");
		
		Map<TrainedQuestion, Integer> sortedMap=matchTracker.sortByMatches(false);
		
		Set<Entry<TrainedQuestion, Integer>> entrySet=sortedMap.entrySet();
		
		for(Entry<TrainedQuestion, Integer> nextTopper: entrySet) {
			
			if(nextTopper==null) {
				System.out.println("No Match...");
				return null;
			}
			
			TrainedQuestion tQuestion=nextTopper.getKey();
			Integer tQuestionMatchWeight=nextTopper.getValue();
			
			int tQuestionActualWeight=tQuestion.getStemQuestionLength();
			
			int averageWeight=(int)Math.ceil(tQuestionActualWeight*0.5);
			
			System.out.println("stem question weight :"+tQuestionActualWeight);
			System.out.println("match weight :"+tQuestionMatchWeight);
			
			if(tQuestionMatchWeight<averageWeight) {
				System.out.println("No Match...");
				return null;
			}
			
			// check if all dynamic variables have values identified from user question
			// otherwise, continue with next topper if it matches dynamic variables as well
			
			Set<Entry<String, String>> variableEntrySet=tQuestion.getVariableMap().entrySet();
			
			boolean allVariablesPopulated=true;
			
			for(Entry<String, String> variableEntry: variableEntrySet) {
				String value=variableEntry.getValue();
				
				allVariablesPopulated&=StringUtils.isNotBlank(value);
			}
			
			if(allVariablesPopulated) {
				variableMap.putAll(tQuestion.getVariableMap());
				System.out.println("Match Found..."+tQuestion.getBaseQuestion());
				return tQuestion.getBaseQuestion();
			}
		}
		
		return null;		
	}

	
	public String match(String userQuestion) {
		SmartMatchTracker matchTracker=new SmartMatchTracker();
		
		String finalQuestion=preProcess(userQuestion);
		TrainedQuestion trainedQuestion=new TrainedQuestion(finalQuestion, userQuestion, this.uptoGramSize);
		
		List<TrainedQuestion> trainedQuestionList=this.questionBase.getTrainedQuestionList();
		
		// compare unigrams, bigrams, trigrams, four-grams, etc in incremental fashion and drop non-matching questions
		for(int gramSize=1; gramSize<=this.uptoGramSize; gramSize++) {
			List<String> nGrams=trainedQuestion.getNGrams(gramSize);
			
			if(nGrams.isEmpty())
				break;
			
			for(TrainedQuestion tQuestion: trainedQuestionList) {
				
				List<String> trainedNGrams=tQuestion.getNGrams(gramSize);
				
				if(trainedNGrams.isEmpty())
					continue;
				
				for(String nGram: nGrams) {
					if(trainedNGrams.contains(nGram)) {
						matchTracker.incrementMatcher(tQuestion, gramSize);
					}
				}
			}
		}
		
		matchTracker.displayMatches();
		
		System.out.println();
		
		Entry<TrainedQuestion, Integer> topper=matchTracker.getTopper();
		
		if(topper==null) {
			System.out.println("No Match...");
			return null;
		}
		
		TrainedQuestion tQuestion=topper.getKey();
		Integer tQuestionMatchWeight=topper.getValue();
		
		int tQuestionActualWeight=tQuestion.getStemQuestionLength();
		
		int averageWeight=(int)Math.ceil(tQuestionActualWeight*0.5);
		
		if(tQuestionMatchWeight<averageWeight) {
			System.out.println("No Match...");
			return null;
		}
		
		System.out.println("Match Found..."+tQuestion.getBaseQuestion());
		
		return tQuestion.getBaseQuestion();
	}
	
	public String preProcess(String question) {
		
		question=question.trim();
		
		// clean up stop words
		String cleanedUpQuestion=StopWordsHandler.getInstance().cleanUpText(question);
		
		String[] words=cleanedUpQuestion.split("\\s+");
		
		OpenNlpTagger nlpTagger=new OpenNlpTagger();
		
		// perform POS tagging
		String[] posTags=nlpTagger.getTags(words);
		
		// stem words
		String[] stems=new WordStemTransformer().transform(words, posTags, true);
		
		String[] taggedWords= stems; //nlpTagger.tagPartOfSpeech(stems, posTags);
		
		String finalQuestion=ConversationUtility.stringify(taggedWords, " ");
		
		return finalQuestion;
	}
	
	public void addQuestion(String question) {

		String finalQuestion=preProcess(question);
		
		this.questionBase.addQuestion(finalQuestion, question);
	}
}
