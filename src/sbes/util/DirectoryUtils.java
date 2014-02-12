package sbes.util;

import java.io.File;

import sbes.Options;
import sbes.logging.Logger;

public class DirectoryUtils {

	private static final Logger logger = new Logger(DirectoryUtils.class);

	private static DirectoryUtils instance = null;
	private static final String baseDirectory = System.getProperty("user.dir");
	private final String experimentDir;

	private DirectoryUtils() {
		String method = Options.I().getMethodSignature();
		experimentDir = method.substring(0, method.indexOf('['));
	}

	public static DirectoryUtils getInstance() {
		if (instance == null) {
			instance = new DirectoryUtils();
		}
		return instance;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public String createExperimentDir() {
		logger.debug("Creating experiment directory");
		
		String toReturn;
		toReturn = toPath(baseDirectory, experimentDir);
		File dir = new File(toReturn);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		logger.debug("Creating experiment directory - done");
		
		return toReturn;
	}
	
	public String getExperimentDir() {
		return experimentDir;
	}

	public static String toPath(final String ... args) {
		StringBuilder builder = new StringBuilder();
		for (String arg : args) {
			builder.append(arg);
			if (!arg.endsWith(File.separator)) {
				builder.append(File.separator);
			}
		}

		return builder.toString();
	}

	public static String lastDir(final String path) {
		String[] dirs = path.split(File.separator);
		return dirs[dirs.length - 1];
	}

}
