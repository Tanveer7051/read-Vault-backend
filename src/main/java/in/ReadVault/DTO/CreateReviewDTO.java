package in.ReadVault.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewDTO {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    @NotBlank(message = "Comment is required")
    @Size(min = 5, max = 300, message = "Comment must be between 5 and 300 characters")
    private String comment;
}