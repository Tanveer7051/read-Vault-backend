package in.ReadVault.GlobalExceptionHandling;

public class UnauthorizedExceptions extends RuntimeException {
    public UnauthorizedExceptions(String message) {
        super(message);
    }
}
