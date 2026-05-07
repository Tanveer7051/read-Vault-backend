package in.ReadVault.DTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserDTO {
    @Size(min = 2, max = 30,
            message = "Firstname must be between 2 and 30 characters")
    private String firstname;

    @Size(min = 2, max = 30,
            message = "Lastname must be between 2 and 30 characters")
    private String lastname;

    @Size(min = 4, max = 20,
            message = "Username must be between 4 and 20 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9._]+$",
            message = "Username can only contain letters, numbers, dot and underscore"
    )
    private String username;
}