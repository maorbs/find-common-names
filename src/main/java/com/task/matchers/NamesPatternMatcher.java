package com.task.matchers;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.task.model.ResultOffset;
import com.task.model.SearchResult;



public class NamesPatternMatcher implements Callable<SearchResult> {
	
	private long startLineOffset;
	private String text;
	private Map<String, Pattern> regexNamesMap;
	

	public NamesPatternMatcher(String text, Map<String, Pattern> regexPattenMap, long startLineOffset, long startCharOffset) {
		this.startLineOffset = startLineOffset;
		this.text = text;
		this.regexNamesMap = regexPattenMap;
	}
	
	@Override
	public SearchResult call() throws Exception {
		
		SearchResult sr = new SearchResult();
		String[] linesText = text.split(System.lineSeparator());
		for (int i = 0; i < linesText.length; i++) {
	        for (Map.Entry<String, Pattern> pattenMap : regexNamesMap.entrySet()) {
	        	String lowerCaseLine = linesText[i].toLowerCase();
				Matcher matcher = pattenMap.getValue().matcher(lowerCaseLine);
	        	while (matcher.find()) {
					sr.appendOrCreateResult(pattenMap.getKey(), new ResultOffset(startLineOffset + i + 1, matcher.start()));
	        	}
	        }
		}
		return sr;
	}

}
