package project.cs.lisa.exceptions;

public class NullPortException extends Exception{
    public NullPortException() {
        super("Port may not be null.");
    }
}
