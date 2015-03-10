package sbes.exceptions;

public class GenerationException extends RuntimeException {

	private static final long serialVersionUID = 6581122057199703982L;

	public GenerationException(final String arg0) {
		super(arg0);
	}

	public GenerationException(final Throwable arg0) {
		super(arg0);
	}

	public GenerationException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
