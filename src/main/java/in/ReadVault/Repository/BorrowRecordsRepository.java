package in.ReadVault.Repository;

import in.ReadVault.Entity.Book;
import in.ReadVault.Entity.BorrowRecords;
import in.ReadVault.Entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowRecordsRepository extends JpaRepository<BorrowRecords, Long> {

    boolean existsByBookAndStatus(Book book, BorrowStatus status);

    Optional<BorrowRecords> findByIdAndUserIdAndStatus(
            Long userId, Long bookId, BorrowStatus status);

    List<BorrowRecords> findByUserId(Long userId);

    List<BorrowRecords> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowStatus status);
    long countByUserIdAndBookIdAndStatus(Long userId, Long bookId,BorrowStatus status);
}