package com.genie.chatbot.conversation.smartmatch;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.algorithm.NGramsGenerator;

/**
 * @author Rajesh Putta
 */
public class TrainedQuestion {

	private String stemQuestion=null;
	
	private String baseQuestion=null;
	
	private int stemQuestionLength=0;
	
	private Map<String, String> variableMap=new HashMap<String, String>(1<<2);

	private NGramsGenerator nGramGenerator=new NGramsGenerator();
	
	private Map<Integer, List<String>> nGramsMap = new LinkedHashMap<Integer, List<String>>();
	
	public TrainedQuestion(String question, String baseQuestion, Integer uptoGramSize) {
		this.stemQuestion=question;
		this.baseQuestion=baseQuestion;
		this.trainUptoNGrams(question, uptoGramSize);
	}
	
	public void trainUptoNGrams(String question, Integer uptoGramSize) {
		
		String[] words=question.split("\\s+");
		
		this.stemQuestionLength=words.length;
		
		// generates unigrams, bigrams, trigrams, four-grams, five-grams, .., N-grams based on configuration
		
		for(int gramSize=1; gramSize<=uptoGramSize; gramSize++) {
			List<String> gramsList=nGramGenerator.nGrams(words, gramSize);
			
			this.nGramsMap.put(gramSize, gramsList);
		}
	}
	
	public Map<Integer, List<String>> getnGramsMap() {
		return nGramsMap;
	}

	public void setnGramsMap(Map<Integer, List<String>> nGramsMap) {
		this.nGramsMap = nGramsMap;
	}
	
	public List<String> getNGrams(Integer gramSize) {
		return this.nGramsMap.get(gramSize);
	}
	
	public String getBaseQuestion() {
		return baseQuestion;
	}
	
	public String getStemQuestion() {
		return stemQuestion;
	}
	
	public int getStemQuestionLength() {
		return stemQuestionLength;
	}
	
	public void addToVariableMap(String name, String value) {
		
		String valueObj=this.variableMap.get(name);
		
		if(StringUtils.isNotBlank(valueObj)) {
			return;
		}
		
		this.variableMap.put(name, value);
	}
	
	public Map<String, String> getVariableMap() {
		return variableMap;
	}
	
	@Override
	public String toString() {
		StringBuilder builder=new StringBuilder();
		
		Set<Entry<Integer, List<String>>> entrySet=this.nGramsMap.entrySet();
		
		for(Entry<Integer, List<String>> entry: entrySet) {
			builder.append("\r\n\t Gram Size :"+entry.getKey()+"\tGrams :"+entry.getValue());
		}
		
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.baseQuestion.equals(((TrainedQuestion)obj).baseQuestion);
	}
	
	@Override
	public int hashCode() {
		return this.baseQuestion.hashCode();
	}
	
}
