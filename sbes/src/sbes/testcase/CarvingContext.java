package sbes.testcase;

public class CarvingContext {

	protected String testDirectory;
	protected String testFilename;
	
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
