package com.genie.chatbot.conversation.preprocessors;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * @author Rajesh Putta
 */
public class RegexPatternPreprocessor implements IPatternPreprocessor {
	
	public PatternInfo preProcess(String pattern, Map<String, String> variableMap) {
		
		PatternInfo patternInfo=new PatternInfo();
		
		StringBuilder temp=new StringBuilder(pattern);
		
		int offset=0;
		
		while(true){
			int startIndex=temp.indexOf("{", offset);
			
			if(startIndex==-1){
				break;
			}
			
			int endIndex=temp.indexOf("}", startIndex);
			
			if(endIndex==-1) {
				break;
			}
			
			String variable=temp.substring(startIndex+1, endIndex).trim();
			
			if(StringUtils.isNotBlank(variable)){
				variableMap.put(variable, null);
			}
			
			temp.replace(startIndex, startIndex+1, "?<");
			temp.replace(endIndex+1, endIndex+2, ">");
			
			offset=endIndex+2;
		}
		
		pattern=temp.toString();
		
		Pattern patternObj=Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		
		patternInfo.setPattern(patternObj);
		
		return patternInfo;
	}
}
