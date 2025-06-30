package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
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
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherCalculationService voucherCalculationService;
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
    @Transactional
    public ApiResponse<OrderResponse> create(OrderRequest request) {
        try {
            // Validate user
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user == null) {
                return new ApiResponse<>(404, "Không tìm thấy người dùng", null);
            }
            
            // Validate address
            Address address = addressRepository.findById(request.getAddressId()).orElse(null);
            if (address == null) {
                return new ApiResponse<>(404, "Không tìm thấy địa chỉ", null);
            }
            
            // Create order
            Order order = orderMapper.toOrder(request);
            order.setUser(user);
            order.setAddress(address);
            
            // Set subtotal and shipping fee from request
            order.setSubtotal(request.getSubtotal());
            order.setShippingFee(request.getShippingFee());
            
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
            
            Order savedOrder = orderRepository.save(order);
            
            // Create order details
            for (OrderDetailRequest detailRequest : request.getOrderDetails()) {
                createOrderDetail(savedOrder, detailRequest);
            }
            
            // Apply vouchers if provided - validate and calculate discounts
            if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
                try {
                    // Validate and calculate vouchers using VoucherCalculationService
                    VoucherCalculationService.VoucherCalculationResult voucherResult = 
                        voucherCalculationService.calculateVoucherDiscount(savedOrder, request.getVoucherIds(), request.getUserId());
                    
                    // Update order amounts based on voucher calculations
                    savedOrder.setDiscountAmount(voucherResult.getTotalProductDiscount());
                    savedOrder.setDiscountShipping(voucherResult.getTotalShippingDiscount());
                    savedOrder.setRegularVoucherCount(voucherResult.getRegularVoucherCount());
                    savedOrder.setShippingVoucherCount(voucherResult.getShippingVoucherCount());
                    
                    // Recalculate total amount
                    BigDecimal recalculatedTotal = request.getSubtotal()
                        .add(request.getShippingFee())
                        .subtract(voucherResult.getTotalProductDiscount())
                        .subtract(voucherResult.getTotalShippingDiscount());
                    savedOrder.setTotalAmount(recalculatedTotal);
                    
                    // Create order vouchers with calculated details
                    for (VoucherCalculationService.VoucherApplicationDetail voucherDetail : voucherResult.getAppliedVouchers()) {
                        createOrderVoucherWithDetails(savedOrder, voucherDetail);
                    }
                    
                    // Update voucher usage
                    voucherCalculationService.updateVoucherUsage(request.getVoucherIds(), request.getUserId());
                    
                    // Save updated order
                    savedOrder = orderRepository.save(savedOrder);
                    
                } catch (Exception e) {
                    return new ApiResponse<>(400, "Lỗi áp dụng voucher: " + e.getMessage(), null);
                }
            } else {
                // No vouchers - just use provided amounts
                savedOrder.setDiscountAmount(request.getDiscountAmount());
                savedOrder.setDiscountShipping(request.getDiscountShipping());
                savedOrder = orderRepository.save(savedOrder);
            }
            
            OrderResponse response = orderResponseMapper.toResponse(savedOrder);
            return new ApiResponse<>(201, "Tạo đơn hàng thành công", response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi tạo đơn hàng: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderResponse> update(OrderRequest request, Integer id) {
        Order existing = getById(id);
        if (existing == null) {
            return new ApiResponse<>(404, "Không tìm thấy đơn hàng", null);
        }
        
        // Only allow update if order is still PENDING
        if (existing.getOrderStatus() != OrderStatus.PENDING) {
            return new ApiResponse<>(400, "Chỉ có thể cập nhật đơn hàng đang chờ xử lý", null);
        }
        
        try {
            // Update basic info
            existing.setTotalAmount(request.getTotalAmount());
            existing.setOrderStatus(request.getOrderStatus());
            existing.setOrderType(request.getOrderType());
            existing.setUpdatedBy(request.getUserId());
            
            // Update address if changed
            if (!existing.getAddress().getId().equals(request.getAddressId())) {
                Address newAddress = addressRepository.findById(request.getAddressId()).orElse(null);
                if (newAddress != null) {
                    existing.setAddress(newAddress);
                }
            }
            
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
        
        // Only allow cancel if order is PENDING or CONFIRMED
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            return new ApiResponse<>(400, "Không thể hủy đơn hàng ở trạng thái này", null);
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
    
    private void createOrderDetail(Order order, OrderDetailRequest detailRequest) {
        // Validate book
        Book book = bookRepository.findById(detailRequest.getBookId()).orElse(null);
        if (book == null) {
            throw new RuntimeException("Không tìm thấy sách với ID: " + detailRequest.getBookId());
        }
        
        OrderDetail orderDetail = new OrderDetail();
        
        // Set composite key
        OrderDetailId detailId = new OrderDetailId();
        detailId.setOrderId(order.getId());
        detailId.setBookId(book.getId());
        orderDetail.setId(detailId);
        
        orderDetail.setOrder(order);
        orderDetail.setBook(book);
        orderDetail.setQuantity(detailRequest.getQuantity());
        orderDetail.setUnitPrice(detailRequest.getUnitPrice());
        orderDetail.setCreatedBy(order.getCreatedBy());
        orderDetail.setStatus((byte) 1);
        
        // Set flash sale item if provided
        if (detailRequest.getFlashSaleItemId() != null) {
            FlashSaleItem flashSaleItem = flashSaleItemRepository.findById(detailRequest.getFlashSaleItemId()).orElse(null);
            if (flashSaleItem != null) {
                orderDetail.setFlashSaleItem(flashSaleItem);
            }
        }
        
        orderDetailRepository.save(orderDetail);
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
        orderVoucher.setVoucherType(voucherDetail.getVoucherType());
        orderVoucher.setDiscountApplied(voucherDetail.getDiscountApplied());
        orderVoucher.setAppliedAt(System.currentTimeMillis());
        
        orderVoucherRepository.save(orderVoucher);
    }
}
