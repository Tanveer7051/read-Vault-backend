package in.ReadVault.DTO;

import in.ReadVault.Entity.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDTO {

        @NotBlank(message = "First name is required")
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Enter valid first name")
        private String firstname;

        @NotBlank(message = "Username is required")
        private String username;

        @Pattern(regexp = "^[A-Za-z]+$", message = "Enter valid last name")
        private String lastname;

        @NotBlank(message = "Email is required")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
                message = "Only Gmail emails are allowed"
        )
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,}$",
                message = "Password must contain number and special character"
        )
//        @Pattern(
//                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,15}$",
//                message = "Password must contain:\n" +
//                        "- At least one uppercase letter\n" +
//                        "- At least one lowercase letter\n" +
//                        "- At least one number\n" +
//                        "- At least one special character\n" +
//                        "- Length between 8 and 15 characters"
//        )
        private String password;

        private Role role;
}