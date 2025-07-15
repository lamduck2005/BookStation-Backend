package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.request.OrderDetailRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.mapper.OrderMapper;
import org.datn.bookstation.mapper.OrderResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BookRepository bookRepository;
    private final VoucherRepository voucherRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final VoucherCalculationService voucherCalculationService;
    private final FlashSaleService flashSaleService;
    private final OrderMapper orderMapper;
    private final OrderResponseMapper orderResponseMapper;

    @Override
    public Optional<Integer> findIdByCode(String code) {
        return orderRepository.findIdByCode(code);
    }

    @Override
    public PaginationResponse<OrderResponse> getAllWithPagination(int page, int size, String code, 
            Integer userId, OrderStatus orderStatus, String orderType, Long startDate, Long endDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Order> specification = OrderSpecification.filterBy(code, userId, orderStatus, orderType, startDate, endDate);
        Page<Order> orderPage = orderRepository.findAll(specification, pageable);
        
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(orderResponseMapper::toResponse)
                .collect(Collectors.toList());
                
        return PaginationResponse.<OrderResponse>builder()
                .content(orderResponses)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order getById(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public OrderResponse getByIdWithDetails(Integer id) {
        Order order = getById(id);
        if (order == null) return null;
        
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
        List<OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(id);
        
        return orderResponseMapper.toResponseWithDetails(order, orderDetails, orderVouchers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OrderResponse> create(OrderRequest request) {
        try {
            log.info("🛒 Creating order for user: {}", request.getUserId());
            
            // Validate user
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                log.error("❌ User not found: {}", request.getUserId());
                return new ApiResponse<>(404, "Không tìm thấy người dùng", null);
            }
            
            // Validate address
            if (request.getAddressId() == null) {
                log.error("❌ Address ID is null for user: {}", request.getUserId());
                return new ApiResponse<>(400, "Thiếu thông tin địa chỉ giao hàng", null);
            }
            
            Address address = addressRepository.findById(request.getAddressId()).orElse(null);
            if (address == null) {
                log.error("❌ Address not found: {}", request.getAddressId());
                return new ApiResponse<>(404, "Không tìm thấy địa chỉ giao hàng", null);
            }
            
            // Validate order details
            if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
                log.error("❌ No order details provided for user: {}", request.getUserId());
                return new ApiResponse<>(400, "Không có sản phẩm nào để đặt hàng", null);
            }
            
            // Create order
            Order order = orderMapper.toOrder(request);
            order.setUser(user);
            order.setAddress(address);
            
            // Set basic order info from request (not subtotal - will be calculated)
            order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
            
            // Set required fields with default values to avoid validation error on first save
            order.setSubtotal(BigDecimal.ZERO);
            order.setTotalAmount(BigDecimal.ZERO);
            
            // Set staff if provided
            if (request.getStaffId() != null) {
                User staff = userRepository.findById(request.getStaffId()).orElse(null);
                if (staff != null) {
                    order.setStaff(staff);
                }
            }
            
            // Generate unique order code
            String orderCode;
            do {
                orderCode = generateOrderCode();
            } while (orderRepository.existsByCode(orderCode));
            order.setCode(orderCode);
            
            order.setOrderDate(Instant.now().toEpochMilli());
            order.setCreatedBy(request.getUserId());
            order.setStatus((byte) 1); // Active
            
            log.info("🔄 Saving order with code: {}", orderCode);
            Order savedOrder = orderRepository.save(order);
            
            // Create order details with proper price calculation
            BigDecimal calculatedSubtotal = BigDecimal.ZERO;
            log.info("🔄 Processing {} order details", request.getOrderDetails().size());
            
            for (OrderDetailRequest detailRequest : request.getOrderDetails()) {
                try {
                    BigDecimal itemSubtotal = createOrderDetailWithCalculation(savedOrder, detailRequest);
                    calculatedSubtotal = calculatedSubtotal.add(itemSubtotal);
                    log.debug("✅ Processed order detail for book {}: subtotal={}", 
                        detailRequest.getBookId(), itemSubtotal);
                } catch (Exception detailEx) {
                    log.error("❌ Failed to create order detail for book {}: {}", 
                        detailRequest.getBookId(), detailEx.getMessage(), detailEx);
                    throw new RuntimeException("Lỗi khi xử lý sản phẩm ID " + detailRequest.getBookId() + ": " + detailEx.getMessage());
                }
            }
            
            // Update order with calculated subtotal
            savedOrder.setSubtotal(calculatedSubtotal);
            savedOrder.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
            savedOrder.setTotalAmount(calculatedSubtotal.add(savedOrder.getShippingFee()));
            
            log.info("🔄 Calculated subtotal: {}, shipping: {}", calculatedSubtotal, savedOrder.getShippingFee());
            
            // Apply vouchers if provided - validate and calculate discounts
            if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
                try {
                    log.info("🔄 Processing {} vouchers", request.getVoucherIds().size());
                    
                    // Create temporary order for voucher calculation
                    Order tempOrder = new Order();
                    tempOrder.setSubtotal(calculatedSubtotal);
                    tempOrder.setShippingFee(savedOrder.getShippingFee());
                    
                    // Validate and calculate vouchers using VoucherCalculationService
                    VoucherCalculationService.VoucherCalculationResult voucherResult = 
                        voucherCalculationService.calculateVoucherDiscount(tempOrder, request.getVoucherIds(), request.getUserId());
                    
                    // Update order amounts based on voucher calculations
                    savedOrder.setDiscountAmount(voucherResult.getTotalProductDiscount());
                    savedOrder.setDiscountShipping(voucherResult.getTotalShippingDiscount());
                    savedOrder.setRegularVoucherCount(voucherResult.getRegularVoucherCount());
                    savedOrder.setShippingVoucherCount(voucherResult.getShippingVoucherCount());
                    
                    // Recalculate total amount
                    BigDecimal recalculatedTotal = calculatedSubtotal
                        .add(savedOrder.getShippingFee())
                        .subtract(voucherResult.getTotalProductDiscount())
                        .subtract(voucherResult.getTotalShippingDiscount());
                    savedOrder.setTotalAmount(recalculatedTotal);
                    
                    log.info("🔄 Applied vouchers: product discount={}, shipping discount={}, final total={}", 
                        voucherResult.getTotalProductDiscount(), voucherResult.getTotalShippingDiscount(), recalculatedTotal);
                    
                    // Create order vouchers with calculated details
                    for (VoucherCalculationService.VoucherApplicationDetail voucherDetail : voucherResult.getAppliedVouchers()) {
                        createOrderVoucherWithDetails(savedOrder, voucherDetail);
                    }
                    
                    // Update voucher usage
                    voucherCalculationService.updateVoucherUsage(request.getVoucherIds(), request.getUserId());
                    
                } catch (Exception e) {
                    log.error("❌ Error applying vouchers: {}", e.getMessage(), e);
                    throw new RuntimeException("Lỗi áp dụng voucher: " + e.getMessage());
                }
            } else {
                // No vouchers - set default discount values
                savedOrder.setDiscountAmount(BigDecimal.ZERO);
                savedOrder.setDiscountShipping(BigDecimal.ZERO);
                savedOrder.setRegularVoucherCount(0);
                savedOrder.setShippingVoucherCount(0);
            }
            
            // Save updated order
            savedOrder = orderRepository.save(savedOrder);
            
            log.info("✅ Successfully created order: {}", orderCode);
            OrderResponse response = orderResponseMapper.toResponse(savedOrder);
            return new ApiResponse<>(201, "Tạo đơn hàng thành công", response);
            
        } catch (Exception e) {
            log.error("💥 Error creating order for user {}: {}", request.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo đơn hàng: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> update(OrderRequest request, Integer id) {
        Order existing = getById(id);
        if (existing == null) {
            return new ApiResponse<>(404, "Không tìm thấy đơn hàng", null);
        }
        
        // Validate if order can be updated
        if (!canUpdateOrder(existing.getOrderStatus())) {
            return new ApiResponse<>(400, "Chỉ có thể cập nhật đơn hàng ở trạng thái PENDING hoặc CONFIRMED", null);
        }
        
        try {            
            // Update basic info
            existing.setOrderStatus(request.getOrderStatus());
            existing.setOrderType(request.getOrderType());
            existing.setShippingFee(request.getShippingFee());
            existing.setUpdatedBy(request.getUserId());
            
            // Update address if changed
            if (!existing.getAddress().getId().equals(request.getAddressId())) {
                Address newAddress = addressRepository.findById(request.getAddressId()).orElse(null);
                if (newAddress != null) {
                    existing.setAddress(newAddress);
                }
            }
            
            // Recalculate order details and subtotal
            // Note: This is a simplified update. Full implementation should handle
            // updating order details, recalculating vouchers, etc.
            // For now, just save basic updates
            
            Order savedOrder = orderRepository.save(existing);
            OrderResponse response = orderResponseMapper.toResponse(savedOrder);
            return new ApiResponse<>(200, "Cập nhật đơn hàng thành công", response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật đơn hàng: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateStatus(Integer id, OrderStatus newStatus, Integer staffId) {
        Order order = getById(id);
        if (order == null) {
            return new ApiResponse<>(404, "Không tìm thấy đơn hàng", null);
        }
        
        // Validate status transition
        String validationError = validateStatusTransition(order.getOrderStatus(), newStatus);
        if (validationError != null) {
            return new ApiResponse<>(400, validationError, null);
        }
        
        try {
            order.setOrderStatus(newStatus);
            if (staffId != null) {
                User staff = userRepository.findById(staffId).orElse(null);
                if (staff != null) {
                    order.setStaff(staff);
                    order.setUpdatedBy(staffId);
                }
            }
            
            Order savedOrder = orderRepository.save(order);
            OrderResponse response = orderResponseMapper.toResponse(savedOrder);
            return new ApiResponse<>(200, "Cập nhật trạng thái thành công", response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), null);
        }
    }

    @Override
    public void delete(Integer id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(orderResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(orderResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> cancelOrder(Integer id, String reason, Integer userId) {
        Order order = getById(id);
        if (order == null) {
            return new ApiResponse<>(404, "Không tìm thấy đơn hàng", null);
        }
        
        // Validate if order can be canceled
        if (!canCancelOrder(order.getOrderStatus())) {
            return new ApiResponse<>(400, "Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc CONFIRMED", null);
        }
        
        try {
            order.setOrderStatus(OrderStatus.CANCELED);
            order.setUpdatedBy(userId);
            
            Order savedOrder = orderRepository.save(order);
            OrderResponse response = orderResponseMapper.toResponse(savedOrder);
            return new ApiResponse<>(200, "Hủy đơn hàng thành công", response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi hủy đơn hàng: " + e.getMessage(), null);
        }
    }
    
    private String generateOrderCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD" + timestamp.substring(timestamp.length() - 6) + randomPart;
    }
    
    /**
     * Create order detail with proper price calculation for regular and flash sale items
     * ✅ SECURITY FIX: Auto-detect flash sales instead of trusting frontend input
     * ✅ STOCK MANAGEMENT: Update stock quantity after order creation
     * @return subtotal for this order detail (quantity * unit_price)
     */
    private BigDecimal createOrderDetailWithCalculation(Order order, OrderDetailRequest detailRequest) {
        Book book = bookRepository.findById(detailRequest.getBookId()).orElseThrow(
            () -> new RuntimeException("Không tìm thấy sách với ID: " + detailRequest.getBookId())
        );
        
        BigDecimal unitPrice;
        FlashSaleItem flashSaleItem = null;
        
        // ✅ AUTO-DETECT: Tự động phát hiện flash sale thay vì tin frontend
        Optional<FlashSaleItem> activeFlashSaleOpt = flashSaleService.findActiveFlashSaleForBook(book.getId().longValue());
        
        if (activeFlashSaleOpt.isPresent()) {
            flashSaleItem = activeFlashSaleOpt.get();
            
            // Validate flash sale business rules
            validateFlashSaleItem(flashSaleItem, detailRequest.getQuantity(), order.getUser().getId());
            
            // Use flash sale price
            unitPrice = flashSaleItem.getDiscountPrice();
            
            log.info("🔥 AUTO-DETECTED flash sale for book {}: regular={}, flash={}", 
                book.getId(), book.getPrice(), unitPrice);
        } else {
            // Use regular book price
            unitPrice = book.getPrice();
            
            log.info("💰 Using regular price for book {}: {}", book.getId(), unitPrice);
        }
        
        // Validate quantity vs stock for flash sale or regular book
        if (flashSaleItem != null) {
            if (detailRequest.getQuantity() > flashSaleItem.getStockQuantity()) {
                throw new RuntimeException("Flash sale không đủ hàng. Có sẵn: " + flashSaleItem.getStockQuantity());
            }
        } else {
            if (detailRequest.getQuantity() > book.getStockQuantity()) {
                throw new RuntimeException("Số lượng yêu cầu vượt quá tồn kho. Có sẵn: " + book.getStockQuantity());
            }
        }
        
        // ✅ STOCK UPDATE: Trừ stock ngay khi tạo order detail thành công
        if (flashSaleItem != null) {
            // Update flash sale stock
            int newFlashSaleStock = flashSaleItem.getStockQuantity() - detailRequest.getQuantity();
            flashSaleItem.setStockQuantity(newFlashSaleStock);
            
            // ✅ SOLD COUNT UPDATE: Cộng số lượng đã bán flash sale
            int newFlashSaleSoldCount = (flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0) + detailRequest.getQuantity();
            flashSaleItem.setSoldCount(newFlashSaleSoldCount);
            
            flashSaleItem.setUpdatedAt(System.currentTimeMillis());
            flashSaleItem.setUpdatedBy(order.getCreatedBy().longValue());
            flashSaleItemRepository.save(flashSaleItem);
            
            log.info("📦 FLASH SALE UPDATED: Book {} flash sale stock: {} → {}, sold count: {} → {}", 
                book.getId(), 
                flashSaleItem.getStockQuantity() + detailRequest.getQuantity(), newFlashSaleStock,
                newFlashSaleSoldCount - detailRequest.getQuantity(), newFlashSaleSoldCount);
        }
        
        // Always update regular book stock and sold count
        int newBookStock = book.getStockQuantity() - detailRequest.getQuantity();
        book.setStockQuantity(newBookStock);
        
        // ✅ SOLD COUNT UPDATE: Cộng số lượng đã bán book
        int newBookSoldCount = (book.getSoldCount() != null ? book.getSoldCount() : 0) + detailRequest.getQuantity();
        book.setSoldCount(newBookSoldCount);
        
        book.setUpdatedAt(System.currentTimeMillis());
        book.setUpdatedBy(order.getCreatedBy());
        bookRepository.save(book);
        
        log.info("📦 BOOK UPDATED: Book {} regular stock: {} → {}, sold count: {} → {}", 
            book.getId(), 
            book.getStockQuantity() + detailRequest.getQuantity(), newBookStock,
            newBookSoldCount - detailRequest.getQuantity(), newBookSoldCount);
        
        // Create order detail
        OrderDetail orderDetail = new OrderDetail();
        
        // Set composite key
        OrderDetailId detailId = new OrderDetailId();
        detailId.setOrderId(order.getId());
        detailId.setBookId(book.getId());
        orderDetail.setId(detailId);
        
        orderDetail.setOrder(order);
        orderDetail.setBook(book);
        orderDetail.setQuantity(detailRequest.getQuantity());
        orderDetail.setUnitPrice(unitPrice); // Use calculated price
        orderDetail.setCreatedBy(order.getCreatedBy());
        orderDetail.setStatus((byte) 1);
        
        if (flashSaleItem != null) {
            orderDetail.setFlashSaleItem(flashSaleItem);
        }
        
        orderDetailRepository.save(orderDetail);
        
        // Return subtotal for this item
        return unitPrice.multiply(BigDecimal.valueOf(detailRequest.getQuantity()));
    }
    
    /**
     * Validate flash sale item business rules
     */
    private void validateFlashSaleItem(FlashSaleItem flashSaleItem, Integer requestedQuantity, Integer userId) {
        FlashSale flashSale = flashSaleItem.getFlashSale();
        long currentTime = System.currentTimeMillis();
        
        // Check flash sale time validity
        if (currentTime < flashSale.getStartTime() || currentTime > flashSale.getEndTime()) {
            throw new RuntimeException("Flash sale không trong thời gian hiệu lực");
        }
        
        // Check flash sale status
        if (flashSale.getStatus() != 1) {
            throw new RuntimeException("Flash sale không hoạt động");
        }
        
        // Check flash sale item status
        if (flashSaleItem.getStatus() != 1) {
            throw new RuntimeException("Sản phẩm flash sale không hoạt động");
        }
        
        // Check stock quantity
        if (requestedQuantity > flashSaleItem.getStockQuantity()) {
            throw new RuntimeException("Số lượng flash sale không đủ. Có sẵn: " + flashSaleItem.getStockQuantity());
        }
        
        // Kiểm tra giới hạn mua trên mỗi user cho flash sale item
        if (flashSaleItem.getMaxPurchasePerUser() != null) {
            // TODO: Nếu cần, kiểm tra lịch sử mua của user cho sản phẩm này
            if (requestedQuantity > flashSaleItem.getMaxPurchasePerUser()) {
                throw new RuntimeException(
                    "Sản phẩm flash sale chỉ cho phép mua tối đa " + flashSaleItem.getMaxPurchasePerUser() +
                    " sản phẩm trên mỗi user. Bạn đã chọn " + requestedQuantity + " sản phẩm."
                );
            }
        }
    }
    
    private void createOrderVoucherWithDetails(Order order, VoucherCalculationService.VoucherApplicationDetail voucherDetail) {
        Voucher voucher = voucherRepository.findById(voucherDetail.getVoucherId()).orElseThrow(
            () -> new RuntimeException("Không tìm thấy voucher với ID: " + voucherDetail.getVoucherId())
        );
        
        OrderVoucher orderVoucher = new OrderVoucher();
        
        // Set composite key
        OrderVoucherId voucherOrderId = new OrderVoucherId();
        voucherOrderId.setOrderId(order.getId());
        voucherOrderId.setVoucherId(voucher.getId());
        orderVoucher.setId(voucherOrderId);
        
        orderVoucher.setOrder(order);
        orderVoucher.setVoucher(voucher);
        // ✅ FIX: Sử dụng VoucherCategory và DiscountType mới thay vì VoucherType cũ
        orderVoucher.setVoucherCategory(voucherDetail.getVoucherCategory());
        orderVoucher.setDiscountType(voucherDetail.getDiscountType());
        orderVoucher.setDiscountApplied(voucherDetail.getDiscountApplied());
        orderVoucher.setAppliedAt(System.currentTimeMillis());
        
        orderVoucherRepository.save(orderVoucher);
    }
    
    /**
     * Validate order status transition according to business rules
     */
    private String validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Allow any status change for PENDING orders
        if (currentStatus == OrderStatus.PENDING) {
            return null;
        }
        
        // Define valid transitions
        switch (currentStatus) {
            case CONFIRMED:
                if (newStatus != OrderStatus.SHIPPED && 
                    newStatus != OrderStatus.CANCELED && 
                    newStatus != OrderStatus.PENDING) {
                    return "Đơn hàng đã xác nhận chỉ có thể chuyển sang: SHIPPED, CANCELED hoặc PENDING";
                }
                break;
                
            case SHIPPED:
                if (newStatus != OrderStatus.DELIVERED && 
                    newStatus != OrderStatus.RETURNED && 
                    newStatus != OrderStatus.CONFIRMED) {
                    return "Đơn hàng đã giao chỉ có thể chuyển sang: DELIVERED, RETURNED hoặc CONFIRMED";
                }
                break;
                
            case DELIVERED:
                if (newStatus != OrderStatus.RETURNED && 
                    newStatus != OrderStatus.SHIPPED) {
                    return "Đơn hàng đã hoàn thành chỉ có thể chuyển sang: RETURNED hoặc SHIPPED";
                }
                break;
                
            case CANCELED:
                if (newStatus != OrderStatus.PENDING) {
                    return "Đơn hàng đã hủy chỉ có thể khôi phục về PENDING";
                }
                break;
                
            case RETURNED:
                if (newStatus != OrderStatus.DELIVERED && 
                    newStatus != OrderStatus.CANCELED) {
                    return "Đơn hàng đã trả về chỉ có thể chuyển sang: DELIVERED hoặc CANCELED";
                }
                break;
                
            default:
                return "Trạng thái đơn hàng không hợp lệ";
        }
        
        return null; // Valid transition
    }
    
    /**
     * Validate if order can be updated
     */
    private boolean canUpdateOrder(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    /**
     * Validate if order can be canceled
     */
    private boolean canCancelOrder(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
}
