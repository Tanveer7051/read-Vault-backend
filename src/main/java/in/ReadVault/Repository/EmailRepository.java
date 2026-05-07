package in.ReadVault.Repository;

import in.ReadVault.Entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailVerification,Long> {
    Optional<EmailVerification> findByEmail(String email);

    List<EmailVerification> findByExpiryTimeBefore(LocalDateTime now);
}
