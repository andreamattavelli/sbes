package sbes.util;

import java.io.File;
import java.util.regex.Matcher;

import sbes.logging.Logger;

public class IOUtils {

	/*
	 * IOUTILS for printing
	 */
	public static void formatInitMessage(Logger logger, String method) {
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<   " + method
				+ "   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		logger.info("Generating equivalences for method " + method);
	}

	public static void formatIterationStartMessage(Logger logger, DirectoryUtils directory) {
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<   "
				+ "Starting synthesis attempt #" + directory.getEquivalences()
				+ "   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	public static void formatIterationEndMessage(Logger logger, DirectoryUtils directory) {
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<   "
				+ "Finished synthesis attempt #" + directory.getEquivalences()
				+ "   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}
	
	
	
	/*
	 * IOUTILS for file system
	 */
	public static String concatFilePath(final String ... args) {
		StringBuilder builder = new StringBuilder();
		for (String arg : args) {
			builder.append(arg);
			if (!arg.endsWith(File.separator)) {
				builder.append(File.separator);
			}
		}
		return builder.toString();
	}
	
	public static String concatClassPath(final String ... args) {
		StringBuilder builder = new StringBuilder();
		for (String arg : args) {
			builder.append(arg);
			if (!arg.endsWith(File.pathSeparator)) {
				builder.append(File.pathSeparator);
			}
		}
		return builder.toString();
	}
	
	public static String fromCanonicalToPath(final String packageName) {
		return packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
	}

	public static String lastDir(final String path) {
		String[] dirs = path.split(File.separatorChar=='\\' ? "\\\\" : File.separator);
		return dirs[dirs.length - 1];
	}
	
}
