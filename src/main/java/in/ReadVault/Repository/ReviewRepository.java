package in.ReadVault.Repository;

import in.ReadVault.Entity.Book;
import in.ReadVault.Entity.Review;
import in.ReadVault.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    Optional<Review> findByUserAndBook(User user, Book book);

    boolean existsByUserAndBook(User user, Book book);
}