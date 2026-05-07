package in.ReadVault.GlobalExceptionHandling;

public class DigitalBookDoesNotHaveCopiesException extends RuntimeException {
    public DigitalBookDoesNotHaveCopiesException(String message) {
        super(message);
    }
}
