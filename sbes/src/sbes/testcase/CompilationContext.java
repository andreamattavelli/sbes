package sbes.testcase;

public class CompilationContext extends CarvingContext {

	private String classPath;
	private String destinationDirectory;
	
	public CompilationContext(final String testDirectory, final String testFilename, final String destinationDirectory, final String classPath) {
		super(testDirectory, testFilename);
		this.classPath = classPath;
		this.destinationDirectory = destinationDirectory;
	}
	
	public String getClassPath() {
		return classPath;
	}
	
	public String getDestinationDirectory() {
		return destinationDirectory;
	}
	
}
