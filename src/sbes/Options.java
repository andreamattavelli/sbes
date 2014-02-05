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

	@Option(name = "--m_bloat_factor",
			usage = "Method bloat factor")
	private int methodBloatFactor = 20;
	
	@Option(name = "--dir",
			usage = "Directory where to store the stubs")
	private String dir = ".";
	
	@Option(name = "--max_iterations",
			usage = "Maximum number of iterations of stages 1/2")
	private int maxIterations;

	@Option(name = "--search_budget",
			usage = "Search budget for test case generation")
	private int searchBudget = 60;


	public String getClassesPath() {
		return classesPath;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public int getMethodBloatFactor() {
		return methodBloatFactor;
	}
	
	public String getDir() {
		return dir;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public int getSearchBudget() {
		return searchBudget;
	}
	
}
