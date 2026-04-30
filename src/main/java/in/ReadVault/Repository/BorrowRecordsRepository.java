package in.ReadVault.Repository;

import in.ReadVault.Entity.BorrowRecords;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRecordsRepository extends JpaRepository<BorrowRecords, Long> {

}