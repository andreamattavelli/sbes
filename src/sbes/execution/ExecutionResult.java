package sbes.execution;

public class ExecutionResult {

	private final String outputDir;
	private String filename;

	private String[] command;
	private String stdout;
	private String stderr;

	public ExecutionResult(final String outputDir) {
		this.outputDir = outputDir;
	}

	public String getOutputDir() {
		return this.outputDir;
	}

	public String[] getCommand() {
		return this.command;
	}

	public void setCommand(final String[] command) {
		this.command = command;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(final String filename) {
		this.filename = filename;
	}

	public String getStdout() {
		return this.stdout;
	}

	public void setStdout(final String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return this.stderr;
	}

	public void setStderr(final String stderr) {
		this.stderr = stderr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.filename == null) ? 0 : this.filename.hashCode());
		result = prime * result + ((this.outputDir == null) ? 0 : this.outputDir.hashCode());
		return result;
	}
	
}
