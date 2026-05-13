package in.ReadVault.Repository;

import in.ReadVault.DTO.CreateReviewDTO;
import in.ReadVault.DTO.ReviewResponseDTO;

import java.util.List;

public interface ReviewService {

    ReviewResponseDTO addReview(
            Long bookId,
            CreateReviewDTO createReviewDTO, Long userId);

    List<ReviewResponseDTO> getReviewsByBook(Long bookId);

    void deleteReview(Long reviewId);
}