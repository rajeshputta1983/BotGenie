package com.genie.chatbot.conversation.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;

/**
 * @author Rajesh Putta
 */
public class NGramsGenerator {
	
	private static final String BLANK_SPACE_REGEX="\\s+";
	
	public static void main(String[] args) {
		String testStr="how to do my stuff";
		
		List<String> grams=new NGramsGenerator().nGrams(testStr.split("\\s+"), 3);
		
		System.out.println(grams);
	}
	
	public void nGrams(String testStr, int gramSize, StringBuilder nGramSB, String delimeter) {
		
		if(StringUtils.isBlank(testStr)) {
			throw new ChatbotFrameworkException("NGrams cannot be generated for null/empty string...");
		}
		
		String[] words=testStr.split("\\s+");
		
		if(gramSize<=0 || gramSize>words.length) {
			throw new ChatbotFrameworkException("Invalid NGram size..."+gramSize);
		}
		
		this.nGrams(words, gramSize, nGramSB, delimeter);
	}
	
	public List<String> nGrams(String[] words, int gramSize, String delimeter) {
		
		List<String> nGramList=new ArrayList<String>();
		
		StringBuilder temp=new StringBuilder();
		
		for(int index=0, subIndex=0;index<words.length;) {
			
			if((index+gramSize)>words.length)
				break;
			
			temp.append(words[index+subIndex]).append(delimeter);
			subIndex++;
			
			if(subIndex>=gramSize){
				index++;
				subIndex=0;
				
				if(temp.length()>0) {
					temp.delete(temp.length()-delimeter.length(), temp.length());
				}
				
				nGramList.add(temp.toString().trim());
				
				// recycle
				temp.delete(0, temp.length());
			}
		}
		
		return nGramList;
	}
	
	
	public List<String> nGrams(String[] words, int gramSize) {
		return nGrams(words, gramSize, " ");
	}
	
	public void nGrams(String[] words, int gramSize, StringBuilder nGramSB, String delimeter) {
		
		StringBuilder temp=new StringBuilder();
		
		for(int index=0, subIndex=0;index<words.length;) {
			
			if((index+gramSize)>words.length)
				break;
			
			temp.append(words[index+subIndex]).append(BLANK_SPACE_REGEX);
			subIndex++;
			
			if(subIndex>=gramSize){
				index++;
				subIndex=0;
				
				if(temp.length()>0) {
					temp.delete(temp.length()-BLANK_SPACE_REGEX.length(), temp.length());
				}
				
				nGramSB.append(temp.toString().trim()).append(delimeter);
				
				// recycle
				temp.delete(0, temp.length());
			}
		}
	}	
}
