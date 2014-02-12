package sbes.util;

public class EvosuiteUtils {

	public static boolean checkEvosuiteOutput(String stdout, String stderr) {
		if (stdout.contains("ERROR") || stderr.contains("ERROR")) {
			return false;
		}
		else if (stdout.contains("Error when generating tests") || stderr.contains("Error when generating tests")) {
			return false;
		}
		
		return true;
	}
	
}
