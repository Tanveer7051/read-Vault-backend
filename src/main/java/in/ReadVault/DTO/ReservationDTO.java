package in.ReadVault.DTO;

import in.ReadVault.Entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {

    private Long id;

    private Long userId;
    private String userName;

    private Long bookId;
    private String bookTitle;

    private LocalDate createdDate;
    private LocalDate expiryDate;

    private ReservationStatus status;
}