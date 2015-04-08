package sbes.util;

import java.io.File;
import java.util.regex.Matcher;

import sbes.logging.Logger;
import sbes.result.EquivalentSequence;

public class IOUtils {

	private static final String END_MESSAGE = "   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>";
	private static final String START_MESSAGE = "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<   ";

	/*
	 * IOUTILS for printing
	 */
	public static void formatInitMessage(final Logger logger, final String method) {
		logger.info(START_MESSAGE + method + END_MESSAGE);
		logger.info("Generating equivalences for method " + method);
	}
	
	public static void formatEndMessage(final Logger logger, final String method) {
		logger.info(START_MESSAGE.substring(0, START_MESSAGE.length() - 3) + endString(method.length() + 6) + END_MESSAGE.substring(3));
	}
	
	private static String endString(int length) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length/2; i++) {
			sb.append('<');
		}
		for (int i = (length/2); i < length; i++) {
			sb.append('>');
		}
		return sb.toString();
	}

	public static void formatIterationStartMessage(final Logger logger, final DirectoryUtils directory) {
		logger.info(START_MESSAGE + "Starting synthesis attempt #" + directory.getEquivalences() + END_MESSAGE);
	}

	public static void formatIterationEndMessage(final Logger logger, final DirectoryUtils directory) {
		logger.info(START_MESSAGE + "Finished synthesis attempt #" + directory.getEquivalences() + END_MESSAGE);
	}
	
	public static void printEquivalence(EquivalentSequence eqSeq) {
		String equivalence = eqSeq.toString();
		String lines[] = StringUtils.split(equivalence, '\n');
		
		System.out.println();
		for (int i = 0; i < lines.length; i++) {
			String string = lines[i];
			System.out.println(StringUtils.repeat(' ', 30) + (i+1) + ". " + string);
		}
		System.out.println();
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
