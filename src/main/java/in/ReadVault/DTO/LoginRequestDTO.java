package in.ReadVault.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @Email(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",message = "Only Gmail emails are allowed")
    @NotBlank(message = "Please Provide Email")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$",
            message = "Password must contain at least one number and one special character"
    )
    private String password;
}
