package sbes.execution;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import sbes.execution.evosuite.Evosuite;
import sbes.logging.Logger;
import sbes.util.DirectoryUtils;

public class Worker implements Callable<ExecutionResult> {

	private static final Logger logger = new Logger(Worker.class);

	private final Evosuite evosuite;
	private int exitStatus;
	private ByteArrayOutputStream errStream = null;
	private ByteArrayOutputStream outStream = null;

	public Worker(final Evosuite evosuite) {
		this.evosuite = evosuite;
	}

	@Override
	public ExecutionResult call() {
		ExecutionResult result = null;

		try {
			ProcessBuilder pb = new ProcessBuilder(evosuite.getCommand());
			pb = pb.directory(new File(DirectoryUtils.I().getBaseDirectory()));

			logger.debug("Going to execute: " + evosuite.toString());

			Process process = pb.start();
			this.errStream = new ByteArrayOutputStream();
			this.outStream = new ByteArrayOutputStream();
			Thread errThread = new StreamRedirectThread(Thread.currentThread().getName() + "-error-reader", process.getErrorStream(), this.errStream);
			Thread outThread = new StreamRedirectThread(Thread.currentThread().getName() + "-output-reader", process.getInputStream(), this.outStream);
			errThread.start();
			outThread.start();

			this.exitStatus = process.waitFor();
			errThread.interrupt();
			outThread.interrupt();

			result = new ExecutionResult(evosuite.getTestDirectory());
			result.setCommand(evosuite.getCommand());
			result.setStdout(getOutStream());
			result.setStderr(getErrStream());
			result.setFilename(evosuite.getTestFilename());
			result.setExitStatus(this.exitStatus);
		}
		catch (IOException | InterruptedException e) {
			logger.error("Unable to execute command", e);
		}

		return result;
	}

	public int getExitStatus() {
		return this.exitStatus;
	}

	public String getErrStream() {
		return this.errStream.toString();
	}

	public String getOutStream() {
		return this.outStream.toString();
	}

	public Evosuite getCommand() {
		return this.evosuite;
	}

}
