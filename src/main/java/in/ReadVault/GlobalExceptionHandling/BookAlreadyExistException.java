package in.ReadVault.GlobalExceptionHandling;

public class BookAlreadyExistException extends RuntimeException{
    public BookAlreadyExistException(String msg){
        super(msg);
    }
}
