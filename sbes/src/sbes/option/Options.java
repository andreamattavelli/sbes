package sbes.option;

import java.io.File;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import sbes.exceptions.SBESException;
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
	
	@Option(name = "-counterexample_budget",
			usage = "Search budget for counterexample synthesis. Default: 180s")
	private int budgetCounterexample = 180;

	@Option(name = "-scenario_budget",
			usage = "Search budget for test case generation. Default: 30s")
	private int budgetScenarios = 30;

	@Option(name = "-synthesis_budget",
			usage = "Search budget for equivalent sequence synthesis. Default: 180s")
	private int budgetSynthesis = 180;

	@Option(name = "-classes",
			usage = "Path to classes",
			required = true)
	private String classesPath;
	
	@Option(name = "-dont_resolve_generics",
			usage = "Force SBES to use Object instead of resolving generic types")
	private boolean dontResolveGenerics = false;
	
	@Option(name = "-evosuite",
			usage = "Path to EvoSuite jar (included jar name). Default: \"./evosuite.jar\"")
	private String evosuitePath = "./evosuite.jar";
	
	@Option(name = "-prune_scenarios",
			usage = "Heuristically pruning initial test scenarios to one")
	private boolean heuristicPruningScenarios = true;

	@Option(name = "-hex",
			usage = "Path to HEX files")
	private String hexPath;
	
	@Option(name = "-java7",
			usage = "Path to Java 7 executable")
	private String java7Path = "";

	@Option(name = "-jbse",
			usage = "Path to JBSE jar (included jar name). Default: \"./lib/jbse-0.7.jar\"")
	private String jbsePath = "./lib/jbse-0.7.jar";
	
	@Option(name = "-jbse_jre",
			usage = "Path to JBSE  JRE jar (included jar name).")
	private String jbseJREPath = null;
	
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

	@Option(name = "-norefinement",
			usage = "Do not refine results when a counterexample is found")
	private boolean noRefinementWhenSpurious = false;
	
	@Option(name = "-sbes_lib",
			usage = "Path to sbes-lib.jar. Default: ./sbes-lib.jar")
	private String sbeslibPath = "./sbes-lib.jar";

	@Option(name = "-test_scenario",
			usage = "Path to Java source file containing initial test scenarios",
			required = true)
	private File scenarioTestPath = null;

	@Option(name = "-stopping_condition",
			usage = "Stopping condition to apply to the main search. Default: NOSYNTHESIS",
			required = true)
	private StoppingConditionType stoppingCondition = StoppingConditionType.NOSYNTHESIS;
	
	@Option(name = "-stopping_condition_value",
			usage = "Value to be applied to the chosen stopping condition.")
	private int stoppingConditionValue = 0;
	
	@Option(name = "-symbolic_counterexample",
			usage = "Generate counterexample with symbolic execution. Default: false")
	private boolean symbolicExecutionCounterexample = false;

	@Option(name = "-target_class",
			usage = "Java class signature under investigation")
	private String targetClassSignature = null;
	
	@Option(name = "-time_measure",
			usage = "How to measure the elapsed time: CPUTIME, GLOBALTIME",
			handler = TimeMeasureHandler.class)
	private TimeMeasure timeMeasure = TimeMeasure.GLOBALTIME;
	
	@Option(name = "-verbose",
			usage = "Verbose output for EvoSuite execution. Default: false")
	private boolean verbose = false;
	
	@Option(name = "-z3",
			usage = "Path to Z3 binary")
	private String z3Path;
	
	private Options() {}
	
	
	// check consistency of the options
	public void checkConsistency() throws CmdLineException {
		if (methodSignature == null && targetClassSignature == null) {
			throw new SBESException("Select either an input method (-target_method) or an input class (-target_class)");
		}
	}
	
	
	public boolean dontResolveGenerics() {
		return dontResolveGenerics;
	}

	public String getClassesPath() {
		return classesPath;
	}

	public int getCounterexampleBudget() {
		return budgetCounterexample;
	}
	
	public String getEvosuitePath() {
		return evosuitePath;
	}

	public String getHexPath() {
		return hexPath;
	}
	
	public String getJavaPath() {
		return java7Path;
	}
	
	public String getJbsePath() {
		return jbsePath;
	}

	public String getJbseJREPath() {
		return jbseJREPath;
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

	public String getSbesLibPath() {
		return sbeslibPath;
	}
	
	public int getSearchBudget() {
		return budgetSynthesis;
	}
	
	public StoppingConditionType getStoppingCondition() {
		return stoppingCondition;
	}
	
	public int getStoppingConditionValue() {
		return stoppingConditionValue;
	}

	public String getTargetClass() {
		return targetClassSignature;
	}

	public String getTargetMethod() {
		return methodSignature;
	}

	public File getTestScenarioPath() {
		return scenarioTestPath;
	}

	public int getTestSearchBudget() {
		return budgetScenarios;
	}

	public TimeMeasure getTimeMeasure() {
		return timeMeasure;
	}
	
	public String getZ3Path() {
		return z3Path;
	}
	
	public boolean isSymbolicExecutionCounterexample() {
		return symbolicExecutionCounterexample;
	}
	
	public boolean isGiveUpWhenSpurious() {
		return noRefinementWhenSpurious;
	}
	
	public boolean isHeuristicPruningScenarios() {
		return heuristicPruningScenarios;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public void setClassesPath(final String classesPath) {
		this.classesPath = classesPath;
	}
	
	public void setDontResolveGenerics(boolean resolveGenerics) {
		this.dontResolveGenerics = resolveGenerics;
	}
	
	public void setHeuristicPruningScenarios(final boolean heuristicPruningScenarios) {
		this.heuristicPruningScenarios = heuristicPruningScenarios;
	}
	
	public void setLogLevel(final Level logLevel) {
		Logger.setLevel(logLevel);
	}
	
	public void setScenarioTestPath(final File scenarioTestPath) {
		this.scenarioTestPath = scenarioTestPath;
	}
	
	public void setSymbolicExecutionCounterexample(boolean symbexeCounterexample) {
		this.symbolicExecutionCounterexample = symbexeCounterexample;
	}

	public void setTargetMethod(final String methodSignature) {
		this.methodSignature = methodSignature;
	}

}
