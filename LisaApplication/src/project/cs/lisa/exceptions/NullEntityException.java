package project.cs.lisa.exceptions;

/**
 * Thrown when the entity of a HTTP response is null.
 * @author Linus Sunde
 * @author Paolo Boschini
 */
public class NullEntityException extends Exception {
    /**
     * Constructs a NullEntityException.
     */
    public NullEntityException() {
        super();
    }

    /**
     * Constructs a NullEntityException with the specified detail message.
     * @param message   the detail message.
     */
    public NullEntityException(String message) {
        super(message);
    }

    /**
     * Constructs a new NullEntityException with the specified detail message and cause.
     * @param message   the detail message
     * @param cause     the cause
     */
    public NullEntityException(String message, Throwable cause) {
        super(message, cause);
    }
}

