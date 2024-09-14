package com.task;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.task.services.CommonNamesService;

public class Main {

	// txt file name in resource folder
	private static final String SRC_TXT_FILE = "big.txt";

	// common names file name in resource folder
	private static final String COMMA_SEPERATED_NAMES_FILE = "comma.seperated.names.txt";

	public static void main(String[] args) throws FileNotFoundException, IOException {
		CommonNamesService commonNamesService = new CommonNamesService(SRC_TXT_FILE, COMMA_SEPERATED_NAMES_FILE);
		commonNamesService.readAndAnalyze();
		commonNamesService.aggregationAndPrint();
		commonNamesService.shutdown();
	}
}
