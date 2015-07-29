package sbes.exceptions;

public class CloningException extends RuntimeException {

    private static final long serialVersionUID = 3815175312001146867L;

    public CloningException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
}
