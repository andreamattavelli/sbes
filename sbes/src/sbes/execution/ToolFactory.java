package sbes.execution;

import sbes.execution.evosuite.EvosuiteSecondStage;
import sbes.execution.jbse.JBSE;
import sbes.option.Options;

public class ToolFactory {

	private ToolFactory() { } // Do not instantiate me!

	public static Tool getTool(String classSignature, String methodSignature, String additionalClasspath) {
		Tool tool;
		if (Options.I().isCounterexampleWithSymbolicExecution()) {
			tool = new JBSE(classSignature, methodSignature, additionalClasspath);
		} else {
			tool = new EvosuiteSecondStage(classSignature, methodSignature, additionalClasspath);
		}
		return tool;
	}
	
}
