package sbes.execution.jbse;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.apps.settings.ParseException;
import jbse.apps.settings.SettingsReader;

public class JBSE {

	//Classpath and sourcepath for code under analysis
	public static final String					HOME								= "./";
	public static final String					CLASSPATH_JRE						= HOME + "data/jre/rt.jar";
	public static final String					CLASSPATH_TARGET					= HOME + "bin/";
	public static final String					SOURCEPATH_JRE						= HOME + "data/jre/src.zip";
	public static final String					SOURCEPATH_TARGET					= HOME + "subjects/";

	//Full classpath and sourcepath
	public static final String[] 				CLASSPATH	= { CLASSPATH_JRE,  CLASSPATH_TARGET };
	public static final String[] 				SOURCEPATH	= { SOURCEPATH_JRE, SOURCEPATH_TARGET };

	//Input/output directories
	public static final String   				SETTINGS_FILES_PATH					= HOME + "jbse_hex/";
	public static final String 					SETTINGS_FILENAME 					= SETTINGS_FILES_PATH + "stack_hex.jbse";
	public static final String   				OUTPUT_PATH							= HOME + "out/";

	public static final String					SICSTUS_PATH						= "/usr/local/bin/";
	public static final String					Z3_PATH								= "/Users/andrea/bin/bin/";
	public static final DecisionProcedureType	DECISION_PROCEDURE_TYPE				= DecisionProcedureType.Z3;
	public static final String					DECISION_PROCEDURE_PATH				= Z3_PATH;

	public static final boolean 				USE_CONSERVATIVE_REP_OKS			= true;

	//What to show on the log files
	public static final StepShowMode			STEP_SHOW_MODE                      = StepShowMode.LEAVES;
	public static final StateFormatMode			STATE_FORMAT_MODE                   = StateFormatMode.JUNIT_TEST;
	public static final boolean					SHOW_SAFE                           = false;
	public static final boolean					SHOW_UNSAFE                         = true;
	public static final boolean					SHOW_OUT_OF_SCOPE                   = false;
	public static final boolean					SHOW_CONTRADICTORY                  = false;
	public static final boolean					SHOW_WARNINGS                       = false;
	public static final boolean					SHOW_DECISION_PROCEDURE_INTERACTION = false;

	//Concretization
	public static final boolean					DO_CONCRETIZATION					= false;

	public static final long                  	TIMEOUT 							= 2;
	public static final TimeUnit              	TIMEOUT_TIME_UNIT                   = TimeUnit.HOURS;
	public static final int                   	DEPTH_SCOPE                         = 500;
	public static final int                   	COUNT_SCOPE							= 2000;
	public static final int                   	CONCR_DEPTH_SCOPE                   = 1000;
	public static final int                   	CONCR_COUNT_SCOPE                   = 10000;

	public static final String   			  	NODE_CLASS_DLL        		      	= "doubly_linked_list/DoubleLinkedList_LICS$Entry";
	public static final int                   	HEAP_SCOPE                          = 8;



	public static RunParameters getRunParameters(String[] methodSignature) {
		RunParameters p = new RunParameters();
		try {
			new SettingsReader(SETTINGS_FILENAME).fillRunParameters(p);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: settings file " + SETTINGS_FILENAME + " not found.");
			return p;
		} catch (ParseException e) {
			System.err.println("ERROR: settings file " + SETTINGS_FILENAME + " ill-formed. " + e.getMessage());
			return p;
		}
		p.addClasspath(CLASSPATH);
		p.addSourcePath(SOURCEPATH);
		p.setMethodSignature(methodSignature[0], methodSignature[2], methodSignature[1]);

		// use conservative rep oks
		p.setUseConservativeRepOks(USE_CONSERVATIVE_REP_OKS);

		// decision procedure
		p.setDecisionProcedureType(DECISION_PROCEDURE_TYPE);
		p.setExternalDecisionProcedurePath(DECISION_PROCEDURE_PATH);

		// concretization
		p.setDoConcretization(DO_CONCRETIZATION);

		// what to show
		p.setStepShowMode(STEP_SHOW_MODE);
		p.setStateFormatMode(STATE_FORMAT_MODE);
		p.setShowSafe(SHOW_SAFE);
		p.setShowUnsafe(SHOW_UNSAFE);
		p.setShowOutOfScope(SHOW_OUT_OF_SCOPE);
		p.setShowContradictory(SHOW_CONTRADICTORY);
		p.setShowWarnings(SHOW_WARNINGS);
		p.setShowDecisionProcedureInteraction(SHOW_DECISION_PROCEDURE_INTERACTION);

		//output file
		p.setOutputFileName(OUTPUT_PATH + "TestSuite_" + methodSignature[1] + ".java");

		// scope
		p.setTimeout(TIMEOUT, TIMEOUT_TIME_UNIT);
		p.setHeapScope(NODE_CLASS_DLL, HEAP_SCOPE);
		p.setDepthScope(DEPTH_SCOPE);
		p.setCountScope(COUNT_SCOPE);
		p.setConcretizationDepthScope(CONCR_DEPTH_SCOPE);
		p.setConcretizationCountScope(CONCR_COUNT_SCOPE);

		return p;
	}

	public void runAnalysis(String[] testMethodSignature) {
		final RunParameters p = getRunParameters(testMethodSignature);
		final Run r = new Run(p);
		r.run();
	}

}
