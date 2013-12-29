package sbes;

import org.kohsuke.args4j.Option;

public class Options {

	private static Options instance = null;

	private Options() { }

	public static Options I() {
		if (instance == null) {
			instance = new Options();
		}
		return instance;
	}
	
	@Option(name = "--classes",
			usage = "Path to classes",
			required = true)
	private String classesPath;
	
	@Option(name = "--method",
			usage = "Java-like method signature under investigation",
			required = true)
	private String methodSignature;

	public String getClassesPath() {
		return classesPath;
	}

	public String getMethodSignature() {
		return methodSignature;
	}
	
}
