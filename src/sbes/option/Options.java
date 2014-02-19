package sbes.option;

import org.kohsuke.args4j.Option;

import sbes.logging.Level;

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
	
	@Option(name = "--junit",
			usage = "Path to JUnit 4 jar",
			required = true)
	private String junitPath;
	
	@Option(name = "--java",
			usage = "Path to Java excutable")
	private String javaPath = "";
	
	@Option(name = "--evosuite",
			usage = "Path to EvoSuite jar")
	private String evosuitePath = ".";
	
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

	@Option(name = "--test_search_budget",
			usage = "Search budget for test case generation")
	private int testSearchBudget = 60;

	@Option(name = "--search_budget",
			usage = "Search budget for equivalent sequence synthesis")
	private int searchBudget = 180;
	
	@Option(name = "--max_time",
			usage = "Maximum time limit for synthesis (test scenario generation excluded)")
	private int maxTime = 180;
	
	@Option(name = "--log_level",
			usage = "Logging level to be used: FATAL, ERROR, WARN, INFO, DEBUG",
			handler = LevelHandler.class)
	private Level logLevel;
	
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

	public int getTestSearchBudget() {
		return testSearchBudget;
	}
	
	public int getSearchBudget() {
		return searchBudget;
	}

	public String getJunitPath() {
		return junitPath;
	}
	
	public String getJavaPath() {
		return javaPath;
	}
	
	public String getEvosuitePath() {
		return evosuitePath;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}
	
	
	
}
