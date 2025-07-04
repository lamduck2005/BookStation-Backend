package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemResponseMapper {
    
    public CartItemResponse toResponse(CartItem cartItem) {
        if (cartItem == null) return null;
        
        CartItemResponse response = new CartItemResponse();
        
        // Basic info
        response.setId(cartItem.getId());
        response.setCartId(cartItem.getCart() != null ? cartItem.getCart().getId() : null);
        response.setQuantity(cartItem.getQuantity());
        response.setCreatedAt(cartItem.getCreatedAt());
        response.setUpdatedAt(cartItem.getUpdatedAt());
        response.setCreatedBy(cartItem.getCreatedBy());
        response.setUpdatedBy(cartItem.getUpdatedBy());
        response.setStatus(cartItem.getStatus());
        
        // Book info
        if (cartItem.getBook() != null) {
            response.setBookId(cartItem.getBook().getId());
            response.setBookName(cartItem.getBook().getBookName());
            response.setBookCode(cartItem.getBook().getBookCode());
            response.setBookImageUrl(cartItem.getBook().getCoverImageUrl());
            response.setBookPrice(cartItem.getBook().getPrice());
            response.setAvailableStock(cartItem.getBook().getStockQuantity());
        }
        
        // Flash sale info
        if (cartItem.getFlashSaleItem() != null) {
            response.setFlashSaleItemId(cartItem.getFlashSaleItem().getId());
            response.setFlashSalePrice(cartItem.getFlashSaleItem().getDiscountPrice());
            response.setFlashSaleDiscount(cartItem.getFlashSaleItem().getDiscountPercentage());
            response.setItemType("FLASH_SALE");
            response.setUnitPrice(cartItem.getFlashSaleItem().getDiscountPrice());
            response.setAvailableStock(cartItem.getFlashSaleItem().getStockQuantity());
            
            if (cartItem.getFlashSaleItem().getFlashSale() != null) {
                response.setFlashSaleName(cartItem.getFlashSaleItem().getFlashSale().getName());
                response.setFlashSaleEndTime(cartItem.getFlashSaleItem().getFlashSale().getEndTime());
                
                // Check if flash sale expired
                long currentTime = System.currentTimeMillis();
                response.setFlashSaleExpired(
                    cartItem.getFlashSaleItem().getFlashSale().getEndTime() < currentTime
                );
            }
        } else {
            response.setItemType("REGULAR");
            response.setUnitPrice(cartItem.getBook() != null ? cartItem.getBook().getPrice() : BigDecimal.ZERO);
        }
        
        // Calculate total price
        if (response.getUnitPrice() != null) {
            response.setTotalPrice(response.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        
        // Stock validation
        if (response.getAvailableStock() != null) {
            response.setOutOfStock(cartItem.getQuantity() > response.getAvailableStock());
            response.setStockLimited(response.getAvailableStock() < cartItem.getQuantity());
            response.setMaxAvailableQuantity(response.getAvailableStock());
            response.setCanAddMore(response.getAvailableStock() > cartItem.getQuantity());
            
            // Generate stock warning message
            if (response.isOutOfStock()) {
                response.setStockWarning("Hết hàng");
            } else if (response.getAvailableStock() <= 5) {
                response.setStockWarning("Chỉ còn " + response.getAvailableStock() + " sản phẩm");
            }
        }
        
        return response;
    }
}
