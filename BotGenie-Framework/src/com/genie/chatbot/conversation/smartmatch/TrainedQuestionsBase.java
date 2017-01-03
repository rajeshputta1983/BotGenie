package com.genie.chatbot.conversation.smartmatch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rajesh Putta
 */
public class TrainedQuestionsBase {
	
	private List<TrainedQuestion> trainedQuestionList = new ArrayList<TrainedQuestion>();
	
	private Integer uptoGramSize=4;

	public TrainedQuestionsBase(Integer uptoGramSize) {
		this.uptoGramSize=uptoGramSize;
	}
	
	public void addQuestion(String question, String baseQuestion) {
		
		TrainedQuestion trainedQuestion=new TrainedQuestion(question, baseQuestion, this.uptoGramSize);
		
		this.trainedQuestionList.add(trainedQuestion);
	}
	
	public List<TrainedQuestion> getTrainedQuestionList() {
		return trainedQuestionList;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder=new StringBuilder();
		
		for(TrainedQuestion question: trainedQuestionList) {
			builder.append("\r\nTrained Question :\r\n");
			builder.append(question);
		}
		
		return builder.toString();
	}
}
