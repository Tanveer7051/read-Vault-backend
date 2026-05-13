package in.ReadVault.Controller;

import in.ReadVault.DTO.CreateReviewDTO;
import in.ReadVault.DTO.ReviewResponseDTO;
import in.ReadVault.Entity.User;
import in.ReadVault.Repository.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Add Review
    @PostMapping("/{bookId}")
    public ResponseEntity<ReviewResponseDTO> addReview(
            @PathVariable Long bookId,
            @Valid @RequestBody CreateReviewDTO createReviewDTO, Authentication authentication
    ) {
        Long userId =
                ((User) authentication.getPrincipal()).getId();
        ReviewResponseDTO review =
                reviewService.addReview(bookId, createReviewDTO,userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(review);
    }

    // Get All Reviews Of A Book
    @GetMapping("/{bookId}")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByBook(
            @PathVariable Long bookId
    ) {

        return ResponseEntity.ok(
                reviewService.getReviewsByBook(bookId)
        );
    }

    // Delete Review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId
    ) {

        reviewService.deleteReview(reviewId);

        return ResponseEntity.ok("Review deleted successfully");
    }
}

