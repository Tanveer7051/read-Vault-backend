package in.ReadVault.GlobalExceptionHandling;


public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String  msg){
        super(msg);
    }
}
