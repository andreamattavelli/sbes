package sbes.option;

import java.io.File;

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

	@Option(name = "-classes",
			usage = "Path to classes",
			required = true)
	private String classesPath;

	@Option(name = "-junit",
			usage = "Path to JUnit 4 jar",
			required = true)
	private String junitPath;

	@Option(name = "-java",
			usage = "Path to Java excutable")
	private String javaPath = "";

	@Option(name = "-evosuite",
			usage = "Path to EvoSuite jar (included jar name). Default: \"./evosuite.jar\"")
	private String evosuitePath = "./evosuite.jar";

	@Option(name = "-method",
			usage = "Java-like method signature under investigation",
			required = true)
	private String methodSignature;

	@Option(name = "-m_bloat_factor",
			usage = "Method bloat factor")
	private int methodBloatFactor = 20;

	@Option(name = "-test_scenario",
			usage = "Path to Java source file containing initial test scenarios")
	private File scenarioTestPath = null;

	@Option(name = "-scenario_budget",
			usage = "Search budget for test case generation. Default: 30s")
	private int scenarioBudget = 30;

	@Option(name = "-synthesis_budget",
			usage = "Search budget for equivalent sequence synthesis. Default: 180s")
	private int synthesisBudget = 180;

	@Option(name = "-counterexample_budget",
			usage = "Search budget for counterexample synthesis. Default: 180s")
	private int counterexampleBudget = 180;

	//	@Option(name = "--max_time",
	//			usage = "Maximum time limit for synthesis (test scenario generation excluded)")
	//	private int maxTime = 180;

	@Option(name = "-log_level",
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

	public int getTestSearchBudget() {
		return scenarioBudget;
	}

	public int getSearchBudget() {
		return synthesisBudget;
	}
	
	public int getCounterexampleBudget() {
		return counterexampleBudget;
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

	public File getTestScenarioPath() {
		return scenarioTestPath;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public void setClassesPath(String classesPath) {
		this.classesPath = classesPath;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

}
