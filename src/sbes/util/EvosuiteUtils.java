package sbes.util;

import java.io.File;

import sbes.execution.ExecutionResult;
import sbes.option.Options;

public class EvosuiteUtils {

	public static boolean succeeded(final ExecutionResult result) {
		String signature = Options.I().getTargetMethod();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatFilePath(result.getOutputDir(), packagename);
		
		String file = testDirectory + File.separatorChar + result.getFilename();
		if (!new File(file).exists()) {
			return false;
		}
		
		if (result.getStdout().contains("ERROR") || result.getStderr().contains("ERROR")) {
			return false;
		}
		else if (result.getStdout().contains("Error when generating tests") || result.getStderr().contains("Error when generating tests")) {
			return false;
		}
		else if (result.getStdout().contains("Failed to register client services") || result.getStderr().contains("Failed to register client services")) {
			return false;
		}
		
		return true;
	}
	
	public static boolean generatedCandidate(final String stdout) {
		if (stdout.contains("Covered 1/1 goals")) {
			return true;
		}
		return false;
	}
	
}
