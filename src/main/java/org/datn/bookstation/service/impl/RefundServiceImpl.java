package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.RefundRequestCreate;
import org.datn.bookstation.dto.request.RefundApprovalRequest;
import org.datn.bookstation.dto.response.RefundRequestResponse;
import org.datn.bookstation.entity.RefundRequest;
import org.datn.bookstation.entity.RefundRequest.RefundType;
import org.datn.bookstation.entity.RefundRequest.RefundStatus;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.RefundItem;
import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.RefundRequestRepository;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.repository.RefundItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.service.RefundService;
import org.datn.bookstation.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class RefundServiceImpl implements RefundService {
    @Override
    public List<RefundRequestResponse> getAllRefundRequests(int page, int size, String sortBy, String sortDir) {
        org.springframework.data.domain.Pageable pageable =
            org.springframework.data.domain.PageRequest.of(page, size,
                "desc".equalsIgnoreCase(sortDir)
                    ? org.springframework.data.domain.Sort.by(sortBy).descending()
                    : org.springframework.data.domain.Sort.by(sortBy).ascending()
            );
        org.springframework.data.domain.Page<RefundRequest> refundPage = refundRequestRepository.findAll(pageable);
        return refundPage.stream().map(this::convertToResponse).collect(java.util.stream.Collectors.toList());
    }

    @Autowired
    private RefundRequestRepository refundRequestRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RefundItemRepository refundItemRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private OrderService orderService;

    @Override
    public RefundRequestResponse createRefundRequest(RefundRequestCreate request, Integer userId) {
        log.info("🔄 CREATING REFUND REQUEST: orderId={}, userId={}, type={}", 
                 request.getOrderId(), userId, request.getRefundType());

        // 1. VALIDATE ORDER
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hoàn trả đơn hàng này");
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Chỉ có thể hoàn trả đơn hàng đã giao thành công");
        }

        // 2. CHECK IF REFUND REQUEST ALREADY EXISTS
        boolean hasActiveRefund = refundRequestRepository.existsActiveRefundRequestForOrder(request.getOrderId());
        
        if (hasActiveRefund) {
            throw new RuntimeException("Đơn hàng này đã có yêu cầu hoàn trả đang xử lý");
        }

        // 3. GET USER
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // 4. CREATE REFUND REQUEST
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrder(order);
        refundRequest.setUser(user);
        refundRequest.setRefundType(RefundType.valueOf(request.getRefundType()));
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setReason(request.getReason());
        refundRequest.setCustomerNote(request.getCustomerNote());
        refundRequest.setEvidenceImages(request.getEvidenceImages());
        refundRequest.setEvidenceVideos(request.getEvidenceVideos());
        refundRequest.setCreatedAt(System.currentTimeMillis());
        refundRequest.setUpdatedAt(System.currentTimeMillis());

        // 5. CALCULATE REFUND AMOUNT
        BigDecimal refundAmount = BigDecimal.ZERO;
        
        if (request.getRefundType().equals("FULL")) {
            refundAmount = order.getTotalAmount();
        } else if (request.getRefundType().equals("PARTIAL")) {
            if (request.getRefundItems() == null || request.getRefundItems().isEmpty()) {
                throw new RuntimeException("Danh sách sản phẩm hoàn trả không được để trống cho hoàn trả một phần");
            }
            
            for (RefundRequestCreate.RefundItemRequest item : request.getRefundItems()) {
                OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(
                        request.getOrderId(), item.getBookId());
                if (orderDetail == null) {
                    throw new RuntimeException("Không tìm thấy sản phẩm trong đơn hàng: " + item.getBookId());
                }
                if (item.getRefundQuantity() > orderDetail.getQuantity()) {
                    throw new RuntimeException("Số lượng hoàn trả vượt quá số lượng đã mua");
                }
                
                BigDecimal itemRefund = orderDetail.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getRefundQuantity()));
                refundAmount = refundAmount.add(itemRefund);
            }
        }
        
        refundRequest.setTotalRefundAmount(refundAmount);

        // 6. SAVE REFUND REQUEST
        RefundRequest savedRequest = refundRequestRepository.save(refundRequest);

        // 7. CREATE REFUND ITEMS FOR PARTIAL REFUND
        if (request.getRefundType().equals("PARTIAL")) {
            for (RefundRequestCreate.RefundItemRequest item : request.getRefundItems()) {
                RefundItem refundItem = new RefundItem();
                refundItem.setRefundRequest(savedRequest);
                
                OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(
                        request.getOrderId(), item.getBookId());
                refundItem.setBook(orderDetail.getBook());
                refundItem.setRefundQuantity(item.getRefundQuantity());
                refundItem.setReason(item.getReason());
                refundItem.setUnitPrice(orderDetail.getUnitPrice());
                
                BigDecimal itemRefundAmount = orderDetail.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getRefundQuantity()));
                refundItem.setTotalAmount(itemRefundAmount);
                refundItem.setCreatedAt(System.currentTimeMillis());
                
                refundItemRepository.save(refundItem);
            }
        }

        log.info("✅ REFUND REQUEST CREATED: id={}, orderId={}, amount={}, status=PENDING", 
                 savedRequest.getId(), request.getOrderId(), refundAmount);

        return convertToResponse(savedRequest);
    }

    @Override
    public RefundRequestResponse approveRefundRequest(Integer refundRequestId, RefundApprovalRequest approval, Integer adminId) {
        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn trả không tồn tại"));

        if (request.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể phê duyệt yêu cầu đang chờ xử lý");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));

        // 🔥 Update RefundRequest status
        request.setStatus(RefundStatus.valueOf(approval.getStatus()));
        request.setApprovedBy(admin);
        request.setAdminNote(approval.getAdminNote());
        request.setApprovedAt(System.currentTimeMillis());
        request.setUpdatedAt(System.currentTimeMillis());

        // 🔥 CRITICAL FIX: Đồng bộ Order status
        Order order = request.getOrder();
        if (approval.getStatus().equals("APPROVED")) {
            order.setOrderStatus(OrderStatus.REFUNDING); // Đã phê duyệt, đang hoàn tiền
        } else if (approval.getStatus().equals("REJECTED")) {
            order.setOrderStatus(OrderStatus.DELIVERED); // Từ chối, trở về delivered
        }
        order.setUpdatedBy(adminId);
        orderRepository.save(order);

        RefundRequest savedRequest = refundRequestRepository.save(request);

        log.info("✅ REFUND REQUEST {}: id={}, adminId={}, refundStatus={}, orderStatus={}", 
                 approval.getStatus(), refundRequestId, adminId, approval.getStatus(), order.getOrderStatus());

        return convertToResponse(savedRequest);
    }

    @Override
    public RefundRequestResponse processRefund(Integer refundRequestId, Integer adminId) {
        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn trả không tồn tại"));

        if (request.getStatus() != RefundStatus.APPROVED) {
            throw new RuntimeException("Chỉ có thể xử lý yêu cầu đã được phê duyệt");
        }

        // Gọi OrderService để thực hiện hoàn tiền
        if (request.getRefundType() == RefundType.FULL) {
            orderService.fullRefund(request.getOrder().getId(), request.getUser().getId(), request.getReason());
        } else {
            // Convert refund items to OrderDetailRefundRequest format
            List<org.datn.bookstation.dto.request.OrderDetailRefundRequest> refundDetails = 
                request.getRefundItems().stream().map(item -> {
                    org.datn.bookstation.dto.request.OrderDetailRefundRequest detail = 
                        new org.datn.bookstation.dto.request.OrderDetailRefundRequest();
                    detail.setBookId(item.getBook().getId());
                    detail.setRefundQuantity(item.getRefundQuantity());
                    detail.setReason(item.getReason());
                    return detail;
                }).collect(Collectors.toList());

            orderService.partialRefund(request.getOrder().getId(), request.getUser().getId(), 
                                     request.getReason(), refundDetails);
        }

        // Update refund request status
        request.setStatus(RefundStatus.COMPLETED);
        request.setCompletedAt(System.currentTimeMillis());
        request.setUpdatedAt(System.currentTimeMillis());

        RefundRequest savedRequest = refundRequestRepository.save(request);

        log.info("✅ REFUND PROCESSED: id={}, orderId={}, adminId={}", 
                 refundRequestId, request.getOrder().getId(), adminId);

        return convertToResponse(savedRequest);
    }

    @Override
    public List<RefundRequestResponse> getRefundRequestsByUser(Integer userId) {
        List<RefundRequest> requests = refundRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return requests.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<RefundRequestResponse> getPendingRefundRequests() {
        List<RefundRequest> requests = refundRequestRepository.findByStatusOrderByCreatedAtDesc(RefundStatus.PENDING);
        return requests.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public RefundRequestResponse getRefundRequestById(Integer refundRequestId) {
        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn trả không tồn tại"));
        return convertToResponse(request);
    }

    @Override
    public String validateRefundRequest(Integer orderId, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!order.getUser().getId().equals(userId)) {
            return "Bạn không có quyền hoàn trả đơn hàng này";
        }

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            return "Chỉ có thể hoàn trả đơn hàng đã giao thành công";
        }

        boolean hasActiveRefund = refundRequestRepository.existsActiveRefundRequestForOrder(orderId);
        
        if (hasActiveRefund) {
            return "Đơn hàng này đã có yêu cầu hoàn trả đang xử lý";
        }

        return null; // Valid
    }

    private RefundRequestResponse convertToResponse(RefundRequest request) {
        RefundRequestResponse response = new RefundRequestResponse();
        response.setId(request.getId());
        response.setOrderId(request.getOrder().getId());
        response.setOrderCode(request.getOrder().getCode());
        response.setUserFullName(request.getUser().getFullName());
        response.setRefundType(request.getRefundType().name());
        response.setStatus(request.getStatus().name());
        response.setStatusDisplay(request.getStatus().getDisplayName());
        response.setReason(request.getReason());
        response.setCustomerNote(request.getCustomerNote());
        response.setAdminNote(request.getAdminNote());
        response.setTotalRefundAmount(request.getTotalRefundAmount());
        response.setEvidenceImages(request.getEvidenceImages());
        response.setEvidenceVideos(request.getEvidenceVideos());
        response.setCreatedAt(request.getCreatedAt());
        response.setApprovedAt(request.getApprovedAt());
        response.setCompletedAt(request.getCompletedAt());
        
        if (request.getApprovedBy() != null) {
            response.setApprovedByName(request.getApprovedBy().getFullName());
        }
        
        return response;
    }
}
