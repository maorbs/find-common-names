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
		
		final SearchResult sr = new SearchResult();
		String[] linesText = text.split(System.lineSeparator());
		for (int i = 0; i < linesText.length; i++) {
			final String lw = linesText[i].toLowerCase();
			final int index = i;
			regexNamesMap.entrySet().parallelStream().forEach(entry->{
				Matcher matcher = entry.getValue().matcher(lw);
	        	while (matcher.find()) {
					sr.appendOrCreateResult(entry.getKey(), new ResultOffset(startLineOffset + index + 1, matcher.start()));
	        	}
			});
		}
		return sr;
	}

}
