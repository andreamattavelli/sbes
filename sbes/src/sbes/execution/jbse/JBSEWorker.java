package sbes.execution.jbse;

import sbes.execution.ExecutionResult;
import sbes.execution.Tool;
import sbes.execution.Worker;
import sbes.logging.Logger;

public class JBSEWorker implements Worker {

	private static final Logger logger = new Logger(JBSEWorker.class);

	private final JBSE jbse;
	private int exitStatus;

	public JBSEWorker(JBSE jbse) {
		this.jbse = jbse;
	}

	@Override
	public ExecutionResult call() {
		ExecutionResult result = null;

		try {
			logger.debug("Going to execute: " + jbse.toString());
			
			jbse.runAnalysis();

			exitStatus = 0;

			result = new ExecutionResult(jbse.getTestDirectory());
			result.setCommand(jbse.getCommand());
			result.setStdout("");
			result.setStderr("");
			result.setFilename(jbse.getTestFilename());
			result.setExitStatus(exitStatus);
		}
		catch (Throwable e) {
			logger.error("Unable to execute command", e);
		}

		return result;
	}

	public int getExitStatus() {
		return exitStatus;
	}

	public Tool getCommand() {
		return jbse;
	}

}
