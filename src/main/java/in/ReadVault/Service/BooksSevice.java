package in.ReadVault.Service;

import com.cloudinary.Cloudinary;
import in.ReadVault.DTO.AddBook;
import in.ReadVault.DTO.BookDTO;
import in.ReadVault.Entity.BookType;
import in.ReadVault.Entity.Book;
import in.ReadVault.Entity.Category;
import in.ReadVault.Entity.User;
import in.ReadVault.GlobalExceptionHandling.*;
import in.ReadVault.Repository.BookRepository;
import in.ReadVault.Repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    public BooksSevice(BookRepository bookRepository, Cloudinary cloudinary, ModelMapper modelMapper, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.cloudinary = cloudinary;
        this.userRepository = userRepository;
    }

    public BookDTO addBook(AddBook addBook, MultipartFile pdfFile, MultipartFile imgUrl) {

        User authUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User currentAdmin = userRepository.findById(authUser.getId())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

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
            System.out.println(currentAdmin);
            System.out.println(currentAdmin.getId());
            System.out.println(currentAdmin.getUsername());
            book.setPublishedBy(currentAdmin);

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

            dto.setPublishedBy(currentAdmin.getFirstname() + " " + currentAdmin.getLastname());
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

            throw new BadRequestExceptions(
                    "Error while adding book: " + e.getMessage()
            );
        }
    }
    public BookDTO updateBook(
            Long id,
            AddBook addBook,
            MultipartFile imageFile,
            MultipartFile pdfFile
    ) {

        try {

            Book book = bookRepository.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Book not found with id : " + id
                            )
                    );


            if (addBook.getTitle() != null &&
                    !addBook.getTitle().trim().isEmpty()) {

                book.setTitle(
                        addBook.getTitle()
                                .trim()
                                .toUpperCase()
                );
            }

            if (addBook.getAuthor() != null &&
                    !addBook.getAuthor().trim().isEmpty()) {

                book.setAuthor(
                        addBook.getAuthor()
                                .trim()
                                .toUpperCase()
                );
            }

            if (addBook.getCategory() != null) {
                book.setCategory(addBook.getCategory());
            }

            if (addBook.getDescription() != null &&
                    !addBook.getDescription().trim().isEmpty()) {

                book.setDescription(
                        addBook.getDescription()
                );
            }

            if (imageFile != null && !imageFile.isEmpty()) {

                // Delete old image
                if (book.getImagePublicId() != null &&
                        !book.getImagePublicId().isEmpty()) {

                    cloudinary.uploader().destroy(
                            book.getImagePublicId(),
                            Map.of("resource_type", "image")
                    );
                }

                // Upload new image
                Map<String, Object> imageUploadResult =
                        cloudinary.uploader().upload(
                                imageFile.getBytes(),
                                Map.of("resource_type", "image")
                        );

                book.setImgUrl(
                        imageUploadResult.get("secure_url").toString()
                );

                book.setImagePublicId(
                        imageUploadResult.get("public_id").toString()
                );
            }


            if (book.getBookType() == BookType.DIGITAL) {

                // Digital books should not have copies
                book.setTotalCopies(0);
                book.setAvailableCopies(0);

                // Update PDF if new file uploaded
                if (pdfFile != null && !pdfFile.isEmpty()) {

                    // Delete old PDF
                    if (book.getPdfPublicId() != null &&
                            !book.getPdfPublicId().isEmpty()) {

                        cloudinary.uploader().destroy(
                                book.getPdfPublicId(),
                                Map.of("resource_type", "raw")
                        );
                    }

                    // Upload new PDF
                    Map<String, Object> pdfUploadResult =
                            cloudinary.uploader().upload(
                                    pdfFile.getBytes(),
                                    Map.of("resource_type", "raw")
                            );

                    book.setPdfurl(
                            pdfUploadResult.get("secure_url").toString()
                    );

                    book.setPdfPublicId(
                            pdfUploadResult.get("public_id").toString()
                    );
                }
            }


            else if (book.getBookType() == BookType.PHYSICAL) {

                // Physical books cannot have PDF
                if (pdfFile != null && !pdfFile.isEmpty()) {

                    throw new PhysicalBookDoesNotHaveDigitalFile(
                            "Physical books cannot have PDF"
                    );
                }

                // Update copies only if provided
                if (addBook.getTotalCopies() > 0) {

                    int borrowedCopies =
                            book.getTotalCopies()
                                    - book.getAvailableCopies();

                    if (addBook.getTotalCopies() < borrowedCopies) {

                        throw new BadRequestExceptions(
                                "Total copies cannot be less than borrowed copies"
                        );
                    }

                    book.setTotalCopies(
                            addBook.getTotalCopies()
                    );

                    book.setAvailableCopies(
                            addBook.getTotalCopies()
                                    - borrowedCopies
                    );
                }
            }

            Book updatedBook = bookRepository.save(book);

            return mapToDTO(updatedBook);

        } catch (PhysicalBookDoesNotHaveDigitalFile e) {

            throw e;

        } catch (BadRequestExceptions e) {

            throw e;

        } catch (Exception e) {

            throw new BadRequestExceptions(
                    "Error while updating book : "
                            + e.getMessage()
            );
        }
    }
    public List<BookDTO> getAll() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::mapToDTO)
                .toList();
    }

    public BookDTO getBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book Not Found With This ID : " + bookId));
        return mapToDTO(book);
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
                .map(this::mapToDTO)
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
                .map(this::mapToDTO)
                .toList();
    }

    private BookDTO mapToDTO(Book book) {

        BookDTO dto = new BookDTO();

        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setCategory(book.getCategory());
        dto.setDescription(book.getDescription());

        dto.setBookType(book.getBookType());

        dto.setTotalCopies(book.getTotalCopies());
        dto.setAvailableCopies(book.getAvailableCopies());

        dto.setImgUrl(book.getImgUrl());
        dto.setPdfurl(book.getPdfurl());

        if (book.getPublishedBy() != null) {

            dto.setPublishedBy(
                    book.getPublishedBy().getFirstname()
                            + " " +
                            book.getPublishedBy().getLastname()
            );
        }

        return dto;
    }

}
