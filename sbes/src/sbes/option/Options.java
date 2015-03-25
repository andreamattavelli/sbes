package sbes.option;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import sbes.logging.Level;
import sbes.logging.Logger;
import sbes.stoppingcondition.StoppingConditionType;
import sbes.stoppingcondition.TimeMeasure;

public class Options {

	private static Options instance = null;

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

	@Option(name = "-target_class",
			usage = "Java class signature under investigation")
	private String classSignature = null;

	@Option(name = "-counterexample_budget",
			usage = "Search budget for counterexample synthesis. Default: 180s")
	private int counterexampleBudget = 180;

	@Option(name = "-evosuite",
			usage = "Path to EvoSuite jar (included jar name). Default: \"./evosuite.jar\"")
	private String evosuitePath = "./evosuite.jar";
	
	@Option(name = "-prune_scenarios",
			usage = "Heuristically pruning initial test scenarios to one")
	private boolean heuristicPruningScenarios = true;

	@Option(name = "-java",
			usage = "Path to Java excutable")
	private String javaPath = "";
	
	@Option(name = "-junit",
			usage = "Path to JUnit 4 jar",
			required = true)
	private String junitPath;

	@Option(name = "-local_search_rate",
			usage = "Apply local search at every X generation")
	private int local_search_rate = -1;
	
	@Option(name = "-log_level",
			usage = "Logging level to be used: FATAL, ERROR, WARN, INFO, DEBUG",
			handler = LevelHandler.class)
	private Level logLevel;

	@Option(name = "-m_bloat_factor",
			usage = "Method bloat factor")
	private int methodBloatFactor = 20;

	@Option(name = "-target_method",
			usage = "Java-like method signature under investigation")
	private String methodSignature = null;
	
	@Option(name = "-scenario_budget",
			usage = "Search budget for test case generation. Default: 30s")
	private int scenarioBudget = 30;

	@Option(name = "-test_scenario",
			usage = "Path to Java source file containing initial test scenarios",
			required = true)
	private File scenarioTestPath = null;

	@Option(name = "-stopping_condition",
			usage = "Stopping condition to apply to the main search:" + 
					"ITERATIONS, TIME, NOSYNTHESIS, MAXWITHOUTSYNTHESIS. Default: NOSYNTHESIS",
			required = true)
	private StoppingConditionType stoppingCondition = StoppingConditionType.NOSYNTHESIS;

	@Option(name = "-stopping_condition_value",
			usage = "Value to be applied to the chosen stopping condition.")
	private int stoppingConditionValue = 0;
	
	@Option(name = "-synthesis_budget",
			usage = "Search budget for equivalent sequence synthesis. Default: 180s")
	private int synthesisBudget = 180;

	@Option(name = "-time_measure",
			usage = "How to measure the elapsed time: CPUTIME, GLOBALTIME",
			handler = LevelHandler.class)
	private TimeMeasure timeMeasure = TimeMeasure.GLOBALTIME;
	
	@Option(name = "-verbose",
			usage = "Verbose output for EvoSuite execution. Default: false")
	private boolean verbose = false;
	
	private Options() {}
	
	
	// check consistency of the options
	public void checkConsistency() throws CmdLineException {
		if (methodSignature == null && classSignature == null) {
			throw new CmdLineException(null, "Select either an input method (-target_method) or an input class (-target_class)");
		}
	}
	
	
	public String getClassesPath() {
		return classesPath;
	}

	public int getCounterexampleBudget() {
		return counterexampleBudget;
	}

	public String getEvosuitePath() {
		return evosuitePath;
	}

	public String getJavaPath() {
		return javaPath;
	}
	
	public String getJunitPath() {
		return junitPath;
	}
	
	public int getLocalSearchRate() {
		return local_search_rate;
	}

	public int getMethodBloatFactor() {
		return methodBloatFactor;
	}

	public int getSearchBudget() {
		return synthesisBudget;
	}
	
	public StoppingConditionType getStoppingCondition() {
		return stoppingCondition;
	}
	
	public int getStoppingConditionValue() {
		return stoppingConditionValue;
	}

	public String getTargetClass() {
		return classSignature;
	}

	public String getTargetMethod() {
		return methodSignature;
	}

	public File getTestScenarioPath() {
		return scenarioTestPath;
	}

	public int getTestSearchBudget() {
		return scenarioBudget;
	}

	public TimeMeasure getTimeMeasure() {
		return timeMeasure;
	}
	
	public boolean isHeuristicPruningScenarios() {
		return heuristicPruningScenarios;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public void setClassesPath(String classesPath) {
		this.classesPath = classesPath;
	}
	
	public void setLogLevel(Level logLevel) {
		Logger.setLevel(logLevel);
	}
	
	public void setHeuristicPruningScenarios(boolean heuristicPruningScenarios) {
		this.heuristicPruningScenarios = heuristicPruningScenarios;
	}
	
	public void setScenarioTestPath(File scenarioTestPath) {
		this.scenarioTestPath = scenarioTestPath;
	}

	public void setTargetMethod(String methodSignature) {
		this.methodSignature = methodSignature;
	}

}
