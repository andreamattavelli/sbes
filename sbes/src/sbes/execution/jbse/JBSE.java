package sbes.execution.jbse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jbse.apps.run.Run;
import jbse.apps.run.RunParameters;
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StateFormatMode;
import jbse.apps.run.RunParameters.StepShowMode;
import jbse.apps.settings.ParseException;
import jbse.apps.settings.SettingsReader;
import sbes.exceptions.GenerationException;
import sbes.execution.Tool;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.util.DirectoryUtils;

public class JBSE extends Tool {

	private static final Logger logger = new Logger(JBSE.class);
	
	private String additionalClasspath;
	
	public JBSE(String classSignature, String methodSignature, String additionalClasspath) {
		super(classSignature, methodSignature);
		this.additionalClasspath = additionalClasspath;
	}
	
	public void runAnalysis() {
		String[] methodSignature = new String[] { classSignature.replace('.', '/'), "method_under_test", "()V" };
		final RunParameters p = new RunParameters();
		try {
			new SettingsReader(Options.I().getHexPath()).fillRunParameters(p);
		} catch (FileNotFoundException e) {
			logger.error("ERROR: settings file " + Options.I().getHexPath() + " not found.", e);
		} catch (ParseException e) {
			logger.error("ERROR: settings file " + Options.I().getHexPath() + " ill-formed. " + e.getMessage(), e);
		}
		List<String> classy = new ArrayList<String>();
		String CLASSPATH_JRE = Options.I().getJbseJREPath();
		classy.add(CLASSPATH_JRE);
		for (String string : additionalClasspath.split(":")) {
			classy.add(string);
		}
		p.addClasspath(classy.toArray(new String[0]));
		p.setMethodSignature(methodSignature[0], methodSignature[2], methodSignature[1]);

		// use conservative rep oks
		p.setUseConservativeRepOks(true);

		// decision procedure
		p.setDecisionProcedureType(DecisionProcedureType.Z3);
		p.setExternalDecisionProcedurePath(Options.I().getZ3Path());

		// concretization
		p.setDoConcretization(false);

		// what to show
		p.setStepShowMode(StepShowMode.LEAVES);
		p.setStateFormatMode(new StateFormatMode[] { StateFormatMode.JUNIT_TEST });
		p.setShowSafe(false);
		p.setShowUnsafe(true);
		p.setShowOutOfScope(false);
		p.setShowContradictory(false);
		p.setShowWarnings(true);
		p.setShowDecisionProcedureInteraction(false);

		//output file
		p.setOutputPath(DirectoryUtils.I().getSecondStubJBSEDir());

		// scope
		p.setTimeout(Options.I().getCounterexampleBudget(), TimeUnit.SECONDS);
		p.setHeapScope("sbes/symbolic/mock/DoubleLinkedList$Entry", 8);
		p.setDepthScope(500);
		p.setCountScope(2000);
		p.setConcretizationDepthScope(1000);
		p.setConcretizationCountScope(10000);
		
		try {
			final Run r = new Run(p);
			r.run();
		} catch (Throwable t) {
			throw new GenerationException("Error during JBSE execution", t);
		}
	}

	@Override
	public String[] getCommand() {
		return new String[0];
	}

	@Override
	public String getTestDirectory() {
		return DirectoryUtils.I().getSecondStubJBSEDir();
	}

	@Override
	public String getTestFilename() {
		return "TestSuite_method_under_test.java";
	}
	
	@Override
	public String toString() {
		return "JBSE on " + classSignature.replace('.', '/') + "method_under_test()V";
	}

}
