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

@RestController
@RequestMapping("/api/book")
public class BooksController {

    private final BooksSevice booksSevice;
    private final ModelMapper modelMapper;

    public BooksController(BooksSevice booksSevice, ModelMapper modelMapper) {
        this.booksSevice = booksSevice;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/addbook")
    public ResponseEntity<BookDTO> addBook(@Valid @ModelAttribute AddBook addBook,
                                           @RequestParam(value = "imageFile") MultipartFile imageFile,
                                           @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(booksSevice.addBook(addBook, file, imageFile));
    }
//    @PutMapping("/{id}")
//    public ResponseEntity<BookDTO> updateBook(
//            @PathVariable Long id,
//            @RequestBody BookDTO bookDTO) {
//
//        return ResponseEntity.ok(bookService.updateBook(id, bookDTO));
//    }

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