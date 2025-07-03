package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;

public interface FlashSaleService {
    ApiResponse<PaginationResponse<FlashSaleResponse>> getAllFlashSaleWithPagination(int page, int size);
    ApiResponse<PaginationResponse<FlashSaleResponse>> getAllWithFilter(int page, int size, String name, Long from, Long to, Byte status);
    ApiResponse<FlashSaleResponse> createFlashSale(FlashSaleRequest request);
    ApiResponse<FlashSaleResponse> updateFlashSale(FlashSaleRequest request, Integer id);
    ApiResponse<FlashSaleResponse> toggleStatus(Integer id);
}
