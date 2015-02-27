package sbes.util;

import java.io.File;
import java.util.regex.Matcher;

public class IOUtils {

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
