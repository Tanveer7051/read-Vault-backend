package in.ReadVault.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {

    private Long id;

    private Integer rating;

    private String comment;

    private String userName;

    private Long bookId;

    private LocalDateTime createdAt;
}