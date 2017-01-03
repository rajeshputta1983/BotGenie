package com.genie.chatbot.conversation.preprocessors;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.genie.chatbot.conversation.algorithm.NGramsGenerator;
import com.genie.chatbot.conversation.algorithm.PermutationGenerator;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.conversation.transform.PatternTransformer;

/**
 * @author Rajesh Putta
 */
public class SimplePatternPreprocessor implements IPatternPreprocessor {
	
	private static final Pattern PATTERN=Pattern.compile("regex\\(|\\+|\\*|\\(|\\{|\\<\\<");
	
	private static final Pattern UNORDERED_GROUP_END_PATTERN=Pattern.compile(">>(?<nGramPattern>(_)([0-9]+)?)?");
	
	private static final Pattern VARIABLE_PATTERN=Pattern.compile("\\{\\s*(?<variable>[^\\s]+)\\s*=\\s*[^\\}]+\\s*\\}");
	
	private static final Pattern WILDCARD_TRIM_PATTERN=Pattern.compile("\\s*(?<wildcard>(\\*|\\+)([0-9]+)?)\\s*");
	
	private static final PermutationGenerator permutationGenerator=new PermutationGenerator();
	
	private static final NGramsGenerator nGramsGenerator=new NGramsGenerator();
	
	private static final PatternTransformer transformer=PatternTransformer.getInstance();
	
	private static final String COMBINATIONS_DELIMETER="|";
	
	public PatternInfo preProcess(String pattern, Map<String, String> variableMap) {
		
		PatternInfo patternInfo=new PatternInfo();
		
		pattern=trimWildCardsInPattern(pattern);
		
		String finalPatternStr=processRecursively(pattern, variableMap, patternInfo);
		
		Pattern patternObj=Pattern.compile(finalPatternStr, Pattern.CASE_INSENSITIVE);
		
		patternInfo.setPattern(patternObj);
		
		return patternInfo;
	}
	
	private String trimWildCardsInPattern(String pattern) {
		
		StringBuilder finalPattern=new StringBuilder();
		
		Matcher wildCardMatcher=WILDCARD_TRIM_PATTERN.matcher(pattern.trim());
		
		int offset=0;
		
		while(offset<pattern.length() && wildCardMatcher.find(offset)) {
			
			String wildcard=wildCardMatcher.group("wildcard");
			finalPattern.append(pattern.substring(offset, wildCardMatcher.start()));
			finalPattern.append(wildcard);
			
			offset=wildCardMatcher.end();
		}
		
		if(offset<pattern.length()) {
			finalPattern.append(pattern.substring(offset));
		}
		
		return finalPattern.toString();
	}
	
