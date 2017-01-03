package com.genie.chatbot.conversation.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.genie.chatbot.context.resolvers.ContextResolverFactory;


/**
 * @author Rajesh Putta
 */
public class ExpressionEvaluator {

	private Pattern quoteString=Pattern.compile("(\"|\')(.+)(\"|\')");
	private Pattern operatorPattern=Pattern.compile("(\\s*)(~=|==|!=|<=|>=|<|^->)(\\s*)");
	private Pattern booleanOperatorPattern=Pattern.compile("(\\s+)(and|or)(\\s+)", Pattern.CASE_INSENSITIVE);
	
	
	public Iterable<?> evaluateAsIterable(String expression, ContextResolverFactory resolverFactory) {
		
		return (Iterable<?>)resolverFactory.resolveField(expression, true);
	}
	
	
	public boolean evaluateBooleanExpression(String expression, ContextResolverFactory resolverFactory) {
		
		Matcher andOrMatcher=booleanOperatorPattern.matcher(expression);
		
		int offset=0;
		
		Boolean finalResult=null;
		String booleanOperator=null;
		
		while(andOrMatcher.find(offset)) {
			int start=andOrMatcher.start();
			int end=andOrMatcher.end();
			
			String expr=expression.substring(offset, start).trim();
			
			finalResult=getBooleanResult(expr, finalResult, booleanOperator, resolverFactory);
			
			booleanOperator=andOrMatcher.group().trim();
			
			offset=end;
		}
		
		String expr=expression.substring(offset);
		finalResult=getBooleanResult(expr, finalResult, booleanOperator, resolverFactory);
		
		return finalResult;
	}

	private Boolean getBooleanResult(String expr, Boolean finalResult, String booleanOperator, ContextResolverFactory resolverFactory) {
		
		boolean result=evaluateRelation(expr, resolverFactory);
		
		if(booleanOperator==null)
		{
			finalResult=Boolean.valueOf(result);
		}
		else
		{
			if(booleanOperator.equalsIgnoreCase("and"))
			{
				finalResult=finalResult.booleanValue() && result;
			}
			else if(booleanOperator.equalsIgnoreCase("or"))
			{
				finalResult=finalResult.booleanValue() || result;
			}
			
			if(!finalResult.booleanValue())
			{
				return false;
			}
		}
		
		return finalResult;
	}
	
	private boolean evaluateRelation(String expr, ContextResolverFactory resolverFactory) {
		
		Matcher operatorMatcher=operatorPattern.matcher(expr);
		
		if(operatorMatcher.find()) {
			
			int startOp=operatorMatcher.start();
			int endOp=operatorMatcher.end();
			
			String leftPart=expr.substring(0, startOp);
			String rightPart=expr.substring(endOp);
		
			String leftPartValue=leftPart;

			if(leftPart.startsWith("$"))
			{
				leftPartValue=(String)resolverFactory.resolveField(leftPart, false);
			}
			
			String rightPartValue=rightPart;
			if(rightPart.startsWith("$"))
			{	
				rightPartValue=(String)resolverFactory.resolveField(rightPart, false);
			}
			
			return evaluate(leftPartValue, operatorMatcher.group(), rightPartValue);
		}
		
		return evaluate(expr, null, null);
	}
	
	private boolean evaluate(String leftValue, String operator, String rightValue){
		
		operator=operator.trim();
		
		String lVal=String.valueOf(leftValue);
		
		if(operator==null)
		{
			return Boolean.valueOf(leftValue);
		}
		
		String rVal=String.valueOf(rightValue);
		
		Matcher matcher=quoteString.matcher(lVal);
		
		if(matcher.matches())
		{
			lVal=lVal.substring(1, lVal.length()-1);
		}
		
		matcher=quoteString.matcher(rVal);
		
		if(matcher.matches())
		{
			rVal=rVal.substring(1, rVal.length()-1);
		}
		
		if(operator.equals("~="))
		{
			return lVal.equalsIgnoreCase(rVal);
		}
		else if(operator.equals("=="))
		{
			return lVal.equals(rVal);
		}
		else if(operator.equals("!="))
		{
			return !lVal.equals(rVal);
		}
		else
		{
			try
			{
				double num1=Double.parseDouble(String.valueOf(lVal));
				double num2=Double.parseDouble(String.valueOf(rVal));
				
				if(operator.equals("<="))
				{
					return num1<=num2;
				}
				else if(operator.equals(">="))
				{
					return num1>=num2;
				}
				else if(operator.equals("<"))
				{
					return num1<num2;
				}
				else if(operator.equals(">"))
				{
					return num1>num2;
				}
			}
			catch (Exception e) {
			}
		}
		
		return false;
	}
}
