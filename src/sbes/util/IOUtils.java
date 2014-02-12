package sbes.util;

import java.io.File;

public class IOUtils {

	public static String concatPath(final String ... args) {
		StringBuilder builder = new StringBuilder();
		for (String arg : args) {
			builder.append(arg);
			if (!arg.endsWith(File.separator)) {
				builder.append(File.separator);
			}
		}
	
		return builder.toString();
	}
	
	public static String fromCanonicalToPath(final String packageName) {
		return packageName.replaceAll("\\.", File.separator);
	}

	public static String lastDir(final String path) {
		String[] dirs = path.split(File.separator);
		return dirs[dirs.length - 1];
	}

}
