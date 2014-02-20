package sbes.util;

import java.io.File;

import sbes.execution.ExecutionResult;
import sbes.option.Options;

public class EvosuiteUtils {

	public static boolean succeeded(ExecutionResult result) {
		String signature = Options.I().getMethodSignature();
		String packagename = IOUtils.fromCanonicalToPath(ClassUtils.getPackage(signature));
		String testDirectory = IOUtils.concatPath(result.getOutputDir(), packagename);
		
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
		
		return true;
	}
	
	public static boolean generatedCandidate(String stdout) {
		if (stdout.contains("Covered 1/1 goals")) {
			return true;
		}
		return false;
	}
	
}
