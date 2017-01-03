package com.genie.chatbot.conversation.algorithm;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Rajesh Putta
 */
public class PermutationGenerator {
	
	private static final String BLANK_SPACE_REGEX="\\s+";	
	
	public static void main(String[] args) {
		String testStr="how to do stuff";
		
		Set<String> permutations=new PermutationGenerator().perm(testStr);
		
		System.out.println("Count ::"+permutations.size());
		System.out.println(permutations);
	}
	
	public void perm(String input, StringBuilder permutationSB, String delimeter) {
		
		String[] words=input.split("\\s+");
		
		perm(words, 0, permutationSB, delimeter);
		
		if(permutationSB.length()>0) {
			permutationSB.delete(permutationSB.length()-delimeter.length(), permutationSB.length());
		}
	}
	
	public Set<String> perm(String input) {
		
		String[] words=input.split("\\s+");
		
		Set<String> permutations=new HashSet<String>();
		
		perm(words, 0, permutations);
		
		return permutations;
	}
	
	public void perm(String[] words, int startIndex, StringBuilder permutationSB, String delimeter) {
		
		int totalLength=words.length;
		
		if(startIndex>=totalLength)
		{
			String perm=stringify(words, BLANK_SPACE_REGEX);
			permutationSB.append(perm.trim()).append(delimeter);
			
			return;
		}
		
		for(int i=startIndex;i<totalLength;i++) {
			swap(words, startIndex, i);
			perm(words, startIndex+1, permutationSB, delimeter);
			swap(words, startIndex, i);
		}
	}	
	
	public void perm(String[] words, int startIndex, Set<String> permutations ) {
		
		int totalLength=words.length;
		
		if(startIndex>=totalLength)
		{
			String perm=stringify(words, BLANK_SPACE_REGEX);
			permutations.add(perm);
			
			return;
		}
		
		for(int i=startIndex;i<totalLength;i++) {
			swap(words, startIndex, i);
			perm(words, startIndex+1, permutations);
			swap(words, startIndex, i);
		}
	}
	
	private void swap(String[] words, int i, int j) {
		String tmpWord=words[i];
		words[i]=words[j];
		words[j]=tmpWord;
	}
	
	private String stringify(String[] words, String delimeter) {
		
		StringBuilder tmp=new StringBuilder();
		
		for(String word: words) {
			tmp.append(word).append(delimeter);
		}

		if(tmp.length()>0) {
			tmp.delete(tmp.length()-BLANK_SPACE_REGEX.length(), tmp.length());
		}
		
		return tmp.toString();
	}
}
