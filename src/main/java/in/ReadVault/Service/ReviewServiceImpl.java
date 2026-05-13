package in.ReadVault.Service;

import in.ReadVault.DTO.CreateReviewDTO;
import in.ReadVault.DTO.ReviewResponseDTO;
import in.ReadVault.Entity.Book;
import in.ReadVault.Entity.Review;
import in.ReadVault.Entity.User;
import in.ReadVault.GlobalExceptionHandling.*;
import in.ReadVault.Repository.BookRepository;
import in.ReadVault.Repository.ReviewRepository;
import in.ReadVault.Repository.ReviewService;
import in.ReadVault.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    public ReviewResponseDTO addReview(
            Long bookId,
            CreateReviewDTO createReviewDTO,
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new BookNotFoundException("Book not found")
                );

        // Prevent duplicate reviews
        if (reviewRepository.existsByUserAndBook(user, book)) {
            throw new YouAlreadyReviewedThisBookException(
                    "You already reviewed this book"
            );
        }

        Review review = Review.builder()
                .rating(createReviewDTO.getRating())
                .comment(createReviewDTO.getComment())
                .createdAt(LocalDateTime.now())
                .user(user)
                .book(book)
                .build();

        Review savedReview = reviewRepository.save(review);

        return mapToDTO(savedReview);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByBook(Long bookId) {

        List<Review> reviews =
                reviewRepository.findByBookId(bookId);

        return reviews.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public void deleteReview(Long reviewId) {

     User user= (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
               .getPrincipal();

        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException("Review not found")
                );

        boolean isAdmin = currentUser.getRole()
                .name()
                .equals("ADMIN");

        boolean isOwner = review.getUser()
                .getId()
                .equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new BadRequestExceptions(
                    "You are not allowed to delete this review"
            );
        }

        reviewRepository.delete(review);
    }

    // DTO Mapper
    private ReviewResponseDTO mapToDTO(Review review) {

        return ReviewResponseDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .userName(review.getUser().getUsername())
                .bookId(review.getBook().getId())
                .createdAt(review.getCreatedAt())
                .build();
    }
}