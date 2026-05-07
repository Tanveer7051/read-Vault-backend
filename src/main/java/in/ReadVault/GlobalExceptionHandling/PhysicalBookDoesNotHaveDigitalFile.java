package in.ReadVault.GlobalExceptionHandling;

public class PhysicalBookDoesNotHaveDigitalFile extends RuntimeException {
  public PhysicalBookDoesNotHaveDigitalFile(String message) {
    super(message);
  }
}
