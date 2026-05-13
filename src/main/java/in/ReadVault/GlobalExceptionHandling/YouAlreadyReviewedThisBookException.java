package in.ReadVault.GlobalExceptionHandling;

public class YouAlreadyReviewedThisBookException extends RuntimeException {
    public YouAlreadyReviewedThisBookException(String message) {
        super(message);
    }
}
