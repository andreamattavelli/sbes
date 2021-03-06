package sbes.util;

import java.io.File;

import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;

public class ClasspathUtils {

	private static final Logger logger = new Logger(ClasspathUtils.class);
	
	public static void checkClasspath() {
		// check classpath: if the class is not found it raise an exception
		logger.debug("Checking classpath");
		if (Options.I().getTargetMethod() != null) {
			checkClasspath(ClassUtils.getCanonicalClassname(Options.I().getTargetMethod()));
		}
		else {
			checkClasspath(Options.I().getTargetClass());
		}
		logger.debug("Classpath OK");
		logger.debug("Checking EvoSuite");
		File evo = new File(Options.I().getEvosuitePath());
		if (!evo.exists() || !evo.canRead()) {
			logger.error("Could not find or execute EvoSuite!");
			throw new SBESException("Unable to find or run EvoSuite");
		}
		logger.debug("EvoSuite OK");
	}
	
	private static void checkClasspath(final String className) {
		try {
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath());
			Class.forName(className, false, ic.getClassLoader());
		} catch (ClassNotFoundException e) {
			logger.error("Could not find class under test: " + className);
			throw new SBESException(e);
		}
	}
	
	public static String getCompilerClasspath(String stubDirectory) {
		String classPath =	Options.I().getClassesPath() + File.pathSeparatorChar + 
				Options.I().getJunitPath() + File.pathSeparatorChar +
				Options.I().getEvosuitePath() + File.pathSeparatorChar +
				stubDirectory + File.pathSeparatorChar +
				ClasspathUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		return classPath;
	}
	
}
