package sbes.testcase;

public class CarvingContext {

	private String testDirectory;
	private String testFilename;
	
	public CarvingContext(String testDirectory, String testFilename) {
		this.testDirectory = testDirectory;
		this.testFilename = testFilename;
	}

	public String getTestDirectory() {
		return testDirectory;
	}

	public String getTestFilename() {
		return testFilename;
	}
	
}
