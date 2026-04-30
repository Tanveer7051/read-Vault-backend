package in.ReadVault.DTO;

import in.ReadVault.Entity.BorrowStatus;
import in.ReadVault.Entity.BorrowType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BorrowRecordDTO {

    private Long id;

    private Long userId;
    private String userName;

    private Long bookId;
    private String bookTitle;

    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate digitalExpiry;

    private BorrowType type;
    private BorrowStatus status;

    private int renewalCount;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}