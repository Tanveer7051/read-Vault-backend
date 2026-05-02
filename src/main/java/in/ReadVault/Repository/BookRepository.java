package in.ReadVault.Repository;

import in.ReadVault.Entity.Book;
import in.ReadVault.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitleAndAuthor(String title, String author);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByCategoryIn(List<Category> category);
    List<Book> findByTitle(String title);
}
