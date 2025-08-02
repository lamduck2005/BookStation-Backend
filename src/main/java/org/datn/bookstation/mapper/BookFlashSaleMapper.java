package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BookFlashSaleMapper {

    public static FlashSaleItemBookRequest mapToFlashSaleItemBookRequest(Object[] data) {
        FlashSaleItemBookRequest dto = new FlashSaleItemBookRequest();

        dto.setId((Integer) data[0]); // bookId
        dto.setBookName((String) data[1]); // bookName
        dto.setPrice((BigDecimal) data[2]); // price
        dto.setOriginalPrice((BigDecimal) data[3]); // originalPrice
        dto.setDiscountPercentage(data[4] != null ? ((Number) data[4]).intValue() : null); // discountPercentage
        dto.setStockQuantity(data[5] != null ? ((Number) data[5]).intValue() : null); // stockQuantity

        // images: String, split thành List<String>
        String imagesStr = (String) data[6];
        if (imagesStr != null && !imagesStr.isEmpty()) {
            List<String> images = Arrays.stream(imagesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            dto.setImages(images);
        } else {
            dto.setImages(Collections.emptyList());
        }

        dto.setCategoryName((String) data[7]);
        dto.setIsInFlashSale(data[8] != null && (Boolean) data[8]);
        dto.setFlashSalePrice(data[9] != null ? (BigDecimal) data[9] : null);
        dto.setFlashSaleStockQuantity(data[10] != null ? ((Number) data[10]).intValue() : null);
        dto.setFlashSaleSoldCount(data[11] != null ? ((Number) data[11]).intValue() : null);
        dto.setSoldCount(data[12] != null ? ((Number) data[12]).intValue() : null);

        return dto;
    }
}
