package sbes.exceptions;

public class SBESException extends RuntimeException {

	private static final long serialVersionUID = 6581122057199703982L;

	public SBESException(String arg0) {
		super(arg0);
	}

	public SBESException(Throwable arg0) {
		super(arg0);
	}

	public SBESException(String arg0, final Throwable arg1) {
		super(arg0, arg1);
	}

}
