package com.genie.chatbot.conversation.components;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;

import org.apache.commons.lang.StringUtils;

import com.genie.chatbot.context.resolvers.ContextResolverFactory;
import com.genie.chatbot.context.resolvers.ResolverType;
import com.genie.chatbot.conversation.exceptions.ChatbotFrameworkException;
import com.genie.chatbot.conversation.smartmatch.StopWordsHandler;

/**
 * @author Rajesh Putta
 */
public class ConversationUtility {
	
    public static final String LOCALE="en_us";
    
    private static final ContextResolverFactory resolverFactory=ContextResolverFactory.getInstance();
    
	public static String postProcessPattern(String response, Map<String, String> variableMap) {
		
		resolverFactory.getResolver(ResolverType.MAP).setContext(variableMap);
		
		StringBuilder temp=new StringBuilder(response);
		
		int offset=0;
		
		while(true){
			int startIndex=temp.indexOf("${", offset);
			
			if(startIndex==-1){
				break;
			}
			
			int endIndex=findMatch(temp.toString(), startIndex+2, '{', '}');
			
			if(endIndex==-1) {
				break;
			}
			
			String variable=temp.substring(startIndex+2, endIndex).trim();
			
			String result="";
			
			if(StringUtils.isNotBlank(variable)){
//				result=variableMap.get(variable);
				result=(String)resolverFactory.resolveField(variable, false);
			}
			
			if(result==null) {
				result="";
			}
			
			temp.replace(startIndex, endIndex+1, result);
			
			offset=endIndex+1;
		}
		
		return temp.toString();
	}
	
