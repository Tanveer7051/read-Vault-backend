package in.ReadVault.Repository;

import in.ReadVault.Entity.ReservationStatus;
import in.ReadVault.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, ReservationStatus status);

    long countByBookIdAndStatus(Long bookId, ReservationStatus status);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByBookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);
}
