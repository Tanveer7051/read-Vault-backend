package in.ReadVault.Service;

import in.ReadVault.DTO.BorrowRecordDTO;
import in.ReadVault.Entity.*;
import in.ReadVault.GlobalExceptionHandling.*;
import in.ReadVault.Repository.BookRepository;
import in.ReadVault.Repository.BorrowRecordsRepository;
import in.ReadVault.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowRecordsService {

    private final ModelMapper modelMapper;
    private final BorrowRecordsRepository borrowRecordsRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public BorrowRecordDTO takeBook(Long userId, Long bookId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book Not Found"));

        if (book.getBookType() != BookType.PHYSICAL) {
            throw new UseDigitalApiException("Use digital API for digital books");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BookNotFoundException("No copies available");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        long count = borrowRecordsRepository.countByUserIdAndBookIdAndStatus(userId, bookId, BorrowStatus.ACTIVE);

        BorrowRecords record = new BorrowRecords();
        record.setUser(user);
        record.setBook(book);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusWeeks(2));
        record.setType(BorrowType.PHYSICAL);
        record.setStatus(BorrowStatus.ACTIVE);
        record.setRenewalCount((int) (count + 1));
        record.setCreatedAt(LocalDate.now());

        bookRepository.save(book);
        BorrowRecords saved = borrowRecordsRepository.save(record);

        return modelMapper.map(saved, BorrowRecordDTO.class);
    }

    @Transactional
    public void returnBook(Long userId, Long borrowId) {
        System.out.println("UserId: " + userId);
        System.out.println("BorrowId: " + borrowId);

        BorrowRecords record = borrowRecordsRepository.findById(borrowId)
                .orElseThrow(() -> new BookNotFoundException("Borrow record not found"));

        if (!record.getUser().getId().equals(userId)) {
            throw new BadRequestExceptions("Unauthorized access");
        }

        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new BadRequestExceptions("Book already returned");
        }
        if (record.getType() != BorrowType.PHYSICAL) {
            throw new BadRequestExceptions("Please Select The Correct BookId");
        }

        Book book = record.getBook();
        if (book == null) {
            throw new BadRequestExceptions("Book not found in borrow record");
        }

        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new BadRequestExceptions("Invalid return state");
        }

        book.setAvailableCopies(book.getAvailableCopies() + 1);

        record.setStatus(BorrowStatus.COMPLETED);
        record.setUpdatedAt(LocalDate.now());

        bookRepository.save(book);
        borrowRecordsRepository.save(record);
    }

    @Transactional
    public BorrowRecordDTO digitalAccess(Long userId, Long bookId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book Not Found"));

        if (book.getBookType() != BookType.DIGITAL) {
            throw new UsePhysicalApiException("Use physical API for physical books");
        }

        long count = borrowRecordsRepository.countByUserIdAndBookIdAndStatus(userId, bookId, BorrowStatus.ACTIVE);

        BorrowRecords record = new BorrowRecords();
        record.setUser(user);
        record.setBook(book);
        record.setIssueDate(LocalDate.now());
        record.setDigitalExpiry(LocalDate.now().plusMonths(1));
        record.setType(BorrowType.DIGITAL);
        record.setStatus(BorrowStatus.ACTIVE);
        record.setRenewalCount((int) (count + 1));
        record.setCreatedAt(LocalDate.now());

        BorrowRecords saved = borrowRecordsRepository.save(record);

        return modelMapper.map(saved, BorrowRecordDTO.class);
    }

    public List<BorrowRecordDTO> getAll() {
        return borrowRecordsRepository.findAll()
                .stream()
                .map(record -> modelMapper.map(record, BorrowRecordDTO.class))
                .toList();
    }

    public List<BorrowRecordDTO> getRecordOfUser(Long userId) {

        List<BorrowRecords> records = borrowRecordsRepository.findByUserId(userId);

        if (records.isEmpty()) {
            throw new NoRecordFoundException("No records found for user: " + userId);
        }

        return records.stream()
                .map(record -> modelMapper.map(record, BorrowRecordDTO.class))
                .toList();
    }
}