	public static int findMatch(String pattern, int startIndex, char openChar, char closeChar) {
		
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

	
	public static String processDelimitedPattern(String text, Map<String, String> variableMap) {
		
		StringBuilder temp=new StringBuilder(text);
		
		while(true){
			int startIndex=temp.indexOf("${");
			
			if(startIndex==-1){
				break;
			}
			
			int endIndex=findMatch(temp.toString(), startIndex+2, '{', '}');
			
			if(endIndex==-1) {
				break;
			}
			
			String variable=temp.substring(startIndex+2, endIndex).trim();
			
			String result="";
			
			if(StringUtils.isNotBlank(variable)){
				result=(String)resolverFactory.resolveField(variable, false);
				
				if(result==null) {
					result=variableMap.get(variable);
				}
			}
			
			if(result==null) {
				result="";
			}
			
			temp.replace(startIndex, endIndex+1, result);
		}
		
		return temp.toString();
	}	
	
	public static String generateRandomId() {
		return UUID.randomUUID().toString();
	}
	
    public static Set<String> getNamedGroupsForRegexp(String regex) {
    	
        Set<String> namedGroups = new HashSet<String>();

        Matcher matcher = Pattern.compile("\\(\\?<(?<name>[a-zA-Z0-9]+)>").matcher(regex);

            while (matcher.find()) {
                namedGroups.add(matcher.group("name"));
            }

            return namedGroups;
    }
    
    public static InputStream loadStream(String configPath, boolean absolutePath) {
    	
    	try
    	{
	    	if(StringUtils.isBlank(configPath)) {
	    		throw new IllegalArgumentException("configuration path cannot be null...");
	    	}
	    	
	    	if(absolutePath) {
	    		return new BufferedInputStream(new FileInputStream(configPath));
	    	}
	    	
	    	return ConversationUtility.class.getResourceAsStream(configPath);
	    	
    	}catch(Exception e) {
    		throw new ChatbotFrameworkException(e);
    	}
    }
    
    public static void loadOpenNlpModels(String baseConfigPath, Map<String, TokenNameFinderModel> nlpModels, Map<String, POSModel> posModels)
    {
        InputStream stream=null;
        BufferedReader reader=null;
        
        try
        {
            stream=loadStream(baseConfigPath+"open-nlp-models.properties", true);
            
            if(stream==null)
                return;
            
            reader=new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            
            String line=null;
            
            while((line=reader.readLine())!=null)
            {
                line=line.trim();
                
                if(line.startsWith("#"))
                    continue;
                
                int equalIndex=line.indexOf("=");
                
                if(equalIndex>0)
                {
                    String key=line.substring(0, equalIndex).trim();
                    String paths=line.substring(equalIndex+1).trim();
                    
                    String[] pathArray=paths.split(",");
                    
                    if(pathArray.length!=2)
                    {
                        throw new ChatbotFrameworkException("Illegal Open NLP Models configuration...."+line);
                    }
                    
                    pathArray[0]=pathArray[0].trim();
                    pathArray[1]=pathArray[1].trim();
                    
                    if(!pathArray[0].equals("") && !pathArray[1].equals(""))
                    { 
                        InputStream nerStream=null;
                        InputStream posStream=null;
                        
                        try
                        {
                            nerStream=loadStream(baseConfigPath+pathArray[0], true);
                            
                            posStream=loadStream(baseConfigPath+pathArray[1], true);
                            
                            if(nerStream!=null)
                            {
                                TokenNameFinderModel maxentModel = new TokenNameFinderModel(nerStream);
                                
                                nlpModels.put(key, maxentModel);
                            }
                            
                            if(posStream!=null)
                            {
                                POSModel maxentModel = new POSModel(posStream);
                                
                                posModels.put(key, maxentModel);
                            }
                            
                        }catch (Exception e) {
                        	throw new ChatbotFrameworkException(e);
                        }
                        finally{
                            closeInputStream(nerStream);
                            closeInputStream(posStream);
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new ChatbotFrameworkException(e);
        } catch (IOException e) {
        	throw new ChatbotFrameworkException(e);
        }
        finally{
            closeReader(reader);
        }           
    }
    
    public static void closeInputStream(InputStream stream)
    {
        if(stream!=null)
        {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void closeReader(Reader reader)
    {
        if(reader!=null)
        {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
	public static String stringify(Set<String> dataSet, String delimeter) {
		
		if(dataSet==null || dataSet.isEmpty())
			return "";
		
		StringBuilder finalContent=new StringBuilder();
		
		for(String data: dataSet) {
			finalContent.append(data).append(delimeter);
		}
		
		finalContent.delete(finalContent.length()-delimeter.length(), finalContent.length());
		
		return finalContent.toString();
	}
    
	public static String stringify(String[] dataSet, String delimeter) {
		
		if(dataSet==null || dataSet.length==0)
			return "";
		
		StringBuilder finalContent=new StringBuilder();
		
		for(String data: dataSet) {
			finalContent.append(data).append(delimeter);
		}
		
		finalContent.delete(finalContent.length()-delimeter.length(), finalContent.length());
		
		return finalContent.toString();
	}
	
	public static Map<String, String> loadFlatFileContent(String filePath) {
		
		Map<String, String> dataMap = new HashMap<String, String>();
		
		InputStream stream=loadStream(filePath, true);
		
		BufferedReader reader = null;
		
		try {
			reader=new BufferedReader(new InputStreamReader(stream, "UTF8"));
			
			String temp=null;
			
			while((temp=reader.readLine())!=null) {
				if(temp.startsWith("#")) {
					continue;
				}
				
				int index=temp.indexOf("=");
				
				String key=null;
				String value=null;
				
				if(index>-1){
					key=temp.substring(0, index).trim();
					value=temp.substring(index+1).trim();
				}
				else
				{
					key=temp.trim();
					value=key;
				}
				
				dataMap.put(key, value);
			}
			
		} catch (UnsupportedEncodingException e) {
			throw new ChatbotFrameworkException(e);
		} catch (IOException e) {
			throw new ChatbotFrameworkException(e);
		} finally {
			closeReader(reader);
		}
		
		return dataMap;
	}
	
    public static String getFuzzyQueryWithoutStopwords(String userQuery)
    {
        StringBuilder finalQuery=new StringBuilder();
        
        String[] tokens=userQuery.split("\\s+");
        
        StopWordsHandler stopWordsHandler=StopWordsHandler.getInstance();
        
        Map<String, String> stopWordsMap=stopWordsHandler.getStopWordsMap();
        
        for(String token: tokens)
        {
            boolean isStopWord=stopWordsMap.containsKey(token.toLowerCase());
            
            if(isStopWord)
            {
               continue;
            }
            
            // fuzzy search with 70% levenshtein distance
            finalQuery.append(token).append("~0.7 ");
        }
        
        return finalQuery.toString();
    }
        
}
