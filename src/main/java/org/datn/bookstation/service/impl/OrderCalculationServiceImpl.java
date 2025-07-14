package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.OrderCalculationRequest;
import org.datn.bookstation.dto.response.OrderCalculationResponse;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.VoucherType;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.OrderCalculationService;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderCalculationServiceImpl implements OrderCalculationService {
    
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final VoucherRepository voucherRepository;
    private final FlashSaleService flashSaleService;
    private final VoucherCalculationService voucherCalculationService;
    
    @Override
    public ApiResponse<OrderCalculationResponse> calculateOrderTotal(OrderCalculationRequest request) {
        try {
            log.info("🔄 Starting order calculation for user: {}", request.getUserId());
            
            // 1. Validate cơ bản
            ApiResponse<String> validation = validateOrderConditions(request);
            if (validation.getStatus() != 200) {
                return new ApiResponse<>(validation.getStatus(), validation.getMessage(), null);
            }
            
            // 2. Lấy thông tin user
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return new ApiResponse<>(404, "Không tìm thấy user", null);
            }
            
            // 3. Tính toán từng sản phẩm
            List<OrderCalculationResponse.ItemCalculationDetail> itemDetails = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO;
            
            for (OrderCalculationRequest.OrderItemCalculationRequest item : request.getItems()) {
                OrderCalculationResponse.ItemCalculationDetail itemDetail = calculateItemDetail(item);
                if (itemDetail == null) {
                    return new ApiResponse<>(400, "Không thể tính toán sản phẩm ID: " + item.getBookId(), null);
                }
                itemDetails.add(itemDetail);
                subtotal = subtotal.add(itemDetail.getItemTotal());
            }
            
            // 4. Tính tổng trước voucher
            BigDecimal totalBeforeDiscount = subtotal.add(request.getShippingFee());
            
            // 5. Tính voucher (nếu có)
            BigDecimal regularVoucherDiscount = BigDecimal.ZERO;
            BigDecimal shippingVoucherDiscount = BigDecimal.ZERO;
            List<OrderCalculationResponse.VoucherDetail> appliedVouchers = new ArrayList<>();
            
            if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
                // Tạo order tạm để tính voucher
                Order tempOrder = new Order();
                tempOrder.setSubtotal(subtotal);
                tempOrder.setShippingFee(request.getShippingFee());
                
                VoucherCalculationService.VoucherCalculationResult voucherResult = 
                    voucherCalculationService.calculateVoucherDiscount(tempOrder, request.getVoucherIds(), request.getUserId());
                
                regularVoucherDiscount = voucherResult.getTotalProductDiscount();
                shippingVoucherDiscount = voucherResult.getTotalShippingDiscount();
                
                // Chuyển đổi thông tin voucher
                for (VoucherCalculationService.VoucherApplicationDetail voucherApp : voucherResult.getAppliedVouchers()) {
                    Voucher voucher = voucherRepository.findById(voucherApp.getVoucherId()).orElse(null);
                    if (voucher != null) {
                        OrderCalculationResponse.VoucherDetail voucherDetail = OrderCalculationResponse.VoucherDetail.builder()
                            .voucherId(voucher.getId())
                            .voucherCode(voucher.getCode())
                            .voucherName(voucher.getName())
                            .voucherType(voucherApp.getVoucherType().name())
                            .discountApplied(voucherApp.getDiscountApplied())
                            .description(generateVoucherDescription(voucher, voucherApp.getDiscountApplied()))
                            .build();
                        appliedVouchers.add(voucherDetail);
                    }
                }
            }
            
            // 6. Tính tổng cuối cùng
            BigDecimal totalVoucherDiscount = regularVoucherDiscount.add(shippingVoucherDiscount);
            BigDecimal finalTotal = totalBeforeDiscount.subtract(totalVoucherDiscount);
            finalTotal = finalTotal.max(BigDecimal.ZERO); // Không âm
            
            // 7. Tạo response
            OrderCalculationResponse response = OrderCalculationResponse.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userName(user.getFullName())
                .itemDetails(itemDetails)
                .subtotal(subtotal)
                .shippingFee(request.getShippingFee())
                .totalBeforeDiscount(totalBeforeDiscount)
                .regularVoucherDiscount(regularVoucherDiscount)
                .shippingVoucherDiscount(shippingVoucherDiscount)
                .totalVoucherDiscount(totalVoucherDiscount)
                .appliedVouchers(appliedVouchers)
                .finalTotal(finalTotal)
                .message(generateCalculationMessage(itemDetails, appliedVouchers))
                .build();
            
            log.info("✅ Order calculation completed. Final total: {}", finalTotal);
            return new ApiResponse<>(200, "Tính toán thành công", response);
            
        } catch (Exception e) {
            log.error("❌ Error calculating order total", e);
            return new ApiResponse<>(500, "Lỗi khi tính toán: " + e.getMessage(), null);
        }
    }
    
    @Override
    public ApiResponse<String> validateOrderConditions(OrderCalculationRequest request) {
        try {
            // 1. Kiểm tra user
            if (!userRepository.existsById(request.getUserId())) {
                return new ApiResponse<>(404, "User không tồn tại", null);
            }
            
            // 2. Kiểm tra sản phẩm
            for (OrderCalculationRequest.OrderItemCalculationRequest item : request.getItems()) {
                Book book = bookRepository.findById(item.getBookId()).orElse(null);
                if (book == null) {
                    return new ApiResponse<>(404, "Sách ID " + item.getBookId() + " không tồn tại", null);
                }
                
                // Kiểm tra tồn kho
                if (book.getStockQuantity() < item.getQuantity()) {
                    return new ApiResponse<>(400, "Sách '" + book.getBookName() + "' không đủ số lượng", null);
                }
            }
            
            // 3. Kiểm tra voucher (nếu có)
            if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
                if (request.getVoucherIds().size() > 2) {
                    return new ApiResponse<>(400, "Chỉ được sử dụng tối đa 2 voucher", null);
                }
                
                int regularCount = 0;
                int shippingCount = 0;
                
                for (Integer voucherId : request.getVoucherIds()) {
                    Voucher voucher = voucherRepository.findById(voucherId).orElse(null);
                    if (voucher == null) {
                        return new ApiResponse<>(404, "Voucher ID " + voucherId + " không tồn tại", null);
                    }
                    
                    // Kiểm tra loại voucher
                    if (voucher.getVoucherType() == VoucherType.FREE_SHIPPING) {
                        shippingCount++;
                    } else {
                        regularCount++;
                    }
                }
                
                if (regularCount > 1) {
                    return new ApiResponse<>(400, "Chỉ được sử dụng 1 voucher thường", null);
                }
                if (shippingCount > 1) {
                    return new ApiResponse<>(400, "Chỉ được sử dụng 1 voucher miễn phí ship", null);
                }
            }
            
            return new ApiResponse<>(200, "Validation thành công", "OK");
            
        } catch (Exception e) {
            log.error("❌ Error validating order conditions", e);
            return new ApiResponse<>(500, "Lỗi khi validate: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tính toán chi tiết cho từng sản phẩm
     */
    private OrderCalculationResponse.ItemCalculationDetail calculateItemDetail(OrderCalculationRequest.OrderItemCalculationRequest item) {
        try {
            Book book = bookRepository.findById(item.getBookId()).orElse(null);
            if (book == null) return null;
            
            BigDecimal originalPrice = book.getPrice();
            BigDecimal unitPrice = originalPrice;
            Boolean isFlashSale = false;
            Integer flashSaleItemId = null;
            BigDecimal savedAmount = BigDecimal.ZERO;
            String flashSaleName = null;
            
            // Tự động phát hiện flash sale
            Optional<FlashSaleItem> activeFlashSaleOpt = flashSaleService.findActiveFlashSaleForBook(book.getId().longValue());
            if (activeFlashSaleOpt.isPresent()) {
                FlashSaleItem flashSaleItem = activeFlashSaleOpt.get();
                
                // Kiểm tra stock flash sale
                if (flashSaleItem.getStockQuantity() >= item.getQuantity()) {
                    unitPrice = flashSaleItem.getDiscountPrice();
                    isFlashSale = true;
                    flashSaleItemId = flashSaleItem.getId();
                    savedAmount = originalPrice.subtract(unitPrice).multiply(BigDecimal.valueOf(item.getQuantity()));
                    flashSaleName = flashSaleItem.getFlashSale().getName();
                    
                    log.info("🔥 Applied flash sale for book {}: {} -> {}", book.getId(), originalPrice, unitPrice);
                }
            }
            
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            
            return OrderCalculationResponse.ItemCalculationDetail.builder()
                .bookId(book.getId())
                .bookName(book.getBookName())
                .bookCode(book.getBookCode())
                .quantity(item.getQuantity())
                .originalPrice(originalPrice)
                .unitPrice(unitPrice)
                .itemTotal(itemTotal)
                .isFlashSale(isFlashSale)
                .flashSaleItemId(flashSaleItemId)
                .savedAmount(savedAmount)
                .flashSaleName(flashSaleName)
                .build();
                
        } catch (Exception e) {
            log.error("❌ Error calculating item detail for book ID: {}", item.getBookId(), e);
            return null;
        }
    }
    
    /**
     * Tạo mô tả voucher
     */
    private String generateVoucherDescription(Voucher voucher, BigDecimal discountApplied) {
        switch (voucher.getVoucherType()) {
            case PERCENTAGE:
                return String.format("Giảm %s%% (tối đa %s)", 
                    voucher.getDiscountPercentage(), 
                    voucher.getMaxDiscountValue() != null ? voucher.getMaxDiscountValue() + "đ" : "không giới hạn");
            case FIXED_AMOUNT:
                return String.format("Giảm %sđ", voucher.getDiscountAmount());
            case FREE_SHIPPING:
                return "Miễn phí vận chuyển";
            default:
                return voucher.getName();
        }
    }
    
    /**
     * Tạo thông báo tổng quan cho admin
     */
    private String generateCalculationMessage(List<OrderCalculationResponse.ItemCalculationDetail> itemDetails, 
                                              List<OrderCalculationResponse.VoucherDetail> appliedVouchers) {
        StringBuilder message = new StringBuilder();
        
        // Thống kê flash sale
        long flashSaleCount = itemDetails.stream().mapToLong(item -> item.getIsFlashSale() ? 1 : 0).sum();
        if (flashSaleCount > 0) {
            message.append(String.format("🔥 %d sản phẩm được áp dụng flash sale. ", flashSaleCount));
        }
        
        // Thống kê voucher
        if (!appliedVouchers.isEmpty()) {
            message.append(String.format("🎫 Áp dụng %d voucher. ", appliedVouchers.size()));
        }
        
        if (message.length() == 0) {
            message.append("💰 Đơn hàng thường, không có ưu đãi đặc biệt.");
        }
        
        return message.toString().trim();
    }
}
