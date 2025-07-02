package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.FlashSaleItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleItemResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import java.math.BigDecimal;

public interface FlashSaleItemService {
    ApiResponse<PaginationResponse<FlashSaleItemResponse>> getAllWithFilter(int page, int size, Integer flashSaleId, Integer bookId, Byte status,
                                                                            BigDecimal minPrice, BigDecimal maxPrice,
                                                                            BigDecimal minPercent, BigDecimal maxPercent,
                                                                            Integer minQuantity, Integer maxQuantity);
    ApiResponse<FlashSaleItemResponse> create(FlashSaleItemRequest request);
    ApiResponse<FlashSaleItemResponse> update(Integer id, FlashSaleItemRequest request);
    ApiResponse<FlashSaleItemResponse> toggleStatus(Integer id);
} 