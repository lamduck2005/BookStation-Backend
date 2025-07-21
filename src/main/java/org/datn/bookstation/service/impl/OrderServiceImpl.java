package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.request.OrderDetailRefundRequest;
import org.datn.bookstation.dto.request.RefundRequestDto;
import org.datn.bookstation.dto.request.AdminRefundDecisionDto;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.exception.BusinessException;
import org.datn.bookstation.mapper.OrderResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.PointManagementService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final AddressRepository addressRepository;
    private final PointManagementService pointManagementService;
    private final OrderResponseMapper orderResponseMapper;
    private final VoucherCalculationService voucherCalculationService;

    @Override
    public Optional<Integer> findIdByCode(String code) {
        return orderRepository.findIdByCode(code);
    }

    @Override
    public PaginationResponse<OrderResponse> getAllWithPagination(int page, int size, String code,
            Integer userId, OrderStatus orderStatus, String orderType, Long startDate, Long endDate) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (code != null && !code.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), 
                    "%" + code.toLowerCase() + "%"));
            }
            
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            
            if (orderStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"), orderStatus));
            }
            
            if (orderType != null && !orderType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("orderType")), 
                    "%" + orderType.toLowerCase() + "%"));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
            .map(orderResponseMapper::toResponse)
            .toList();
        
        return PaginationResponse.<OrderResponse>builder()
            .content(orderResponses)
            .pageNumber(page)
            .pageSize(size)
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
        return orderRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Override
    public OrderResponse getByIdWithDetails(Integer id) {
        Order order = getById(id);
        return orderResponseMapper.toResponse(order);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> create(OrderRequest request) {
        // Validate user
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        // Validate order type - CHỈ CHO PHÉP "ONLINE" và "COUNTER"
        if (!"ONLINE".equalsIgnoreCase(request.getOrderType()) && 
            !"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Kiểu đơn hàng chỉ được phép là 'ONLINE' hoặc 'COUNTER'");
        }

        // Validate address
        Address address = addressRepository.findById(request.getAddressId())
            .orElseThrow(() -> new BusinessException("Không tìm thấy địa chỉ với ID: " + request.getAddressId()));

        // ✅ BACKEND TỰ TÍNH TOÁN SUBTOTAL từ orderDetails - KHÔNG TIN FRONTEND
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        for (var detailRequest : request.getOrderDetails()) {
            BigDecimal itemTotal = detailRequest.getUnitPrice().multiply(BigDecimal.valueOf(detailRequest.getQuantity()));
            calculatedSubtotal = calculatedSubtotal.add(itemTotal);
        }

        // ✅ TỰ TÍNH VOUCHER DISCOUNT (nếu có)
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountShipping = BigDecimal.ZERO;
        
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            // Tạo order tạm để tính voucher
            Order tempOrder = new Order();
            tempOrder.setSubtotal(calculatedSubtotal);
            tempOrder.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
            
            try {
                VoucherCalculationService.VoucherCalculationResult voucherResult = voucherCalculationService.calculateVoucherDiscount(tempOrder, request.getVoucherIds(), request.getUserId());
                discountAmount = voucherResult.getTotalProductDiscount();
                discountShipping = voucherResult.getTotalShippingDiscount();
            } catch (Exception e) {
                throw new BusinessException("Lỗi tính toán voucher: " + e.getMessage());
            }
        }

        // ✅ TỰ TÍNH TOTAL AMOUNT - KHÔNG TIN FRONTEND
        BigDecimal shippingFee = request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO;
        BigDecimal calculatedTotalAmount = calculatedSubtotal.add(shippingFee).subtract(discountAmount).subtract(discountShipping);
        calculatedTotalAmount = calculatedTotalAmount.max(BigDecimal.ZERO); // Không âm

        // Create order
        Order order = new Order();
        order.setCode(generateOrderCode());
        order.setUser(user);
        order.setAddress(address);
        order.setOrderType(request.getOrderType().toUpperCase());
        order.setOrderStatus(request.getOrderStatus());
        order.setOrderDate(System.currentTimeMillis());
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setDiscountShipping(discountShipping);
        order.setSubtotal(calculatedSubtotal); // ✅ Dùng giá trị backend tính
        order.setTotalAmount(calculatedTotalAmount); // ✅ Dùng giá trị backend tính  
        order.setNotes(request.getNotes());
        order.setCreatedBy(user.getId());

        if (request.getStaffId() != null) {
            User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên với ID: " + request.getStaffId()));
            order.setStaff(staff);
        }

        order = orderRepository.save(order);

        // Create order details
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (var detailRequest : request.getOrderDetails()) {
            Book book = bookRepository.findById(detailRequest.getBookId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy sách với ID: " + detailRequest.getBookId()));

            // Check and handle flash sale items
            FlashSaleItem flashSaleItem = null;
            if (detailRequest.getFlashSaleItemId() != null) {
                flashSaleItem = flashSaleItemRepository.findById(detailRequest.getFlashSaleItemId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy flash sale item với ID: " + detailRequest.getFlashSaleItemId()));
                
                // Validate flash sale stock
                if (flashSaleItem.getStockQuantity() < detailRequest.getQuantity()) {
                    throw new BusinessException("Không đủ số lượng flash sale cho sản phẩm: " + book.getBookName());
                }
                
                // ✅ CHÍNH SÁCH MỚI: CHỈ TRỪ STOCK, CHƯA CỘNG SOLD COUNT
                // sold count sẽ được cộng khi đơn hàng DELIVERED
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() - detailRequest.getQuantity());
                // REMOVED: flashSaleItem.setSoldCount(...) - Sẽ cộng khi DELIVERED
                flashSaleItemRepository.save(flashSaleItem);
            } else {
                // ✅ FIX: Validate và trừ tồn kho sách thông thường
                if (book.getStockQuantity() < detailRequest.getQuantity()) {
                    throw new BusinessException("Không đủ số lượng tồn kho cho sản phẩm: " + book.getBookName() + 
                        " (Tồn kho: " + book.getStockQuantity() + ", Yêu cầu: " + detailRequest.getQuantity() + ")");
                }
                
                // ✅ CHÍNH SÁCH MỚI: CHỈ TRỪ STOCK, CHƯA CỘNG SOLD COUNT  
                // sold count sẽ được cộng khi đơn hàng DELIVERED
                book.setStockQuantity(book.getStockQuantity() - detailRequest.getQuantity());
                // REMOVED: book.setSoldCount(...) - Sẽ cộng khi DELIVERED
                bookRepository.save(book);
            }

            OrderDetail orderDetail = new OrderDetail();
            OrderDetailId orderDetailId = new OrderDetailId();
            orderDetailId.setOrderId(order.getId());
            orderDetailId.setBookId(book.getId());
            orderDetail.setId(orderDetailId);
            orderDetail.setOrder(order);
            orderDetail.setBook(book);
            orderDetail.setFlashSaleItem(flashSaleItem);
            orderDetail.setQuantity(detailRequest.getQuantity());
            orderDetail.setUnitPrice(detailRequest.getUnitPrice());
            orderDetail.setCreatedBy(order.getUser().getId());

            orderDetails.add(orderDetail);
        }

        orderDetailRepository.saveAll(orderDetails);

        // ✅ CẬP NHẬT VOUCHER USAGE (nếu có sử dụng voucher)
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            try {
                voucherCalculationService.updateVoucherUsage(request.getVoucherIds(), request.getUserId());
                
                // Update voucher count trong order theo số lượng discount đã tính
                order.setRegularVoucherCount(discountAmount.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
                order.setShippingVoucherCount(discountShipping.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
                orderRepository.save(order);
                
            } catch (Exception e) {
                log.warn("Không thể cập nhật voucher usage: {}", e.getMessage());
            }
        }

        OrderResponse response = orderResponseMapper.toResponse(order);
        return new ApiResponse<>(HttpStatus.CREATED.value(), "Tạo đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> update(OrderRequest request, Integer id) {
        Order existingOrder = getById(id);
        
        // Only allow updates for PENDING orders
        if (existingOrder.getOrderStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Chỉ có thể cập nhật đơn hàng ở trạng thái PENDING");
        }

        // Validate order type
        if (!"ONLINE".equalsIgnoreCase(request.getOrderType()) && 
            !"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Kiểu đơn hàng chỉ được phép là 'ONLINE' hoặc 'COUNTER'");
        }

        // Update order fields
        existingOrder.setOrderType(request.getOrderType().toUpperCase());
        existingOrder.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
        existingOrder.setSubtotal(request.getSubtotal() != null ? request.getSubtotal() : BigDecimal.ZERO);
        existingOrder.setTotalAmount(request.getTotalAmount());
        existingOrder.setNotes(request.getNotes());
        existingOrder.setUpdatedBy(request.getUserId());

        Order updatedOrder = orderRepository.save(existingOrder);
        OrderResponse response = orderResponseMapper.toResponse(updatedOrder);
        
        return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> updateStatus(Integer id, OrderStatus newStatus, Integer staffId) {
        Order order = getById(id);
        OrderStatus oldStatus = order.getOrderStatus();
        
        // Update status
        order.setOrderStatus(newStatus);
        
        if (staffId != null) {
            User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên với ID: " + staffId));
            order.setStaff(staff);
            order.setUpdatedBy(staffId);
        }

        // Handle business logic based on status change
        handleStatusChangeBusinessLogic(order, oldStatus, newStatus);

        Order updatedOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(updatedOrder);
        
        return new ApiResponse<>(HttpStatus.OK.value(), "Cập nhật trạng thái đơn hàng thành công", response);
    }

    @Override
    public void delete(Integer id) {
        Order order = getById(id);
        
        // Only allow deletion of PENDING or CANCELED orders
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CANCELED) {
            throw new BusinessException("Chỉ có thể xóa đơn hàng ở trạng thái PENDING hoặc CANCELED");
        }
        
        orderRepository.delete(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUser(Integer userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
            .map(orderResponseMapper::toResponse)
            .toList();
    }

    @Override
    public PaginationResponse<OrderResponse> getOrdersByUserWithPagination(Integer userId, int page, int size) {
        userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + userId));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Specification<Order> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user").get("id"), userId);
            
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);
        
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
            .map(orderResponseMapper::toResponse)
            .toList();
        
        return PaginationResponse.<OrderResponse>builder()
            .content(orderResponses)
            .pageNumber(page)
            .pageSize(size)
            .totalElements(orderPage.getTotalElements())
            .totalPages(orderPage.getTotalPages())
            .build();
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByOrderStatusOrderByCreatedAtDesc(status);
        return orders.stream()
            .map(orderResponseMapper::toResponse)
            .toList();
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> cancelOrder(Integer id, String reason, Integer userId) {
        Order order = getById(id);
        
        // Validate that order can be canceled
        if (order.getOrderStatus() == OrderStatus.DELIVERED || 
            order.getOrderStatus() == OrderStatus.CANCELED ||
            order.getOrderStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        // Validate user authorization
        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền hủy đơn hàng này");
        }

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELED);
        order.setCancelReason(reason);
        order.setUpdatedBy(userId);

        // Handle cancellation business logic
        handleCancellationBusinessLogic(order, oldStatus);

        Order canceledOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(canceledOrder);
        
        return new ApiResponse<>(HttpStatus.OK.value(), "Hủy đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> partialRefund(Integer orderId, Integer userId, String reason, 
            List<OrderDetailRefundRequest> refundDetails) {
        
        Order order = getById(orderId);
        
        // Validate order status - only allow refund for DELIVERED orders
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Chỉ có thể hoàn trả đơn hàng đã giao");
        }

        // Validate user authorization
        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền hoàn trả đơn hàng này");
        }

        // Calculate refund amount and update order details
        BigDecimal totalRefundAmount = handlePartialRefundBusinessLogic(order, refundDetails, reason);

        // Update order status
        order.setOrderStatus(OrderStatus.PARTIALLY_REFUNDED);
        order.setCancelReason(reason);
        order.setUpdatedBy(userId);

        // Deduct points if customer earned points from this order
        pointManagementService.deductPointsFromPartialRefund(totalRefundAmount, order, order.getUser());

        Order refundedOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(refundedOrder);
        
        return new ApiResponse<>(HttpStatus.OK.value(), "Hoàn trả một phần đơn hàng thành công", response);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> fullRefund(Integer orderId, Integer userId, String reason) {
        
        Order order = getById(orderId);
        
        // Validate order status
        if (order.getOrderStatus() != OrderStatus.DELIVERED && order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("Chỉ có thể hoàn trả đơn hàng đã giao hoặc đang vận chuyển");
        }

        // Validate user authorization
        if (userId != null && !order.getUser().getId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền hoàn trả đơn hàng này");
        }

        // Handle full refund business logic
        handleFullRefundBusinessLogic(order, reason);

        // Update order status
        order.setOrderStatus(OrderStatus.REFUNDED);
        order.setCancelReason(reason);
        order.setUpdatedBy(userId);

        // Deduct all points earned from this order
        pointManagementService.deductPointsFromCancelledOrder(order, order.getUser());

        Order refundedOrder = orderRepository.save(order);
        OrderResponse response = orderResponseMapper.toResponse(refundedOrder);
        
        return new ApiResponse<>(HttpStatus.OK.value(), "Hoàn trả toàn bộ đơn hàng thành công", response);
    }

    // ================== PRIVATE HELPER METHODS ==================

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }

    private void handleStatusChangeBusinessLogic(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        User user = order.getUser();
        
        switch (newStatus) {
            case CONFIRMED:
                if (oldStatus == OrderStatus.PENDING) {
                    log.info("Order {} confirmed", order.getCode());
                }
                break;
                
            case SHIPPED:
                if (oldStatus == OrderStatus.CONFIRMED) {
                    log.info("Order {} shipped", order.getCode());
                }
                break;
                
            case DELIVERED:
                // ✅ CHÍNH THỨC CỘNG SỐ LƯỢNG ĐÃ BÁN KHI GIAO THÀNH CÔNG
                handleDeliveredBusinessLogic(order);
                
                // ✅ Award points khi đơn hàng DELIVERED (không chỉ từ SHIPPED)
                // Đảm bảo chỉ tích điểm 1 lần
                pointManagementService.earnPointsFromOrder(order, user);
                log.info("Order {} delivered successfully, sold count updated, points awarded", order.getCode());
                break;
                
            case DELIVERY_FAILED:
                // ✅ KHÔI PHỤC STOCK KHI GIAO HÀNG THẤT BẠI
                handleDeliveryFailedBusinessLogic(order, oldStatus);
                log.info("Order {} delivery failed, stock restored", order.getCode());
                break;
                
            case CANCELED:
                handleCancellationBusinessLogic(order, oldStatus);
                break;
                
            default:
                log.info("Order {} status changed from {} to {}", order.getCode(), oldStatus, newStatus);
        }
    }

    private void handleCancellationBusinessLogic(Order order, OrderStatus oldStatus) {
        // ✅ Restore stock for canceled orders - cả book thông thường và flash sale
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                // ✅ CHỈ restore flash sale stock (không cần trừ sold count)
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + detail.getQuantity());
                // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
                flashSaleItemRepository.save(flashSaleItem);
            } else {
                // ✅ CHỈ restore book stock (không cần trừ sold count)
                Book book = detail.getBook();
                book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
                // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
                bookRepository.save(book);
            }
        }

        // Restore voucher usage if applicable
        if (order.getRegularVoucherCount() > 0 || order.getShippingVoucherCount() > 0) {
            // Would need voucher restoration logic here
            log.info("Order {} canceled, voucher usage should be restored", order.getCode());
        }

        log.info("Order {} canceled, stock restored", order.getCode());
    }

    /**
     * ✅ LOGIC NGHIỆP VỤ KHI ĐƠN HÀNG ĐƯỢC GIAO THÀNH CÔNG
     * - CHÍNH THỨC cộng số lượng đã bán cho cả Book và FlashSaleItem
     * - Chỉ gọi khi chuyển sang DELIVERED
     */
    private void handleDeliveredBusinessLogic(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                // ✅ Cộng sold count cho flash sale item
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                int currentSoldCount = flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0;
                flashSaleItem.setSoldCount(currentSoldCount + detail.getQuantity());
                flashSaleItemRepository.save(flashSaleItem);
                
                log.info("FlashSaleItem {} sold count updated: +{} = {}", 
                    flashSaleItem.getId(), detail.getQuantity(), flashSaleItem.getSoldCount());
            }
            
            // ✅ Cộng sold count cho book (cả flash sale và regular)
            Book book = detail.getBook();
            int currentBookSoldCount = book.getSoldCount() != null ? book.getSoldCount() : 0;
            book.setSoldCount(currentBookSoldCount + detail.getQuantity());
            bookRepository.save(book);
            
            log.info("Book {} sold count updated: +{} = {}", 
                book.getId(), detail.getQuantity(), book.getSoldCount());
        }
        
        log.info("Order {} delivered successfully, all sold counts updated", order.getCode());
    }

    /**
     * ✅ LOGIC NGHIỆP VỤ KHI GIAO HÀNG THẤT BẠI
     * - Khôi phục stock về số lượng ban đầu (vì khi tạo đơn đã trừ stock)
     * - KHÔNG cần trừ sold count (vì khi tạo đơn chưa cộng sold count)
     */
    private void handleDeliveryFailedBusinessLogic(Order order, OrderStatus oldStatus) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                // ✅ CHỈ restore flash sale stock (không cần trừ sold count)
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + detail.getQuantity());
                // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
                flashSaleItemRepository.save(flashSaleItem);
                
                log.info("FlashSaleItem {} stock restored: +{}", 
                    flashSaleItem.getId(), detail.getQuantity());
            }
            
            // ✅ CHỈ restore book stock (không cần trừ sold count)
            Book book = detail.getBook();
            book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
            // KHÔNG cần trừ sold count vì khi tạo đơn chưa cộng
            bookRepository.save(book);
            
            log.info("Book {} stock restored: +{}", 
                book.getId(), detail.getQuantity());
        }
        
        log.info("Order {} delivery failed, stock restored", order.getCode());
    }

    private BigDecimal handlePartialRefundBusinessLogic(Order order, List<OrderDetailRefundRequest> refundDetails, String reason) {
        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        
        for (OrderDetailRefundRequest refundDetail : refundDetails) {
            // Find order detail by orderId and bookId
            OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(order.getId(), refundDetail.getBookId());
            if (orderDetail == null) {
                throw new BusinessException("Không tìm thấy chi tiết đơn hàng cho sách ID: " + refundDetail.getBookId());
            }
            
            // Validate refund quantity
            if (refundDetail.getRefundQuantity() > orderDetail.getQuantity()) {
                throw new BusinessException("Số lượng hoàn trả vượt quá số lượng đã mua");
            }
            
            // Calculate refund amount for this detail
            BigDecimal unitRefundAmount = orderDetail.getUnitPrice();
            BigDecimal detailRefundAmount = unitRefundAmount.multiply(BigDecimal.valueOf(refundDetail.getRefundQuantity()));
            totalRefundAmount = totalRefundAmount.add(detailRefundAmount);
            
            // ✅ Restore stock và CHỈ trừ sold count nếu đơn hàng đã DELIVERED
            boolean wasDelivered = order.getOrderStatus() == OrderStatus.DELIVERED || 
                                   order.getOrderStatus() == OrderStatus.REFUNDING ||
                                   order.getOrderStatus() == OrderStatus.REFUNDED ||
                                   order.getOrderStatus() == OrderStatus.PARTIALLY_REFUNDED;
            
            if (orderDetail.getFlashSaleItem() != null) {
                FlashSaleItem flashSaleItem = orderDetail.getFlashSaleItem();
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + refundDetail.getRefundQuantity());
                
                // CHỈ trừ sold count nếu đơn hàng đã được giao (đã cộng sold count)
                if (wasDelivered) {
                    flashSaleItem.setSoldCount((flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0) - refundDetail.getRefundQuantity());
                }
                flashSaleItemRepository.save(flashSaleItem);
            } else {
                // ✅ Restore book stock  
                Book book = orderDetail.getBook();
                book.setStockQuantity(book.getStockQuantity() + refundDetail.getRefundQuantity());
                
                // CHỈ trừ sold count nếu đơn hàng đã được giao (đã cộng sold count)
                if (wasDelivered) {
                    book.setSoldCount((book.getSoldCount() != null ? book.getSoldCount() : 0) - refundDetail.getRefundQuantity());
                }
                bookRepository.save(book);
            }
            
            // Update order detail quantity
            orderDetail.setQuantity(orderDetail.getQuantity() - refundDetail.getRefundQuantity());
            orderDetailRepository.save(orderDetail);
        }
        
        return totalRefundAmount;
    }

    private void handleFullRefundBusinessLogic(Order order, String reason) {
        // ✅ Restore stock và CHỈ trừ sold count nếu đơn hàng đã DELIVERED
        boolean wasDelivered = order.getOrderStatus() == OrderStatus.DELIVERED || 
                               order.getOrderStatus() == OrderStatus.REFUNDING ||
                               order.getOrderStatus() == OrderStatus.REFUNDED;
        
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlashSaleItem() != null) {
                FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() + detail.getQuantity());
                
                // CHỈ trừ sold count nếu đơn hàng đã được giao (đã cộng sold count)
                if (wasDelivered) {
                    flashSaleItem.setSoldCount((flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0) - detail.getQuantity());
                }
                flashSaleItemRepository.save(flashSaleItem);
            } else {
                // ✅ Restore book stock
                Book book = detail.getBook();
                book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
                
                // CHỈ trừ sold count nếu đơn hàng đã được giao (đã cộng sold count)
                if (wasDelivered) {
                    book.setSoldCount((book.getSoldCount() != null ? book.getSoldCount() : 0) - detail.getQuantity());
                }
                bookRepository.save(book);
            }
        }

        // Restore voucher usage if applicable
        if (order.getRegularVoucherCount() > 0 || order.getShippingVoucherCount() > 0) {
            // Would need voucher restoration logic here
            log.info("Order {} fully refunded, voucher usage should be restored", order.getCode());
        }

        log.info("Order {} fully refunded, all stock restored", order.getCode());
    }
    
    /**
     * ✅ THÊM MỚI: Khách hàng gửi yêu cầu hoàn trả
     */
    @Override
    @Transactional
    public ApiResponse<OrderResponse> requestRefund(Integer orderId, RefundRequestDto refundRequest) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + orderId));
            
            // Kiểm tra quyền truy cập
            if (!order.getUser().getId().equals(refundRequest.getUserId().intValue())) {
                throw new BusinessException("Bạn không có quyền hoàn trả đơn hàng này");
            }
            
            // Kiểm tra trạng thái đơn hàng
            if (order.getOrderStatus() != OrderStatus.DELIVERED) {
                throw new BusinessException("Chỉ có thể yêu cầu hoàn trả đơn hàng đã giao thành công");
            }
            
            // Cập nhật trạng thái đơn hàng
            order.setOrderStatus(OrderStatus.REFUND_REQUESTED);
            order.setCancelReason(refundRequest.getReason());
            order.setUpdatedBy(refundRequest.getUserId().intValue());
            orderRepository.save(order);
            
            // Lưu thông tin chi tiết yêu cầu hoàn trả vào order_detail (evidence)
            if (refundRequest.getRefundDetails() != null) {
                for (OrderDetailRefundRequest detail : refundRequest.getRefundDetails()) {
                    OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(orderId, detail.getBookId());
                    if (orderDetail == null) {
                        throw new BusinessException("Không tìm thấy sản phẩm trong đơn hàng");
                    }
                    
                    // Lưu evidence vào order detail (có thể cần thêm field mới trong OrderDetail)
                    // Hoặc tạo bảng riêng để lưu refund request details
                }
            }
            
            log.info("Customer {} requested refund for order {}", refundRequest.getUserId(), order.getCode());
            
            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Yêu cầu hoàn trả đã được gửi thành công. Admin sẽ xem xét và phản hồi sớm nhất.",
                response
            );
                
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error requesting refund for order {}: {}", orderId, e.getMessage(), e);
            throw new BusinessException("Có lỗi xảy ra khi gửi yêu cầu hoàn trả: " + e.getMessage());
        }
    }
    
    /**
     * ✅ THÊM MỚI: Admin chấp nhận yêu cầu hoàn trả
     */
    @Override
    @Transactional
    public ApiResponse<OrderResponse> approveRefundRequest(AdminRefundDecisionDto decision) {
        try {
            Order order = orderRepository.findById(decision.getOrderId().intValue())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + decision.getOrderId()));
            
            // Kiểm tra trạng thái đơn hàng
            if (order.getOrderStatus() != OrderStatus.REFUND_REQUESTED) {
                throw new BusinessException("Đơn hàng không ở trạng thái chờ xem xét hoàn trả");
            }
            
            // Admin chấp nhận -> chuyển sang REFUNDING và thực hiện hoàn trả
            order.setOrderStatus(OrderStatus.REFUNDING);
            order.setUpdatedBy(decision.getAdminId().intValue());
            orderRepository.save(order);
            
            // Thực hiện logic hoàn trả toàn bộ
            handleFullRefundBusinessLogic(order, decision.getAdminNotes());
            
            // Trừ điểm khách hàng
            if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                // TODO: Implement deductPointsForRefund method in PointManagementService
                log.info("Should deduct points for user {} amount {}", order.getUser().getId(), order.getTotalAmount());
            }
            
            // Chuyển sang trạng thái REFUNDED
            order.setOrderStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            
            log.info("Admin {} approved refund for order {}", decision.getAdminId(), order.getCode());
            
            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Yêu cầu hoàn trả đã được chấp nhận và xử lý thành công",
                response
            );
                
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error approving refund for order {}: {}", decision.getOrderId(), e.getMessage(), e);
            throw new BusinessException("Có lỗi xảy ra khi chấp nhận hoàn trả: " + e.getMessage());
        }
    }
    
    /**
     * ✅ THÊM MỚI: Admin từ chối yêu cầu hoàn trả
     */
    @Override
    @Transactional
    public ApiResponse<OrderResponse> rejectRefundRequest(AdminRefundDecisionDto decision) {
        try {
            Order order = orderRepository.findById(decision.getOrderId().intValue())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đơn hàng với ID: " + decision.getOrderId()));
            
            // Kiểm tra trạng thái đơn hàng
            if (order.getOrderStatus() != OrderStatus.REFUND_REQUESTED) {
                throw new BusinessException("Đơn hàng không ở trạng thái chờ xem xét hoàn trả");
            }
            
            // Admin từ chối -> chuyển về DELIVERED
            order.setOrderStatus(OrderStatus.DELIVERED);
            order.setCancelReason(decision.getAdminNotes()); // Lưu lý do từ chối
            order.setUpdatedBy(decision.getAdminId().intValue());
            orderRepository.save(order);
            
            log.info("Admin {} rejected refund for order {}", decision.getAdminId(), order.getCode());
            
            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Yêu cầu hoàn trả đã bị từ chối",
                response
            );
                
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting refund for order {}: {}", decision.getOrderId(), e.getMessage(), e);
            throw new BusinessException("Có lỗi xảy ra khi từ chối hoàn trả: " + e.getMessage());
        }
    }

    @Override
    public OrderResponse getOrderDetailById(Integer id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return null;
        // Lấy danh sách sản phẩm và voucher của đơn hàng
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
        List<org.datn.bookstation.entity.OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(id);
        // Map sang DTO
        OrderResponse response = orderResponseMapper.toResponseWithDetails(order, orderDetails, orderVouchers);
        return response;
    }
}
