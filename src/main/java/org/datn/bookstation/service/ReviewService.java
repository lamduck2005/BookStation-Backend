package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;

public interface ReviewService {
    ApiResponse<PaginationResponse<ReviewResponse>> getAllReviewWithPagination(int page, int size);
}
