package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.enums.ReviewStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Integer id;

    private Integer bookId;
    private String bookName;

    private Integer userId;
    private String userName;
    private String userEmail;

    private Integer rating;

    private String comment;

    private Long reviewDate;

    private ReviewStatus reviewStatus;

    private Long createdAt;

    private Long updatedAt;

    private Long createdBy;

    private Long updatedBy;


    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getBook().getId(),
            review.getBook().getBookName(),
            review.getUser().getId(),
            review.getUser().getFullName(),
            review.getUser().getEmail(),
            review.getRating(),
            review.getComment(),
            review.getReviewDate(),
            review.getReviewStatus(),
            review.getCreatedAt(),
            review.getUpdatedAt(),
            review.getCreatedBy(),
            review.getUpdatedBy()
        );
    }
}
