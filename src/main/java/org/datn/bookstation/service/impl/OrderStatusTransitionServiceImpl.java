package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.OrderStatusTransitionRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderStatusTransitionResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.OrderStatusTransitionService;
import org.datn.bookstation.service.PointManagementService;
import org.datn.bookstation.service.VoucherManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implementation của OrderStatusTransitionService
 * Xử lý chuyển đổi trạng thái đơn hàng với đầy đủ business logic
 */
@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class OrderStatusTransitionServiceImpl implements OrderStatusTransitionService {
    
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final BookRepository bookRepository;
    private final PointManagementService pointManagementService;
    private final VoucherManagementService voucherManagementService;
    
    // Định nghĩa các luồng chuyển đổi trạng thái hợp lệ
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED),
        OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELED),
        OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELED),
        OrderStatus.DELIVERED, Set.of(OrderStatus.RETURNED, OrderStatus.PARTIALLY_REFUNDED),
        OrderStatus.CANCELED, Set.of(OrderStatus.REFUNDING),
        OrderStatus.RETURNED, Set.of(OrderStatus.REFUNDING),
        OrderStatus.REFUNDING, Set.of(OrderStatus.REFUNDED),
        OrderStatus.PARTIALLY_REFUNDED, Set.of(OrderStatus.RETURNED, OrderStatus.REFUNDING),
        OrderStatus.REFUNDED, Collections.emptySet() // Trạng thái cuối
    );
    
    @Override
    @Transactional
    public ApiResponse<OrderStatusTransitionResponse> transitionOrderStatus(OrderStatusTransitionRequest request) {
        try {
            // 1. VALIDATION CƠ BẢN
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + request.getOrderId()));
            
            if (!order.getOrderStatus().equals(request.getCurrentStatus())) {
                return new ApiResponse<>(400, 
                    "Trạng thái hiện tại không khớp. Hiện tại: " + order.getOrderStatus() + 
                    ", Yêu cầu: " + request.getCurrentStatus(), null);
            }
            
            // 2. VALIDATION LUỒNG CHUYỂN ĐỔI
            if (!isValidTransition(request.getCurrentStatus(), request.getNewStatus())) {
                return new ApiResponse<>(400, 
                    "Không thể chuyển từ " + request.getCurrentStatus() + " sang " + request.getNewStatus(), null);
            }
            
            // 3. VALIDATION NGHIỆP VỤ ĐẶC BIỆT
            String validationError = validateSpecialBusinessRules(order, request);
            if (validationError != null) {
                return new ApiResponse<>(400, validationError, null);
            }
            
            // 4. THỰC HIỆN CHUYỂN ĐỔI
            OrderStatusTransitionResponse.BusinessImpactSummary businessImpact = 
                executeStatusTransition(order, request);
            
            // 5. CẬP NHẬT DATABASE
            order.setOrderStatus(request.getNewStatus());
            order.setUpdatedBy(request.getPerformedBy());
            order.setUpdatedAt(System.currentTimeMillis());
            orderRepository.save(order);
            
            // 6. TẠO RESPONSE
            OrderStatusTransitionResponse response = OrderStatusTransitionResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .previousStatus(request.getCurrentStatus())
                .newStatus(request.getNewStatus())
                .transitionMessage(getTransitionDescription(request.getCurrentStatus(), request.getNewStatus()))
                .transitionTime(System.currentTimeMillis())
                .businessImpact(businessImpact)
                .build();
            
            log.info("✅ Chuyển đổi trạng thái đơn hàng {} từ {} sang {} thành công", 
                    order.getCode(), request.getCurrentStatus(), request.getNewStatus());
            
            return new ApiResponse<>(200, "Chuyển đổi trạng thái thành công", response);
            
        } catch (Exception e) {
            log.error("❌ Lỗi khi chuyển đổi trạng thái đơn hàng: {}", e.getMessage(), e);
            return new ApiResponse<>(500, "Lỗi hệ thống: " + e.getMessage(), null);
        }
    }
    
    @Override
    public boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        Set<OrderStatus> validNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        return validNextStatuses != null && validNextStatuses.contains(newStatus);
    }
    
    @Override
    public List<OrderStatus> getValidNextStatuses(OrderStatus currentStatus) {
        Set<OrderStatus> validStatuses = VALID_TRANSITIONS.get(currentStatus);
        return validStatuses != null ? new ArrayList<>(validStatuses) : Collections.emptyList();
    }
    
    @Override
    public String getTransitionDescription(OrderStatus currentStatus, OrderStatus newStatus) {
        String key = currentStatus + "_TO_" + newStatus;
        
        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("PENDING_TO_CONFIRMED", "Xác nhận đơn hàng - Đơn hàng đã được duyệt và sẵn sàng xử lý");
        descriptions.put("PENDING_TO_CANCELED", "Hủy đơn hàng - Đơn hàng bị hủy khi đang chờ xử lý");
        descriptions.put("CONFIRMED_TO_SHIPPED", "Giao hàng - Đơn hàng đã được đóng gói và bàn giao cho đơn vị vận chuyển");
        descriptions.put("CONFIRMED_TO_CANCELED", "Hủy đơn hàng - Đơn hàng bị hủy sau khi đã xác nhận");
        descriptions.put("SHIPPED_TO_DELIVERED", "Giao thành công - Khách hàng đã nhận được hàng");
        descriptions.put("SHIPPED_TO_CANCELED", "Hủy đơn hàng - Đơn hàng bị hủy trong quá trình giao");
        descriptions.put("DELIVERED_TO_RETURNED", "Trả hàng - Khách hàng yêu cầu trả lại sản phẩm");
        descriptions.put("DELIVERED_TO_PARTIALLY_REFUNDED", "Hoàn tiền một phần - Hoàn tiền cho một số sản phẩm trong đơn hàng");
        descriptions.put("CANCELED_TO_REFUNDING", "Bắt đầu hoàn tiền - Tiến hành hoàn tiền cho đơn hàng đã hủy");
        descriptions.put("RETURNED_TO_REFUNDING", "Bắt đầu hoàn tiền - Tiến hành hoàn tiền cho đơn hàng đã trả");
        descriptions.put("REFUNDING_TO_REFUNDED", "Hoàn tiền thành công - Đã hoàn tiền cho khách hàng");
        descriptions.put("PARTIALLY_REFUNDED_TO_RETURNED", "Trả hàng toàn bộ - Khách hàng yêu cầu trả thêm sản phẩm còn lại");
        descriptions.put("PARTIALLY_REFUNDED_TO_REFUNDING", "Hoàn tiền toàn bộ - Tiến hành hoàn tiền cho toàn bộ đơn hàng");
        
        return descriptions.getOrDefault(key, "Chuyển đổi trạng thái từ " + currentStatus + " sang " + newStatus);
    }
    
    /**
     * Validation các quy tắc nghiệp vụ đặc biệt
     */
    private String validateSpecialBusinessRules(Order order, OrderStatusTransitionRequest request) {
        // Kiểm tra loại đơn hàng
        if (!isValidOrderType(order.getOrderType())) {
            return "Loại đơn hàng không hợp lệ. Chỉ cho phép 'ONLINE' hoặc 'COUNTER'";
        }
        
        // Kiểm tra quyền thực hiện
        if (request.getNewStatus() == OrderStatus.CONFIRMED && order.getOrderType().equals("COUNTER")) {
            // Đơn tại quầy phải có staff xác nhận
            if (request.getStaffId() == null) {
                return "Đơn hàng tại quầy phải có nhân viên xác nhận";
            }
        }
        
        // Đã bỏ yêu cầu mã vận đơn khi chuyển trạng thái sang SHIPPED cho đơn hàng online
        
        return null; // Không có lỗi
    }
    
    /**
     * Kiểm tra loại đơn hàng có hợp lệ hay không
     */
    private boolean isValidOrderType(String orderType) {
        return "ONLINE".equals(orderType) || "COUNTER".equals(orderType);
    }
    
    /**
     * Thực hiện các tác động nghiệp vụ khi chuyển đổi trạng thái
     */
    private OrderStatusTransitionResponse.BusinessImpactSummary executeStatusTransition(
            Order order, OrderStatusTransitionRequest request) {
        
        OrderStatusTransitionResponse.BusinessImpactSummary.BusinessImpactSummaryBuilder impactBuilder = 
            OrderStatusTransitionResponse.BusinessImpactSummary.builder();
        
        User user = order.getUser();
        OrderStatus newStatus = request.getNewStatus();
        
        // XỬ LÝ ĐIỂM TÍCH LŨY
        OrderStatusTransitionResponse.BusinessImpactSummary.PointImpact pointImpact = 
            handlePointImpact(order, user, newStatus);
        impactBuilder.pointImpact(pointImpact);
        
        // XỬ LÝ KHO HÀNG
        OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact stockImpact = 
            handleStockImpact(order, newStatus);
        impactBuilder.stockImpact(stockImpact);
        
        // XỬ LÝ VOUCHER
        OrderStatusTransitionResponse.BusinessImpactSummary.VoucherImpact voucherImpact = 
            handleVoucherImpact(order, newStatus);
        impactBuilder.voucherImpact(voucherImpact);
        
        return impactBuilder.build();
    }
    
    /**
     * Xử lý tác động lên điểm tích lũy
     */
    private OrderStatusTransitionResponse.BusinessImpactSummary.PointImpact handlePointImpact(
            Order order, User user, OrderStatus newStatus) {
        
        OrderStatusTransitionResponse.BusinessImpactSummary.PointImpact.PointImpactBuilder builder = 
            OrderStatusTransitionResponse.BusinessImpactSummary.PointImpact.builder();
        
        try {
            switch (newStatus) {
                case DELIVERED:
                    // Tích điểm khi giao thành công
                    pointManagementService.earnPointsFromOrder(order, user);
                    int earnedPoints = pointManagementService.calculateEarnedPoints(order.getTotalAmount(), user);
                    builder.pointsEarned(earnedPoints)
                           .description("Tích " + earnedPoints + " điểm từ đơn hàng " + order.getCode());
                    break;
                    
                case CANCELED:
                case RETURNED:
                    // Trừ điểm khi hủy/trả hàng (nếu đã tích điểm trước đó)
                    pointManagementService.deductPointsFromCancelledOrder(order, user);
                    int earnedPointsBefore = pointManagementService.calculateEarnedPoints(order.getTotalAmount(), user);
                    builder.pointsDeducted(earnedPointsBefore)
                           .description("Trừ " + earnedPointsBefore + " điểm do hủy/trả đơn hàng " + order.getCode());
                    break;
                    
                default:
                    builder.pointsEarned(0)
                           .pointsDeducted(0)
                           .description("Không có thay đổi điểm tích lũy");
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý điểm tích lũy: {}", e.getMessage(), e);
            builder.pointsEarned(0).pointsDeducted(0).description("Lỗi xử lý điểm tích lũy");
        }
        
        return builder.build();
    }
    
    /**
     * Xử lý tác động lên kho hàng
     */
    private OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact handleStockImpact(
            Order order, OrderStatus newStatus) {
        
        List<OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact.StockAdjustment> adjustments = 
            new ArrayList<>();
        
        try {
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
            
            for (OrderDetail detail : orderDetails) {
                Book book = detail.getBook();
                Integer quantity = detail.getQuantity();
                
                switch (newStatus) {
                    case CONFIRMED:
                        // Đặt trước hàng trong kho
                        adjustments.add(OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact.StockAdjustment.builder()
                            .bookId(book.getId())
                            .bookTitle(book.getBookName())
                            .quantityAdjusted(quantity)
                            .adjustmentType("RESERVED")
                            .build());
                        break;
                        
                    case DELIVERED:
                        // ✅ CỘNG SOLD COUNT KHI GIAO THÀNH CÔNG
                        if (detail.getFlashSaleItem() != null) {
                            // Flash sale item
                            FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                            flashSaleItem.setSoldCount(flashSaleItem.getSoldCount() + quantity);
                            flashSaleItemRepository.save(flashSaleItem);
                            
                            // ✅ CỘNG SOLD COUNT CHO BOOK GỐC LUÔN
                            book.setSoldCount(book.getSoldCount() + quantity);
                            bookRepository.save(book);
                        } else {
                            // ✅ SỬA LỖI: Cộng sold count cho book thông thường
                            book.setSoldCount(book.getSoldCount() + quantity);
                            bookRepository.save(book);
                        }
                        adjustments.add(OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact.StockAdjustment.builder()
                            .bookId(book.getId())
                            .bookTitle(book.getBookName())
                            .quantityAdjusted(quantity)
                            .adjustmentType("SOLD_COUNT_INCREASED")
                            .build());
                        break;
                        
                    case CANCELED:
                    case RETURNED:
                        // ✅ LOGIC HOÀN TRẢ: CHỈ trừ sold count nếu trước đó đã DELIVERED
                        if (detail.getFlashSaleItem() != null) {
                            FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                            // Chỉ trừ sold count nếu đơn đã delivered trước đó
                            if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                                flashSaleItem.setSoldCount(Math.max(0, flashSaleItem.getSoldCount() - quantity));
                                flashSaleItemRepository.save(flashSaleItem);
                            }
                            // Khôi phục stock
                            flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + quantity);
                            flashSaleItemRepository.save(flashSaleItem);
                        } else {
                            // ✅ SỬA LỖI: Trừ sold count cho book thông thường (chỉ khi đã delivered)
                            if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                                book.setSoldCount(Math.max(0, book.getSoldCount() - quantity));
                            }
                            // Khôi phục stock
                            book.setStockQuantity(book.getStockQuantity() + quantity);
                            bookRepository.save(book);
                        }
                        adjustments.add(OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact.StockAdjustment.builder()
                            .bookId(book.getId())
                            .bookTitle(book.getBookName())
                            .quantityAdjusted(quantity)
                            .adjustmentType("RELEASED")
                            .build());
                        break;
                    
                    default:
                        // Không có thay đổi kho hàng cho các trạng thái khác
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý kho hàng: {}", e.getMessage(), e);
        }
        
        return OrderStatusTransitionResponse.BusinessImpactSummary.StockImpact.builder()
            .adjustments(adjustments)
            .build();
    }
    
    /**
     * Xử lý tác động lên voucher
     */
    private OrderStatusTransitionResponse.BusinessImpactSummary.VoucherImpact handleVoucherImpact(
            Order order, OrderStatus newStatus) {
        
        OrderStatusTransitionResponse.BusinessImpactSummary.VoucherImpact.VoucherImpactBuilder builder = 
            OrderStatusTransitionResponse.BusinessImpactSummary.VoucherImpact.builder();
        
        try {
            switch (newStatus) {
                case CANCELED:
                    // Không hoàn voucher khi hủy đơn (theo yêu cầu)
                    voucherManagementService.refundVouchersFromCancelledOrder(order);
                    builder.vouchersUsed(0)
                           .vouchersRefunded(0)
                           .totalDiscountImpacted(order.getDiscountAmount())
                           .description("Đơn hàng đã hủy - voucher KHÔNG được hoàn lại");
                    break;
                    
                case RETURNED:
                case REFUNDED:
                    // Hoàn voucher khi trả hàng
                    voucherManagementService.refundVouchersFromReturnedOrder(order);
                    builder.vouchersUsed(0)
                           .vouchersRefunded(order.getRegularVoucherCount() + order.getShippingVoucherCount())
                           .totalDiscountImpacted(order.getDiscountAmount().add(order.getDiscountShipping()))
                           .description("Đã hoàn lại voucher do trả hàng");
                    break;
                    
                default:
                    builder.vouchersUsed(0)
                           .vouchersRefunded(0)
                           .totalDiscountImpacted(java.math.BigDecimal.ZERO)
                           .description("Không có thay đổi voucher");
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý voucher: {}", e.getMessage(), e);
            builder.vouchersUsed(0).vouchersRefunded(0)
                   .totalDiscountImpacted(java.math.BigDecimal.ZERO)
                   .description("Lỗi xử lý voucher");
        }
        
        return builder.build();
    }
}
