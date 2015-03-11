package sbes.exceptions;

public class CompilationException extends RuntimeException {

	private static final long serialVersionUID = 4638443038569302025L;
	
	public CompilationException(final String arg0) {
		super(arg0);
	}

	public CompilationException(final Throwable arg0) {
		super(arg0);
	}

	public CompilationException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}
	
}
