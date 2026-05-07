package in.ReadVault.GlobalExceptionHandling;

public class DigitalBookMustHavePdfException extends RuntimeException {
    public DigitalBookMustHavePdfException(String message) {
        super(message);
    }
}
