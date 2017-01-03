package com.genie.chatbot.conversation.smartmatch;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Rajesh Putta
 */
public class SmartMatchTracker {

	private Map<TrainedQuestion, Integer> matches = new LinkedHashMap<TrainedQuestion, Integer>();

	public void incrementMatcher(TrainedQuestion tQuestion, int increment) {
		Integer matchCount = this.matches.get(tQuestion);

		if (matchCount == null) {
			matchCount = 0;
		}

		this.matches.put(tQuestion, matchCount + increment);
	}
	
	public void removeMatcher(TrainedQuestion tQuestion) {
		this.matches.remove(tQuestion);
	}
	
	public void clear() {
		this.matches.clear();
	}
	
	public Integer getMatchesForQuestion(String question) {
		Integer matchCount = this.matches.get(question);

		if (matchCount == null) {
			matchCount = 0;
		}
		
		return matchCount;
	}
	
	public Entry<TrainedQuestion, Integer> getTopper() {
		
		Map<TrainedQuestion, Integer> sortedMap=this.sortByMatches(false);
		
		Iterator<Entry<TrainedQuestion, Integer>> iterator=sortedMap.entrySet().iterator();
		
		if(iterator.hasNext()) {
			return iterator.next();
		}
		
		return null;
	}
	
	public void displayMatches() {
		Map<TrainedQuestion, Integer> sortedMap=sortByMatches(false);
		
		Set<Entry<TrainedQuestion, Integer>> entrySet=sortedMap.entrySet();
		
		for(Entry<TrainedQuestion, Integer> entry: entrySet) {
			System.out.println(entry.getKey().getBaseQuestion()+"\t=>\t"+entry.getValue());
		}
	}

	public Map<TrainedQuestion, Integer> sortByMatches(final boolean isAscending) {
		
		List<Map.Entry<TrainedQuestion, Integer>> list = new LinkedList<Map.Entry<TrainedQuestion, Integer>>(
				this.matches.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<TrainedQuestion, Integer>>() {
			public int compare(Map.Entry<TrainedQuestion, Integer> o1, Map.Entry<TrainedQuestion, Integer> o2) {
				
				return isAscending?(o1.getValue()).compareTo(o2.getValue()):(o2.getValue()).compareTo(o1.getValue()); 
			}
		});

		Map<TrainedQuestion, Integer> result = new LinkedHashMap<TrainedQuestion, Integer>();
		
		for (Map.Entry<TrainedQuestion, Integer> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		
		return result;
	}
}
