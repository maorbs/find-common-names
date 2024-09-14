package com.task.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SearchResult {

	private Map<String, List<ResultOffset>> resultMap = new ConcurrentHashMap<String, List<ResultOffset>>();

	public Map<String, List<ResultOffset>> getResultMap() {
		return resultMap;
	}

	public void appendOrCreateResult(String key, List<ResultOffset> result) {

		if (resultMap.containsKey(key)) {
			resultMap.get(key).addAll(result);
		} else {
			resultMap.put(key, result);
		}

	}

	public void appendOrCreateResult(String key, ResultOffset resultOffset) {

		if (resultMap.containsKey(key)) {
			resultMap.get(key).add(resultOffset);
		} else {
			List<ResultOffset> listResultOffSet = new ArrayList<>();
			listResultOffSet.add(resultOffset);
			List<ResultOffset> putIfAbsent = resultMap.putIfAbsent(key, listResultOffSet);
			if(putIfAbsent != null) {
				putIfAbsent.add(resultOffset);
			}
		}

	}

	public void appendOrCreateResult(SearchResult result) {
		result.getResultMap().forEach((key, resultList) -> appendOrCreateResult(key, resultList));
	}

	@Override
	public String toString() {
		final StringBuilder toString = new StringBuilder();
		resultMap.forEach(
				(key, result) -> 
				toString.append(key)
				.append(" -> ")
				.append(result).append(System.lineSeparator()));
		return toString.toString();
	}
}
