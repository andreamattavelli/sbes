package sbes.util;

import java.io.File;

import sbes.logging.Logger;

public class DirectoryUtils {

	private static final Logger logger = new Logger(DirectoryUtils.class);

	private static DirectoryUtils instance = null;
	private static final String baseDirectory = System.getProperty("user.dir");
	private static String dumpDirectory = null;

	private DirectoryUtils() {}

	public static DirectoryUtils getInstance() {
		if (instance == null) {
			instance = new DirectoryUtils();
		}
		return instance;
	}

	public String getBaseDirectory() {
		return baseDirectory;
	}

	public static String createExperimentDir() {
		logger.info("Creating experiment directory");
		
		String toReturn;
		String dirName = Long.toString(System.currentTimeMillis());
		toReturn = toPath(baseDirectory, dirName);
		File dir = new File(toReturn);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		logger.info("Experiment directory created successfully: " + dumpDirectory);

		return toReturn;
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
