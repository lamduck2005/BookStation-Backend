package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.OrderDetailRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.service.PriceValidationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PriceValidationServiceImpl implements PriceValidationService {
    
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    
    @Override
    public ApiResponse<String> validateProductPrices(List<OrderDetailRequest> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return new ApiResponse<>(400, "Danh sách sản phẩm không được để trống", null);
        }
        
        List<String> errors = new ArrayList<>();
        
        for (OrderDetailRequest detail : orderDetails) {
            String error = validateSingleProductPrice(detail);
            if (error != null) {
                errors.add(error);
            }
        }
        
        if (!errors.isEmpty()) {
            String errorMessage = "Giá sản phẩm đã thay đổi:\n" + String.join("\n", errors);
            return new ApiResponse<>(400, errorMessage, null);
        }
        
        return new ApiResponse<>(200, "Tất cả giá sản phẩm hợp lệ", "valid");
    }
    
    @Override
    public String validateSingleProductPrice(OrderDetailRequest orderDetail) {
        if (orderDetail.getBookId() == null) {
            return "Book ID không hợp lệ";
        }
        
        Book book = bookRepository.findById(orderDetail.getBookId()).orElse(null);
        if (book == null) {
            return "Không tìm thấy sách với ID: " + orderDetail.getBookId();
        }
        
        // Kiểm tra stock
        if (book.getStockQuantity() == null || book.getStockQuantity() < orderDetail.getQuantity()) {
            return String.format("Sách '%s' chỉ còn %d cuốn trong kho", 
                                book.getBookName(), 
                                book.getStockQuantity() != null ? book.getStockQuantity() : 0);
        }
        
        // Lấy giá hiện tại của sách
        BigDecimal currentBookPrice = getCurrentBookPrice(book);
        
        // Kiểm tra flash sale nếu có
        BigDecimal currentFlashSalePrice = null;
        FlashSaleItem activeFlashSale = getCurrentActiveFlashSale(orderDetail.getBookId());
        
        if (activeFlashSale != null) {
            currentFlashSalePrice = activeFlashSale.getDiscountPrice();            
            // Kiểm tra số lượng flash sale còn lại
            int quantityRemaining = activeFlashSale.getStockQuantity() - 
                                   (activeFlashSale.getSoldCount() != null ? activeFlashSale.getSoldCount() : 0);
            if (quantityRemaining < orderDetail.getQuantity()) {
                return String.format("Flash sale cho sách '%s' chỉ còn %d sản phẩm", 
                                    book.getBookName(), quantityRemaining);
            }
        } else {
            // Không có flash sale, không cần kiểm tra các trường liên quan
        }
        
        // Validate giá frontend
        if (orderDetail.getFrontendPrice() == null) {
            return "Giá frontend không được để trống";
        }
        BigDecimal expectedPrice = currentFlashSalePrice != null ? currentFlashSalePrice : currentBookPrice;
        if (orderDetail.getFrontendPrice().compareTo(expectedPrice) != 0) {
            return String.format("Giá của sách '%s' đã thay đổi từ %s VND thành %s VND", 
                                book.getBookName(), 
                                formatPrice(orderDetail.getFrontendPrice()),
                                 formatPrice(expectedPrice));
        }
        
        return null; // Hợp lệ
    }
    
    /**
     * Lấy giá hiện tại của sách (đã bao gồm discount nếu có)
     */
    private BigDecimal getCurrentBookPrice(Book book) {
        BigDecimal basePrice = book.getPrice();
        
        // Áp dụng discount nếu có và đang active
        if (book.getDiscountActive() != null && book.getDiscountActive()) {
            if (book.getDiscountValue() != null && book.getDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
                basePrice = basePrice.subtract(book.getDiscountValue());
            } else if (book.getDiscountPercent() != null && book.getDiscountPercent() > 0) {
                BigDecimal discountAmount = basePrice.multiply(new BigDecimal(book.getDiscountPercent()))
                                                    .divide(new BigDecimal("100"));
                basePrice = basePrice.subtract(discountAmount);
            }
        }
        
        // Đảm bảo giá không âm
        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            basePrice = BigDecimal.ZERO;
        }
        
        return basePrice;
    }
    
    /**
     * Lấy flash sale đang hoạt động cho sách
     */
    private FlashSaleItem getCurrentActiveFlashSale(Integer bookId) {
        return flashSaleItemRepository.findAll().stream()
            .filter(item -> item.getBook() != null && item.getBook().getId().equals(bookId))
            .filter(item -> item.getStatus() != null && item.getStatus() == 1)
            .filter(item -> item.getFlashSale() != null && item.getFlashSale().getStatus() != null && item.getFlashSale().getStatus() == 1)
            .filter(item -> {
                // Kiểm tra thời gian hiệu lực
                long now = System.currentTimeMillis();
                return item.getFlashSale().getStartTime() != null && item.getFlashSale().getStartTime() <= now &&
                       item.getFlashSale().getEndTime() != null && item.getFlashSale().getEndTime() > now;
            })
            .filter(item -> {
                // Kiểm tra số lượng còn lại
                int quantityRemaining = item.getStockQuantity() - 
                                       (item.getSoldCount() != null ? item.getSoldCount() : 0);
                return quantityRemaining > 0;
            })
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Format giá tiền để hiển thị
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        return String.format("%,.0f", price);
    }
}
