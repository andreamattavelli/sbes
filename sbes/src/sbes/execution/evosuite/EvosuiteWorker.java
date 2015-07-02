package sbes.execution.evosuite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import sbes.execution.ExecutionResult;
import sbes.execution.StreamRedirectThread;
import sbes.execution.Worker;
import sbes.logging.Logger;
import sbes.util.DirectoryUtils;

public class EvosuiteWorker implements Worker {

	private static final Logger logger = new Logger(EvosuiteWorker.class);

	private final Evosuite evosuite;
	private int exitStatus;
	private ByteArrayOutputStream errStream = null;
	private ByteArrayOutputStream outStream = null;

	public EvosuiteWorker(Evosuite evosuite) {
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
