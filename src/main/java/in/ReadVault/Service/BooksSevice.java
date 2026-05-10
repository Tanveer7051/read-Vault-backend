package in.ReadVault.Service;

import com.cloudinary.Cloudinary;
import in.ReadVault.DTO.AddBook;
import in.ReadVault.DTO.BookDTO;
import in.ReadVault.Entity.BookType;
import in.ReadVault.Entity.Book;
import in.ReadVault.Entity.Category;
import in.ReadVault.GlobalExceptionHandling.*;
import in.ReadVault.Repository.BookRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class BooksSevice {

    private final BookRepository bookRepository;
    private final Cloudinary cloudinary;
    private final ModelMapper modelMapper;

    public BooksSevice(BookRepository bookRepository, Cloudinary cloudinary, ModelMapper modelMapper) {
        this.bookRepository = bookRepository;
        this.cloudinary = cloudinary;
        this.modelMapper = modelMapper;
    }

    public BookDTO addBook(AddBook addBook, MultipartFile pdfFile, MultipartFile imgUrl) {

        try {

            if (addBook.getBookType() == null) {
                throw new BookNotFoundException("Book type is required");
            }

            String title = addBook.getTitle() == null
                    ? null
                    : addBook.getTitle().trim().toUpperCase();

            String author = addBook.getAuthor() == null
                    ? null
                    : addBook.getAuthor().trim().toUpperCase();

            if (bookRepository.findByTitleAndAuthor(title, author).isPresent()) {
                throw new BookAlreadyExistException(
                        "Book already exists with title " + title + " and author " + author
                );
            }

            // Validate image
            if (imgUrl == null || imgUrl.isEmpty()) {
                throw new BadRequestExceptions("Book image is required");
            }

            if (imgUrl.getContentType() == null ||
                    !imgUrl.getContentType().startsWith("image/")) {

                throw new BadRequestExceptions("Only image files are allowed");
            }

            // Upload image
            Map<String, Object> imageUpload = cloudinary.uploader().upload(
                    imgUrl.getBytes(),
                    Map.of("resource_type", "image")
            );

            String imageUrl = imageUpload.get("secure_url").toString();
            String publicImgUrl = imageUpload.get("public_id").toString();

            Book book = new Book();

            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(addBook.getCategory());
            book.setDescription(addBook.getDescription());

            book.setImgUrl(imageUrl);
            book.setImagePublicId(publicImgUrl);

            // DIGITAL BOOK
            if (addBook.getBookType() == BookType.DIGITAL) {

                if (addBook.getTotalCopies() > 0) {
                    throw new DigitalBookDoesNotHaveCopiesException(
                            "Digital books should not have copies"
                    );
                }

                if (pdfFile == null || pdfFile.isEmpty()) {
                    throw new BadRequestExceptions(
                            "PDF file is required for digital book"
                    );
                }

                if (pdfFile.getContentType() == null ||
                        !pdfFile.getContentType().equals("application/pdf")) {

                    throw new BadRequestExceptions(
                            "Only PDF files are allowed"
                    );
                }

                // Upload PDF
                Map<String, Object> pdfUpload = cloudinary.uploader().upload(
                        pdfFile.getBytes(),
                        Map.of("resource_type", "raw")
                );

                String pdfUrl = pdfUpload.get("secure_url").toString();
                String publicPdfUrl = pdfUpload.get("public_id").toString();

                book.setBookType(BookType.DIGITAL);
                book.setPdfurl(pdfUrl);
                book.setPdfPublicId(publicPdfUrl);

                // Digital books don't use copies
                book.setTotalCopies(0);
                book.setAvailableCopies(0);
            }

            // PHYSICAL BOOK
            else if (addBook.getBookType() == BookType.PHYSICAL) {

                if (addBook.getTotalCopies() <= 0) {
                    throw new BadRequestExceptions(
                            "Total copies must be greater than zero"
                    );
                }

                if (pdfFile != null && !pdfFile.isEmpty()) {
                    throw new PhysicalBookDoesNotHaveDigitalFile(
                            "Physical books should not have PDF file"
                    );
                }

                book.setBookType(BookType.PHYSICAL);

                book.setTotalCopies(addBook.getTotalCopies());
                book.setAvailableCopies(addBook.getTotalCopies());
            }

            Book savedBook = bookRepository.save(book);

            BookDTO dto = new BookDTO();

            dto.setId(savedBook.getId());
            dto.setTitle(savedBook.getTitle());
            dto.setAuthor(savedBook.getAuthor());
            dto.setCategory(savedBook.getCategory());
            dto.setDescription(savedBook.getDescription());

            dto.setAvailableCopies(savedBook.getAvailableCopies());
            dto.setTotalCopies(savedBook.getTotalCopies());

            return dto;

        } catch (BookAlreadyExistException |
                 DigitalBookDoesNotHaveCopiesException |
                 PhysicalBookDoesNotHaveDigitalFile |
                 BadRequestExceptions e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while adding book: " + e.getMessage()
            );
        }
    }
    public List<BookDTO> getAll() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

    public BookDTO getBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book Not Found With This ID : " + bookId));
        return modelMapper.map(book, BookDTO.class);
    }

    public String deleteBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found"));

        if (!book.getBorrowRecords().isEmpty()) {
            throw new BadRequestExceptions("Cannot delete book. It is currently borrowed.");
        }
        if (!book.getReservations().isEmpty()) {
            throw new BadRequestExceptions("Cannot delete book. It has active reservations.");
        }
        try{
            if(book.getImagePublicId() != null && !book.getImagePublicId().isEmpty()){
                cloudinary.uploader().destroy(book.getImagePublicId(),
                        Map.of("resource_type","image"));
            }

            if(book.getPdfurl() != null && !book.getPdfurl().isEmpty()){
                cloudinary.uploader().destroy(book.getPdfPublicId(),
                        Map.of("resource_type","raw"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        bookRepository.delete(book);

        return "Book successfully deleted!";
    }

    public List<BookDTO> getBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAll();
        }
        if (bookRepository.findByTitleContainingIgnoreCase(title).isEmpty()) {
            throw new BookNotFoundException("Please Try to check another title, currently we don't have the book with " + title);
        }
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        return books.stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

    public List<BookDTO> getBooksByCategory(String category) {

        if (category == null || category.trim().isEmpty()) {
            return getAll();
        }

        String input = category.trim().toLowerCase();

        List<Category> matchedCategories = Arrays.stream(Category.values())
                .filter(cat -> cat.name().toLowerCase().contains(input))
                .toList();

        if (matchedCategories.isEmpty()) {
            throw new BookNotFoundException("No matching category for: " + category);
        }

        List<Book> books = bookRepository.findByCategoryIn(matchedCategories);

        if (books.isEmpty()) {
            throw new BookNotFoundException("No books found for category: " + category);
        }

        return books.stream()
                .map(book -> modelMapper.map(book, BookDTO.class))
                .toList();
    }

}
