package sbes.execution;

public abstract class Tool {

	protected String classSignature;
	protected String methodSignature;
	
	public Tool(final String classSignature, final String methodSignature) {
		this.classSignature = classSignature;
		this.methodSignature = methodSignature;
	}
	
	public abstract String[] getCommand();

	public abstract String getTestDirectory();

	public abstract String getTestFilename();

}
