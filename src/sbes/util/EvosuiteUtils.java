package sbes.util;

public class EvosuiteUtils {

	public static boolean succeeded(String stdout, String stderr) {
		if (stdout.contains("ERROR") || stderr.contains("ERROR")) {
			return false;
		}
		else if (stdout.contains("Error when generating tests") || stderr.contains("Error when generating tests")) {
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
