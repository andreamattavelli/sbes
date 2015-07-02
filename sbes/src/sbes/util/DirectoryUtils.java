package sbes.util;

import java.io.File;

import sbes.logging.Logger;
import sbes.option.Options;

public class DirectoryUtils {

	private static final Logger logger = new Logger(DirectoryUtils.class);

	private static DirectoryUtils instance = null;
	private static String baseDirectory = System.getProperty("user.dir");
	private static String experimentDir;
	private static String scenariosDir = "init-scenarios";
	private static String evosuiteDir = "evosuite-tests";
	private static String jbseDir = "jbse-tests";
	private static String stubDir = "stubs";
	private static String firstStubDir = "first-stage";
	private static String secondStubDir = "second-stage";
	private static String equivalencePrefix = "equivalence_";
	private static String iterationPrefix = "iteration_";
	
	private int equivalence;
	private int firstStubs;
	private int secondStubs;

	private DirectoryUtils() {
		String method = Options.I().getTargetMethod();
		experimentDir = method;
		equivalence = 0;
		firstStubs = 0;
		secondStubs = 0;
	}

	public static DirectoryUtils I() {
		if (instance == null) {
			instance = new DirectoryUtils();
			createExperimentDirs();
		}
		return instance;
	}
	
	public static void reset() {
		instance = null;
	}

	private static void createExperimentDirs() {
		logger.debug("Creating experiment directories");
		
		try {
			// base dir
			String toReturn;
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir);
			File base = new File(toReturn);
			if (!base.exists()) {
				base.mkdirs();
			}
		}
		catch (SecurityException e) {
			logger.error("Unable to create experiment directories due: ", e);
		}

		logger.debug("Creating experiment directories - done");
	}
	
	public int getEquivalences() {
		return equivalence;
	}
	
	public void createEquivalenceDirs() {
		logger.debug("Creating experiment directories");
		
		equivalence++;
		firstStubs = 0;
		secondStubs = 0;
		try {
			// base dir
			String toReturn;
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence));
			File base = new File(toReturn);
			if (!base.exists()) {
				base.mkdirs();
			}
			// stub dir
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), stubDir);
			File stub = new File(toReturn);
			if (!stub.exists()) {
				stub.mkdirs();
			}
			// synthesis dir
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), evosuiteDir);
			File synthesis = new File(toReturn);
			if (!synthesis.exists()) {
				synthesis.mkdirs();
			}
		}
		catch (SecurityException e) {
			logger.error("Unable to create experiment directories due: ", e);
		}

		logger.debug("Creating experiment directories - done");
	}
	
	public String getBaseDirectory() {
		return baseDirectory;
	}
	
	public String getExperimentDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir);
	}
	
	public String getTestScenarioDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir, scenariosDir);
	}

	public String getFirstStubEvosuiteDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), evosuiteDir, firstStubDir, iterationPrefix + Integer.toString(firstStubs));
	}

	public String getSecondStubEvosuiteDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), evosuiteDir, secondStubDir, iterationPrefix + Integer.toString(secondStubs));
	}

	public String getSecondStubJBSEDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), jbseDir, secondStubDir, iterationPrefix + Integer.toString(secondStubs));
	}
	
	public String getFirstStubDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), stubDir, firstStubDir, iterationPrefix + Integer.toString(firstStubs));
	}

	public String getSecondStubDir() {
		return IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), stubDir, secondStubDir, iterationPrefix + Integer.toString(secondStubs));
	}

	public void createFirstStubDir() {
		firstStubs++;
		logger.debug("Creating directory for first stage stub #" + firstStubs);
		
		try {
			String toReturn;
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), stubDir, firstStubDir, iterationPrefix + Integer.toString(firstStubs));
			File stub = new File(toReturn);
			if (!stub.exists()) {
				stub.mkdirs();
			}
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), evosuiteDir, firstStubDir, iterationPrefix + Integer.toString(firstStubs));
			File synthesis = new File(toReturn);
			if (!synthesis.exists()) {
				synthesis.mkdirs();
			}
		}
		catch (SecurityException e) {
			logger.error("Unable to create first stage stub directory due: ", e);
		}

		logger.debug("Creating directory for first stage stub - done");
	}
	
	public void createSecondStubDir() { // consider refactoring
		secondStubs++;
		logger.debug("Creating directory for second stage stub #" + secondStubs);
		
		try {
			String toReturn;
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), stubDir, secondStubDir, iterationPrefix + Integer.toString(secondStubs));
			File stub = new File(toReturn);
			if (!stub.exists()) {
				stub.mkdirs();
			}
			toReturn = IOUtils.concatFilePath(baseDirectory, experimentDir, equivalencePrefix + Integer.toString(equivalence), evosuiteDir, secondStubDir, iterationPrefix + Integer.toString(secondStubs));
			File synthesis = new File(toReturn);
			if (!synthesis.exists()) {
				synthesis.mkdirs();
			}
		}
		catch (SecurityException e) {
			logger.error("Unable to create second stage stub directory due: ", e);
		}

		logger.debug("Creating directory for second stage stub - done");
	}
	
}
