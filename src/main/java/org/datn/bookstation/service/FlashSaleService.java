package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;

public interface FlashSaleService {
    ApiResponse<PaginationResponse<FlashSaleResponse>> getAllFlashSaleWithPagination(int page, int size);
    ApiResponse<FlashSaleResponse> createFlashSale(FlashSaleRequest request);
    ApiResponse<FlashSaleResponse> updateFlashSale(FlashSaleRequest request, Integer id);
}
