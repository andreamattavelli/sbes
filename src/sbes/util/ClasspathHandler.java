package sbes.util;

import sbes.Options;
import sbes.SBESException;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;

public class ClasspathHandler {

	private static final Logger logger = new Logger(ClasspathHandler.class);
	
	public static void checkClasspath() {
		// check classpath: if the class is not found it raise an exception
		logger.debug("Checking classpath");
		checkClasspath(ClassUtils.getCanonicalClassname(Options.I().getMethodSignature()));
		logger.debug("Classpath OK");
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
