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
import org.datn.bookstation.entity.RefundRequest.RefundStatus;
import org.datn.bookstation.entity.RefundRequest.RefundType;
import org.datn.bookstation.exception.BusinessException;
import org.datn.bookstation.mapper.OrderResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.PointManagementService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.datn.bookstation.service.FlashSaleService;
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
    private final RefundRequestRepository refundRequestRepository;
    private final RefundItemRepository refundItemRepository;
    private final VoucherRepository voucherRepository;
    private final PointManagementService pointManagementService;
    private final OrderResponseMapper orderResponseMapper;
    private final VoucherCalculationService voucherCalculationService;
    private final FlashSaleService flashSaleService;

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
        
        // Lấy order details với đầy đủ thông tin
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(id);
        
        // Lấy order vouchers
        List<OrderVoucher> orderVouchers = orderVoucherRepository.findByOrderId(id);
        
        // Sử dụng mapper với details
        return orderResponseMapper.toResponseWithDetails(order, orderDetails, orderVouchers);
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> create(OrderRequest request) {
        // ✅ MODIFIED: Validate user - allow null for counter sales
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng với ID: " + request.getUserId()));
        } else if (!"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("User ID là bắt buộc cho đơn hàng online");
        }

        // Validate order type - CHỈ CHO PHÉP "ONLINE" và "COUNTER"
        if (!"ONLINE".equalsIgnoreCase(request.getOrderType()) && 
            !"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Kiểu đơn hàng chỉ được phép là 'ONLINE' hoặc 'COUNTER'");
        }

        // ✅ MODIFIED: Validate address - allow null for counter sales
        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy địa chỉ với ID: " + request.getAddressId()));
        } else if (!"COUNTER".equalsIgnoreCase(request.getOrderType())) {
            throw new BusinessException("Address ID là bắt buộc cho đơn hàng online");
        }

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
        
        // ✅ THÊM: Set thông tin người nhận cho đơn hàng tại quầy
        if ("COUNTER".equalsIgnoreCase(request.getOrderType())) {
            order.setRecipientName(request.getRecipientName());
            order.setPhoneNumber(request.getPhoneNumber());
        }
        
        // ✅ FIX: Set createdBy properly for counter sales
        if (user != null) {
            order.setCreatedBy(user.getId()); // Online order - use customer ID
        } else if (request.getStaffId() != null) {
            order.setCreatedBy(request.getStaffId()); // Counter sales - use staff ID
        } else {
            throw new BusinessException("Phải có staffId cho đơn hàng counter sales");
        }

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

            // ✅ ENHANCED: Xử lý logic flash sale và mixed purchase
            FlashSaleItem flashSaleItem = null;
            int quantityToOrder = detailRequest.getQuantity();
            
            if (detailRequest.getFlashSaleItemId() != null) {
                // Trường hợp 1: Frontend đã chỉ định flash sale item
                flashSaleItem = flashSaleItemRepository.findById(detailRequest.getFlashSaleItemId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy flash sale item với ID: " + detailRequest.getFlashSaleItemId()));
                
                // ✅ Validate flash sale purchase limit per user
                if (!flashSaleService.canUserPurchaseMore(flashSaleItem.getId().longValue(), request.getUserId(), quantityToOrder)) {
                    int currentPurchased = flashSaleService.getUserPurchasedQuantity(flashSaleItem.getId().longValue(), request.getUserId());
                    int maxAllowed = flashSaleItem.getMaxPurchasePerUser();
                    throw new BusinessException("Bạn đã mua " + currentPurchased + "/" + maxAllowed + 
                        " sản phẩm flash sale này. Không thể mua thêm " + quantityToOrder + " sản phẩm.");
                }
                
                // Validate flash sale stock
                if (flashSaleItem.getStockQuantity() < quantityToOrder) {
                    throw new BusinessException("Không đủ số lượng flash sale cho sản phẩm: " + book.getBookName() + 
                        " (Flash sale còn: " + flashSaleItem.getStockQuantity() + ", Yêu cầu: " + quantityToOrder + ")");
                }
                
                // Trừ flash sale stock
                flashSaleItem.setStockQuantity(flashSaleItem.getStockQuantity() - quantityToOrder);
                flashSaleItemRepository.save(flashSaleItem);
                
                // Trừ book stock (vì sách flash sale cũng tính vào tổng stock)
                book.setStockQuantity(book.getStockQuantity() - quantityToOrder);
                bookRepository.save(book);
                
            } else {
                // Trường hợp 2: Không chỉ định flash sale - kiểm tra tự động
                // Tìm flash sale active cho book này
                Optional<FlashSaleItem> activeFlashSaleOpt = flashSaleItemRepository
                    .findActiveFlashSalesByBookId(book.getId().longValue(), System.currentTimeMillis())
                    .stream()
                    .findFirst();
                
                if (activeFlashSaleOpt.isPresent()) {
                    FlashSaleItem activeFlashSale = activeFlashSaleOpt.get();
                    int flashSaleStock = activeFlashSale.getStockQuantity();
                    
                    if (flashSaleStock >= quantityToOrder) {
                        // ✅ ENHANCED: Validate flash sale purchase limit với hai loại thông báo
                        if (!flashSaleService.canUserPurchaseMore(activeFlashSale.getId().longValue(), request.getUserId(), quantityToOrder)) {
                            int currentPurchased = flashSaleService.getUserPurchasedQuantity(activeFlashSale.getId().longValue(), request.getUserId());
                            int maxAllowed = activeFlashSale.getMaxPurchasePerUser();
                            
                            // ✅ LOẠI 1: Đã đạt giới hạn tối đa
                            if (currentPurchased >= maxAllowed) {
                                throw new BusinessException("Bạn đã mua đủ " + maxAllowed + " sản phẩm flash sale '" + 
                                    book.getBookName() + "' cho phép. Không thể đặt hàng thêm.");
                            }
                            
                            // ✅ LOẠI 2: Chưa đạt giới hạn nhưng đặt quá số lượng cho phép  
                            int remainingAllowed = maxAllowed - currentPurchased;
                            if (quantityToOrder > remainingAllowed) {
                                throw new BusinessException("Bạn đã mua " + currentPurchased + " sản phẩm, chỉ được mua thêm tối đa " + 
                                    remainingAllowed + " sản phẩm flash sale '" + book.getBookName() + "'.");
                            }
                            
                            // ✅ LOẠI 3: Thông báo chung
                            throw new BusinessException("Bạn chỉ được mua tối đa " + maxAllowed + " sản phẩm flash sale '" + 
                                book.getBookName() + "'.");
                        }
                        
                        // Đủ flash sale stock - dùng toàn bộ flash sale
                        flashSaleItem = activeFlashSale;
                        flashSaleItem.setStockQuantity(flashSaleStock - quantityToOrder);
                        flashSaleItemRepository.save(flashSaleItem);
                        
                        // Cập nhật unit price về flash sale price
                        detailRequest.setUnitPrice(activeFlashSale.getDiscountPrice());
                        
                        log.info("✅ Auto-applied flash sale for book {}: {} items at price {}", 
                            book.getId(), quantityToOrder, activeFlashSale.getDiscountPrice());
                    } else if (flashSaleStock > 0) {
                        // Không đủ flash sale stock - KHÔNG hỗ trợ mixed purchase trong OrderServiceImpl
                        // Để tránh phức tạp, báo lỗi để frontend xử lý
                        throw new BusinessException("Flash sale chỉ còn " + flashSaleStock + " sản phẩm. " +
                            "Vui lòng đặt " + flashSaleStock + " sản phẩm flash sale trong đơn riêng.");
                    }
                    // Nếu flashSaleStock = 0, không áp dụng flash sale
                }
                
                // Validate và trừ book stock thông thường
                if (book.getStockQuantity() < quantityToOrder) {
                    throw new BusinessException("Không đủ số lượng tồn kho cho sản phẩm: " + book.getBookName() + 
                        " (Tồn kho: " + book.getStockQuantity() + ", Yêu cầu: " + quantityToOrder + ")");
                }
                
                book.setStockQuantity(book.getStockQuantity() - quantityToOrder);
                bookRepository.save(book);
            }

            // Tạo OrderDetail
            OrderDetail orderDetail = new OrderDetail();
            OrderDetailId orderDetailId = new OrderDetailId();
            orderDetailId.setOrderId(order.getId());
            orderDetailId.setBookId(book.getId());
            orderDetail.setId(orderDetailId);
            orderDetail.setOrder(order);
            orderDetail.setBook(book);
            orderDetail.setFlashSaleItem(flashSaleItem); // null nếu không phải flash sale
            orderDetail.setQuantity(quantityToOrder);
            orderDetail.setUnitPrice(detailRequest.getUnitPrice());
            
            // ✅ FIX: Set createdBy properly for counter sales
            if (order.getUser() != null) {
                orderDetail.setCreatedBy(order.getUser().getId()); // Online order
            } else {
                orderDetail.setCreatedBy(order.getCreatedBy()); // Counter sales - use same as order
            }

            orderDetails.add(orderDetail);
        }

        orderDetailRepository.saveAll(orderDetails);

        // ✅ CẬP NHẬT VOUCHER USAGE VÀ LƯU ORDERV OUCHER ENTITIES (nếu có sử dụng voucher)
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            try {
                // 1. Update voucher usage counts
                voucherCalculationService.updateVoucherUsage(request.getVoucherIds(), request.getUserId());
                
                // 2. ✅ FIX: Save OrderVoucher entities để vouchers hiển thị trong API responses
                saveOrderVouchers(order, request.getVoucherIds(), calculatedSubtotal, shippingFee);
                
                // 3. Update voucher count trong order theo số lượng discount đã tính
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
    public List<OrderResponse> getProcessingOrdersByBookId(Integer bookId) {
        // Sử dụng trạng thái processing từ BookProcessingQuantityService
        List<OrderStatus> processingStatuses = List.of(
            OrderStatus.PENDING,                        // Chờ xử lý
            OrderStatus.CONFIRMED,                      // Đã xác nhận  
            OrderStatus.SHIPPED,                        // Đang giao hàng
            OrderStatus.DELIVERY_FAILED,                // Giao hàng thất bại
            OrderStatus.REDELIVERING,                   // Đang giao lại
            OrderStatus.RETURNING_TO_WAREHOUSE,         // Đang trả về kho
            OrderStatus.REFUND_REQUESTED,               // Yêu cầu hoàn trả
            OrderStatus.AWAITING_GOODS_RETURN,          // Đang chờ lấy hàng hoàn trả
            OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER,   // Đã nhận hàng hoàn trả từ khách
            OrderStatus.GOODS_RETURNED_TO_WAREHOUSE,    // Hàng đã về kho
            OrderStatus.REFUNDING                       // Đang hoàn tiền
        );
        
        List<Order> processingOrders = orderDetailRepository.findProcessingOrdersByBookId(bookId, processingStatuses);
        return processingOrders.stream()
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
        
        // Validate order status - Cho phép REFUNDING từ admin approval process
        if (order.getOrderStatus() != OrderStatus.DELIVERED && 
            order.getOrderStatus() != OrderStatus.REFUNDING) {
            throw new BusinessException("Chỉ có thể hoàn trả đơn hàng đã giao hoặc đã được phê duyệt hoàn trả");
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
        
        // Validate order status - Cho phép REFUNDING từ admin approval process
        if (order.getOrderStatus() != OrderStatus.DELIVERED && 
            order.getOrderStatus() != OrderStatus.SHIPPED && 
            order.getOrderStatus() != OrderStatus.REFUNDING) {
            throw new BusinessException("Chỉ có thể hoàn trả đơn hàng đã giao, đang vận chuyển hoặc đã được phê duyệt hoàn trả");
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
            
            // ✅ KHÔNG cộng stock ở đây nữa - chỉ khi admin đổi trạng thái về GOODS_RETURNED_TO_WAREHOUSE
            log.info("Partial refund calculated for book {}: quantity={}, amount={}", 
                     refundDetail.getBookId(), refundDetail.getRefundQuantity(), detailRefundAmount);
            
            // Update order detail quantity
            orderDetail.setQuantity(orderDetail.getQuantity() - refundDetail.getRefundQuantity());
            orderDetailRepository.save(orderDetail);
        }
        
        return totalRefundAmount;
    }

    private void handleFullRefundBusinessLogic(Order order, String reason) {
        // ✅ KHÔNG cộng stock ở đây nữa - chỉ khi admin đổi trạng thái về GOODS_RETURNED_TO_WAREHOUSE
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        
        // Tính toán tổng số lượng để log
        int totalQuantity = orderDetails.stream().mapToInt(OrderDetail::getQuantity).sum();
        log.info("Full refund processed for order {}: {} items. Stock will be restored when admin changes status to GOODS_RETURNED_TO_WAREHOUSE", 
                 order.getCode(), totalQuantity);

        // Restore voucher usage if applicable
        if (order.getRegularVoucherCount() > 0 || order.getShippingVoucherCount() > 0) {
            // Would need voucher restoration logic here
            log.info("Order {} fully refunded, voucher usage should be restored", order.getCode());
        }

        log.info("Order {} fully refunded, stock will be restored separately", order.getCode());
    }
    
    /**
     * ✅ THÊM MỚI: Khách hàng gửi yêu cầu hoàn trả
     * 🔥 FIXED: Tạo RefundRequest record trong database
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
            
            // 🔥 SOLUTION: Tạo RefundRequest record thông qua RefundService
            RefundRequest newRefundRequest = new RefundRequest();
            newRefundRequest.setOrder(order);
            newRefundRequest.setUser(order.getUser());
            newRefundRequest.setReason(refundRequest.getReason());
            newRefundRequest.setRefundType(RefundType.PARTIAL); // Default, có thể điều chỉnh theo logic
            newRefundRequest.setStatus(RefundStatus.PENDING);
            newRefundRequest.setCreatedAt(System.currentTimeMillis());
            
            // Set evidence images/videos if provided
            if (refundRequest.getEvidenceImages() != null) {
                newRefundRequest.setEvidenceImages(new ArrayList<>(refundRequest.getEvidenceImages()));
            }
            if (refundRequest.getEvidenceVideos() != null) {
                newRefundRequest.setEvidenceVideos(new ArrayList<>(refundRequest.getEvidenceVideos()));
            }
            
            // Save RefundRequest first
            RefundRequest savedRefundRequest = refundRequestRepository.save(newRefundRequest);
            
            // 🔥 Tạo RefundItem records cho từng sản phẩm
            if (refundRequest.getRefundDetails() != null) {
                List<RefundItem> refundItems = new ArrayList<>();
                for (OrderDetailRefundRequest detail : refundRequest.getRefundDetails()) {
                    OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(orderId, detail.getBookId());
                    if (orderDetail == null) {
                        throw new BusinessException("Không tìm thấy sản phẩm trong đơn hàng");
                    }
                    
                    RefundItem refundItem = new RefundItem();
                    refundItem.setRefundRequest(savedRefundRequest);
                    refundItem.setBook(orderDetail.getBook());
                    refundItem.setRefundQuantity(detail.getRefundQuantity());
                    refundItem.setUnitPrice(orderDetail.getUnitPrice());
                    refundItem.setTotalAmount(orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getRefundQuantity())));
                    refundItem.setCreatedAt(System.currentTimeMillis());
                    
                    refundItems.add(refundItem);
                }
                refundItemRepository.saveAll(refundItems);
            }
            
            // Cập nhật trạng thái đơn hàng
            order.setOrderStatus(OrderStatus.REFUND_REQUESTED);
            order.setCancelReason(refundRequest.getReason());
            order.setUpdatedBy(refundRequest.getUserId().intValue());
            orderRepository.save(order);
            
            log.info("Customer {} requested refund for order {} - RefundRequest ID: {}", 
                refundRequest.getUserId(), order.getCode(), savedRefundRequest.getId());
            
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
            
            // ✅ FIX: Admin chấp nhận -> CHỈ chuyển sang REFUNDING (không auto REFUNDED)
            // Admin sẽ manual chuyển sau khi đã xử lý đầy đủ
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
            
            // ✅ REMOVED: Không tự động chuyển thành REFUNDED nữa
            // Admin sẽ sử dụng Order Status Transition API để chuyển thành:
            // REFUNDING → GOODS_RECEIVED_FROM_CUSTOMER → GOODS_RETURNED_TO_WAREHOUSE → REFUNDED
            
            log.info("✅ Admin {} approved refund for order {} - Status: REFUNDING (admin must manually transition to complete)", 
                     decision.getAdminId(), order.getCode());
            
            OrderResponse response = orderResponseMapper.toResponse(order);
            return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Yêu cầu hoàn trả đã được chấp nhận. Admin cần chuyển trạng thái đơn hàng để hoàn thành quy trình.",
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
    
    /**
     * ✅ ENHANCED: Save OrderVoucher entities để vouchers hiển thị trong API responses
     */
    private void saveOrderVouchers(Order order, List<Integer> voucherIds, BigDecimal orderSubtotal, BigDecimal shippingFee) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return;
        }
        
        try {
            // Get vouchers from database
            List<Voucher> vouchers = voucherRepository.findAllById(voucherIds);
            
            for (Voucher voucher : vouchers) {
                // Calculate actual discount applied for this voucher
                BigDecimal discountApplied = voucherCalculationService.calculateSingleVoucherDiscount(voucher, orderSubtotal, shippingFee);
                
                // Create OrderVoucher entity
                OrderVoucher orderVoucher = new OrderVoucher();
                
                // Set composite ID
                OrderVoucherId id = new OrderVoucherId();
                id.setOrderId(order.getId());
                id.setVoucherId(voucher.getId());
                orderVoucher.setId(id);
                
                // Set relationships
                orderVoucher.setOrder(order);
                orderVoucher.setVoucher(voucher);
                
                // Set voucher information (will be auto-set in @PrePersist)
                orderVoucher.setVoucherCategory(voucher.getVoucherCategory());
                orderVoucher.setDiscountType(voucher.getDiscountType());
                
                // Set discount applied amount
                orderVoucher.setDiscountApplied(discountApplied);
                
                // appliedAt will be set in @PrePersist
                
                // Save OrderVoucher
                orderVoucherRepository.save(orderVoucher);
                
                log.info("✅ Saved OrderVoucher: orderId={}, voucherId={}, discountApplied={}", 
                    order.getId(), voucher.getId(), discountApplied);
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to save OrderVoucher entities for order {}: {}", order.getId(), e.getMessage(), e);
            // Không throw exception để không làm fail việc tạo order
        }
    }
}
