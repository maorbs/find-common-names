package com.task;

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

public class Main {

	// txt file name in resource folder
	private static final String SRC_TXT_FILE = "big.txt";

	// common names file name in resource folder
	private static final String COMMA_SEPERATED_NAMES_FILE = "comma.seperated.names.txt";

	// Lines to read
	private static final int LINES = 1000;

	// Fixed thread pool
	private static final int FIXED_THREAD_POOL = 4;

	private static long duration;

	public static void main(String[] args) throws FileNotFoundException, IOException {

		// Create executor service
		ExecutorService executor = Executors.newFixedThreadPool(FIXED_THREAD_POOL);

		CompletionService<SearchResult> completionService = new ExecutorCompletionService<SearchResult>(executor);

		// analyze and get the number of task submitted
		List<Future<SearchResult>> taskSubmit = readAndAnalyze(SRC_TXT_FILE, COMMA_SEPERATED_NAMES_FILE,
				completionService);

		// Aggregation and print result
		aggregationAndPrint(completionService, taskSubmit);

		// Shutdown executor
		executor.shutdown();

	}

	/*
	 * Reads the input file path and search the common names from the input returns
	 * the number of tasks that was submitted
	 */
	private static List<Future<SearchResult>> readAndAnalyze(String filePath, String namesFile,
			CompletionService<SearchResult> completionService) throws IOException, FileNotFoundException {

		List<Future<SearchResult>> future = new ArrayList<>();
		Set<String> commonNameSet = getCommonNameFromFile(namesFile);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(classloader.getResourceAsStream(filePath)))) {
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
					future.add(completionService.submit(matcher));
					duration = System.currentTimeMillis();
					text = new StringBuilder();
					lineOffset = lineCounter;
					charOffset = charCounter;
				}
			}
			if (!text.isEmpty()) {
				NamesPatternMatcher matcher = new NamesPatternMatcher(text.toString(), regexPattenMap, lineOffset,
						charOffset);
				future.add(completionService.submit(matcher));
			}
		}
		return future;
	}

	private static void aggregationAndPrint(CompletionService<SearchResult> completionService, List<Future<SearchResult>> taskSubmit) {
		int receivedResult = 0;
		SearchResult sr = new SearchResult();
		int size = taskSubmit.size();

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

	private static Set<String> getCommonNameFromFile(String commonNamesPath) throws IOException {
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

	private static Map<String, Pattern> createRegexPattenMap(Set<String> names) {
		Map<String, Pattern> regexNamesMap = new HashMap<>();
		names.forEach(name -> {
			String lowerCaseName = name.toLowerCase();
			regexNamesMap.put(name, Pattern.compile(
					"(^" + lowerCaseName + "([\\W])?)|(" + lowerCaseName + "$)|([\\W](" + lowerCaseName + ")([\\W]))"));
		});
		return regexNamesMap;
	}
}
