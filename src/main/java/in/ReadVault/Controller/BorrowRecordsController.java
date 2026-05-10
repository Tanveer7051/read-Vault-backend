package in.ReadVault.Controller;

import in.ReadVault.DTO.BorrowRecordDTO;
import in.ReadVault.Entity.User;
import in.ReadVault.Service.BorrowRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowRecordsController {

    private final BorrowRecordsService borrowRecordsService;

    private Long getUserId(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    @PostMapping("/take/{bookId}")
    public ResponseEntity<BorrowRecordDTO> takeBook(@PathVariable Long bookId, Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(borrowRecordsService.takeBook(userId, bookId));
    }

    @PutMapping("/return/{borrowId}")
    public ResponseEntity<Map<String, Object>> returnBook(
            @PathVariable Long borrowId,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            borrowRecordsService.adminReturnBook(borrowId);
        } else {
            Long userId = getUserId(authentication);
            borrowRecordsService.returnBook(userId, borrowId);
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Book returned successfully",
                        "timeStamp", LocalDate.now()
                )
        );
    }

    @PostMapping("/digital/{bookId}")
    public ResponseEntity<BorrowRecordDTO> digitalAccess(@PathVariable Long bookId, Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.status(HttpStatus.CREATED).body(borrowRecordsService.digitalAccess(userId, bookId));
    }

    @GetMapping
    public ResponseEntity<List<BorrowRecordDTO>> getAll() {
        return ResponseEntity.ok(borrowRecordsService.getAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<BorrowRecordDTO>> getMyRecords(Authentication authentication) {

        Long userId = getUserId(authentication);

        return ResponseEntity.ok(borrowRecordsService.getRecordOfUser(userId));
    }
}