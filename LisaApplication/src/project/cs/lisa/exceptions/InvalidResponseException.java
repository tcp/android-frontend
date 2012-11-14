package project.cs.lisa.exceptions;

/**
 * Thrown when the response to a NetInf message can't be parsed.
 * @author Linus Sunde
 *
 */
public class InvalidResponseException extends Exception {
    /**
     * Constructs a InvalidResponseException with the specified detail message.
     * @param message   the detail message.
     */
    public InvalidResponseException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message   the detail message
     * @param cause     the cause
     */
    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }   
}