	private String processRecursively(String pattern, Map<String, String> variableMap, PatternInfo patternInfo) {
		
		pattern=pattern.trim();
		
		Matcher matcher=PATTERN.matcher(pattern);
		
		StringBuilder finalPattern=new StringBuilder();
		
		int offset=0;
		
		int totalLength=pattern.length();
		
		while(offset<totalLength && matcher.find(offset)){
			
			int startIndex=matcher.start();
			
			char patternChar=pattern.charAt(startIndex);
			
			String temp=pattern.substring(offset, startIndex);
			
			finalPattern.append(transformer.transform(temp).replaceAll("\\s+", "\\\\s+"));
			
			if(temp.endsWith(" ")){
				finalPattern.append("\\s+");
			}

			if(matcher.group().startsWith("regex")) {
				
				int endParenIndex=findMatch(pattern, matcher.end(), '(', ')');
				
				if(endParenIndex==-1)
					throw new ChatbotFrameworkException("Illegal Pattern identified..."+pattern);
				
				String regexGroup=pattern.substring(matcher.end(), endParenIndex);
				
				finalPattern.append("(").append(regexGroup).append(")");
				
				offset = endParenIndex+1;
			}
			else if(patternChar=='*') {
				if(startIndex+1<totalLength) {
					char ch=pattern.charAt(startIndex+1);
					
					if(Character.isDigit(ch)) {
						finalPattern.append("((\\s+)?([^\\s]+\\s*)){"+ch+"}");
						
						offset = startIndex+2;
						continue;
					}
					else {
						finalPattern.append("(\\s+)?([^\\s]\\s*)*");
					}
				}
				else {
					finalPattern.append("(\\s+)?([^\\s]\\s*)*");
				}
				
				offset = startIndex+1;
			}
			else if(patternChar=='+') {
				if(startIndex+1<totalLength) {
					char ch=pattern.charAt(startIndex+1);
					
					if(Character.isDigit(ch)) {
						finalPattern.append("((\\s+)?([^\\s]+\\s*)){"+ch+"}");
						
						offset = startIndex+2;
						continue;
					}
					else {
						finalPattern.append("(\\s+)?([^\\s]\\s*)+");
					}
				}
				else {
					finalPattern.append("(\\s+)?([^\\s]\\s*)+");
				}
				
				offset = startIndex+1;
			}
			else if(patternChar=='(') {
				
				int endParenIndex=findMatch(pattern, startIndex+1, '(', ')');
				
				if(endParenIndex==-1)
					throw new ChatbotFrameworkException("Illegal Pattern identified..."+pattern);
				
				String parenGroup=pattern.substring(startIndex+1, endParenIndex);
				
				String[] parenGroupParts=parenGroup.split("\\|");
				
				if(parenGroupParts.length>0) {
					finalPattern.append("(");
				}
				
				for(String parenGroupPart: parenGroupParts) {
					String processedGroup=processRecursively(parenGroupPart, variableMap, patternInfo);
					finalPattern.append(processedGroup).append("|");
				}
				
				if(parenGroupParts.length>0) {
					finalPattern.deleteCharAt(finalPattern.length()-1);
					finalPattern.append(")");
				}
				
				offset = endParenIndex+1;
			}
			else if(patternChar=='{') {
				int endParenIndex=findMatch(pattern, startIndex+1, '{', '}');
				
				if(endParenIndex==-1)
					throw new ChatbotFrameworkException("Illegal Pattern identified..."+pattern);
				
				String curlyGroup=pattern.substring(startIndex, endParenIndex+1);
				
				Matcher variablePatternMatcher=VARIABLE_PATTERN.matcher(curlyGroup);
				
				if(variablePatternMatcher.matches()) {
					
					String variableName=variablePatternMatcher.group("variable");
					
					int equalIndex=pattern.indexOf("=", startIndex+1);
					
					String processedGroup=processRecursively(pattern.substring(equalIndex+1, endParenIndex).trim(), variableMap, patternInfo);
					
					finalPattern.append("(?<").append(variableName).append(">").append(processedGroup).append(")");
					
					variableMap.put(variableName, null);
					
					offset = endParenIndex+1;
				}
				else {
					throw new ChatbotFrameworkException("Illegal Variable Pattern identified..."+curlyGroup);
				}
			}
			else if(patternChar=='<') {
				
				Matcher unOrderedGroupMatcher=UNORDERED_GROUP_END_PATTERN.matcher(pattern);
				
				if(unOrderedGroupMatcher.find(startIndex+2)) {
					String nGramPattern=unOrderedGroupMatcher.group("nGramPattern");
					
					String unOrderedGroup=pattern.substring(startIndex+2, unOrderedGroupMatcher.start()).trim();

					StringBuilder finalContent=new StringBuilder();
					
					if(nGramPattern==null) {
						// handle as default keyword match
						String oneOrMoreWords="(\\s+)?([^\\s]\\s*)+";
						int currentSize=patternInfo.getKeywordPatternMap().size();
						
						finalPattern.append("(?<keywords").append(currentSize).append(">").append(oneOrMoreWords).append(")");
						
						String[] keywords=PatternTransformer.getInstance().transformWords(unOrderedGroup.trim());
						
						patternInfo.addKeywordPattern("keywords"+currentSize, keywords);
					}
					else if(nGramPattern.equals("_")){
						// handle as permutations match
						permutationGenerator.perm(unOrderedGroup, finalContent, COMBINATIONS_DELIMETER);
					}
					else {
						// handle as nGrams match
						
						int gramSize=Integer.parseInt(nGramPattern.substring(1));
						
						nGramsGenerator.nGrams(unOrderedGroup, gramSize, finalContent, COMBINATIONS_DELIMETER);
					}
					
					finalPattern.append("(").append(finalContent).append(")");
					
					offset = unOrderedGroupMatcher.end();
				}
				else {
					throw new ChatbotFrameworkException("Invalid Unordered Group identified..."+pattern);
				}
			}
		}
		
		if(offset<totalLength) {
			
			String temp=pattern.substring(offset);
			
			finalPattern.append(transformer.transform(temp).replaceAll("\\s+", "\\\\s+"));
			
			if(temp.endsWith(" ")){
				finalPattern.append("\\s+");
			}
		}
		
		return finalPattern.toString();
	}
	
	private int findMatch(String pattern, int startIndex, char openChar, char closeChar) {
		
		int totalPairs=0;
		
		for(int position=startIndex; position<pattern.length(); position++) {
			char currentChar=pattern.charAt(position);
			
			if(currentChar==closeChar) {
				if(totalPairs==0)
				{
					return position;
				}
				else {
					totalPairs--;
				}
			}
			else if(currentChar==openChar) {
				totalPairs++;
			}
		}
		
		return -1;
	}
}
