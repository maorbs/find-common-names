package com.task.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import com.task.matchers.NamesPatternMatcher;
import com.task.model.SearchResult;

public class CommonNamesService {
	// Lines to read
	private static final int LINES = 1000;
	// Fixed thread pool
	private static final int FIXED_THREAD_POOL = 4;
	
	private String resourceFilePath;
	private String  resourceNameFilePath;
	private CompletionService<SearchResult> completionService;
	private List<Future<SearchResult>> futureTasks;
	private ExecutorService executor;
	private long duration;
	
	public CommonNamesService(String resourceFilePath, String resourceNameFilePath) {
		// Create executor service
		this.executor = Executors.newFixedThreadPool(FIXED_THREAD_POOL);
		this.completionService = new ExecutorCompletionService<SearchResult>(executor);
		this.resourceFilePath = resourceFilePath;
		this.resourceNameFilePath = resourceNameFilePath;
	}
	
	public void shutdown() {
		this.executor.shutdown();
	}
	
	public List<Future<SearchResult>> readAndAnalyze() throws IOException, FileNotFoundException {

		futureTasks = new ArrayList<>();
		Set<String> commonNameSet = getCommonNameFromFile(resourceNameFilePath);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(resourceFilePath)))) {
			Map<String, Pattern> regexPattenMap = createRegexPattenMap(commonNameSet);
			long lineCounter = 0;
			long charCounter = 0;
			long charOffset = 0;
			long lineOffset = 0;
			String line;
			StringBuilder text = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				lineCounter++;
				text.append(line).append(System.lineSeparator());
				charCounter = text.length();
				// Call matcher every X lines (defined above)
				if (lineCounter % LINES == 0) {
					NamesPatternMatcher matcher = new NamesPatternMatcher(text.toString(), regexPattenMap, lineOffset,
							charOffset);
					futureTasks.add(completionService.submit(matcher));
					duration = System.currentTimeMillis();
					text = new StringBuilder();
					lineOffset = lineCounter;
					charOffset = charCounter;
				}
			}
			if (!text.isEmpty()) {
				NamesPatternMatcher matcher = new NamesPatternMatcher(text.toString(), regexPattenMap, lineOffset,
						charOffset);
				futureTasks.add(completionService.submit(matcher));
			}
		}
		return futureTasks;
	}
	
	public void aggregationAndPrint() {
		int receivedResult = 0;
		SearchResult sr = new SearchResult();
		int size = futureTasks.size();

		while (receivedResult < size) {
			try {
				Future<SearchResult> resultFuture = completionService.take(); // blocks if none available
				receivedResult++;
				if (size == receivedResult) {
					duration = System.currentTimeMillis() - duration;
				}
				SearchResult result = resultFuture.get();
				if (!result.getResultMap().isEmpty()) {
					sr.appendOrCreateResult(result);
				}

			} catch (InterruptedException | ExecutionException e) {
				System.err.println("failed execution " + e.getMessage());
				receivedResult++;
			}
		}

		System.out.println("##### Most common names found in txt file: #####");
		System.out.println("------------------------------------------------");
		System.out.println(sr.toString());
		System.out.println("Duration time " + duration + " ms.");
	}
	
	private  Set<String> getCommonNameFromFile(String commonNamesPath) throws IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		Set<String> names = new HashSet<String>();
		String line;
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(classloader.getResourceAsStream(commonNamesPath)))) {
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				names.addAll(new HashSet<String>(Arrays.asList(values)));
			}
		}
		return names;
	}
	
	private  Map<String, Pattern> createRegexPattenMap(Set<String> names) {
		Map<String, Pattern> regexNamesMap = new HashMap<>();
		names.forEach(name -> {
			String lowerCaseName = name.toLowerCase();
			regexNamesMap.put(name, Pattern.compile(
					"(^" + lowerCaseName + "([\\W])?)|(" + lowerCaseName + "$)|([\\W](" + lowerCaseName + ")([\\W]))"));
		});
		return regexNamesMap;
	}
}
