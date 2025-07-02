package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.datn.bookstation.entity.FlashSale;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FlashSaleMapper {
    FlashSaleResponse toResponse(FlashSale flashSale);
    FlashSale toFlashSale(FlashSaleRequest request);
}
