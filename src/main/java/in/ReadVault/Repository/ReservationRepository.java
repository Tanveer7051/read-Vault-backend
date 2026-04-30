package in.ReadVault.Repository;

import in.ReadVault.Entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReservationRepository extends JpaRepository<Reservation,Long> {

}
