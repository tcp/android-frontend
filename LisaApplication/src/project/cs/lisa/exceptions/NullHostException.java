package project.cs.lisa.exceptions;

/**
 * (Generic) Invalid host name failure exception.
 * @author Thiago Costa Porto
 */
public class NullHostException extends Exception {
    public NullHostException() {
        super("Host name may not be null.");
    }
}
