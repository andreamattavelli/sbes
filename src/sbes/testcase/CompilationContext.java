package sbes.testcase;

public class CompilationContext extends CarvingContext {

	private String classPath;
	
	public CompilationContext(String testDirectory, String testFilename, String classPath) {
		super(testDirectory, testFilename);
		this.classPath = classPath;
	}
	
	public String getClassPath() {
		return classPath;
	}
	
}
