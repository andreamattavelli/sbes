package sbes.util;

import java.io.File;

import sbes.Options;
import sbes.SBESException;
import sbes.evosuite.Evosuite;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;

public class ClasspathHandler {

	private static final Logger logger = new Logger(ClasspathHandler.class);
	
	public static void checkClasspath() {
		// check classpath: if the class is not found it raise an exception
		logger.debug("Checking classpath");
		checkClasspath(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()));
		logger.debug("Classpath OK");
		logger.debug("Checking EvoSuite");
		File evo = new File(IOUtils.concatPath(DirectoryUtils.I().getBaseDirectory(), Evosuite.jarName));
		if (!evo.exists() || !evo.canRead()) {
			logger.error("Could not find or execute EvoSuite!");
			throw new SBESException("Unable to find or run EvoSuite");
		}
		logger.debug("EvoSuite OK");
	}
	
	private static void checkClasspath(final String className) {
		try {
			Class.forName(className, false, InternalClassloader.getInternalClassLoader());
		} catch (ClassNotFoundException e) {
			logger.error("Could not find class under test: " + className);
			throw new SBESException(e);
		}
	}
	
}
