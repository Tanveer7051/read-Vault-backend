package in.ReadVault.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate digitalExpiry;

    private int renewalCount;

    @Enumerated(EnumType.STRING)
    private BorrowType type;

    @Enumerated(EnumType.STRING)
    private BorrowStatus status;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    @JsonBackReference(value = "user-borrow")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonBackReference(value = "book-borrow")
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}