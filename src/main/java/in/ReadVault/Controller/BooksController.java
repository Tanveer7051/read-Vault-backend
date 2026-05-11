package in.ReadVault.Controller;

import in.ReadVault.DTO.AddBook;
import in.ReadVault.DTO.BookDTO;
import in.ReadVault.Service.BooksSevice;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BooksController {

    private final BooksSevice booksSevice;

    public BooksController(BooksSevice booksSevice, ModelMapper modelMapper) {
        this.booksSevice = booksSevice;
    }

    @PostMapping("/addbook")
    public ResponseEntity<BookDTO> addBook(@Valid @ModelAttribute AddBook addBook,
                                           @RequestParam(value = "imageFile") MultipartFile imageFile,
                                           @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(booksSevice.addBook(addBook, file, imageFile));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBook(

            @PathVariable Long id,

            @ModelAttribute AddBook addBook,

            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile,

            @RequestParam(value = "pdfFile", required = false)
            MultipartFile pdfFile
    ) {

        BookDTO updatedBook = booksSevice.updateBook(
                id,
                addBook,
                imageFile,
                pdfFile
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Book updated successfully",
                        "data", updatedBook
                )
        );
    }

    @GetMapping("/all")
    public List<BookDTO> getAll() {
        return booksSevice.getAll();
    }

    @GetMapping("/{bookId}")
    public BookDTO getBook(@PathVariable Long bookId) {
        return booksSevice.getBook(bookId);

    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable Long bookId) {
        return ResponseEntity.status(HttpStatus.OK).body(booksSevice.deleteBook(bookId));
    }

    @GetMapping("/search")
    public List<BookDTO> getBooksByTitle(@RequestParam(required = false) String title) {
        return booksSevice.getBooksByTitle(title);
    }

    @GetMapping("/category")
    public List<BookDTO> getBooksByCategory(@RequestParam(required = false) String category) {
        return booksSevice.getBooksByCategory(category);
    }
}