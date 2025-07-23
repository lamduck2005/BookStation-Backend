package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer>, JpaSpecificationExecutor<Review> {
    
    // Tìm review theo bookId và status
    List<Review> findByBookIdAndReviewStatusIn(Integer bookId, List<ReviewStatus> statuses);
    
    // Tìm tất cả review published của một book  
    List<Review> findByBookId(Integer bookId);

    // Kiểm tra xem user đã viết review cho sách này chưa
    boolean existsByBookIdAndUserId(Integer bookId, Integer userId);

    // Kiểm tra review thuộc về user hay không (để cho phép sửa)
    boolean existsByIdAndUserId(Integer id, Integer userId);
    
    // Tìm review theo userId
    List<Review> findByUserId(Integer userId);

    // Tìm review theo id và user
    Review findByIdAndUserId(Integer id, Integer userId);

    // Tìm review theo bookId và userId
    Review findByBookIdAndUserId(Integer bookId, Integer userId);
}
