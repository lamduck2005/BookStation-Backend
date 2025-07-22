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
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.RefundRequestRepository;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.repository.RefundItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.service.RefundService;
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
    private BookRepository bookRepository;
    
    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

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
    public RefundRequestResponse rejectRefundRequest(Integer refundRequestId, RefundApprovalRequest rejection, Integer adminId) {
        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn trả không tồn tại"));

        if (request.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể từ chối yêu cầu đang chờ xử lý");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));

        // ✅ Update RefundRequest status thành REJECTED
        request.setStatus(RefundStatus.REJECTED);
        request.setApprovedBy(admin);
        request.setAdminNote(rejection.getAdminNote());
        
        // ✅ Lưu thông tin từ chối chi tiết
        request.setRejectReason(rejection.getRejectReason());
        request.setRejectReasonDisplay(rejection.getRejectReasonDisplay());
        request.setSuggestedAction(rejection.getSuggestedAction());
        request.setRejectedAt(System.currentTimeMillis());
        request.setUpdatedAt(System.currentTimeMillis());

        // ✅ Đổi trạng thái đơn hàng về DELIVERED (khách có thể tạo yêu cầu hoàn trả mới sau)
        Order order = request.getOrder();
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setUpdatedBy(adminId);
        orderRepository.save(order);

        RefundRequest savedRequest = refundRequestRepository.save(request);

        log.info("❌ REFUND REQUEST REJECTED: id={}, adminId={}, reason={}, order back to DELIVERED", 
                 refundRequestId, adminId, rejection.getRejectReason());

        return convertToResponse(savedRequest);
    }

    @Override
    public RefundRequestResponse processRefund(Integer refundRequestId, Integer adminId) {
        RefundRequest request = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu hoàn trả không tồn tại"));

        if (request.getStatus() != RefundStatus.APPROVED) {
            throw new RuntimeException("Chỉ có thể xử lý yêu cầu đã được phê duyệt");
        }

        // ✅ SỬA LOGIC: CHỈ hoàn voucher, KHÔNG cộng stock
        // NHƯNG phải trừ sold count ngay lập tức
        Order order = request.getOrder();
        
        // ✅ TRỪ SOLD COUNT NGAY LẬP TỨC (không đợi về kho)
        if (request.getRefundType() == RefundType.FULL) {
            // Full refund - trừ sold count cho toàn bộ đơn hàng
            deductSoldCountForFullRefund(order);
        } else {
            // Partial refund - trừ sold count cho từng item
            deductSoldCountForPartialRefund(order, request.getRefundItems());
        }
        
        // Hoàn voucher nếu có
        if (order.getRegularVoucherCount() > 0 || order.getShippingVoucherCount() > 0) {
            // Call voucher service to restore voucher usage
            log.info("Order {} - Restoring voucher usage: regular={}, shipping={}", 
                     order.getCode(), order.getRegularVoucherCount(), order.getShippingVoucherCount());
        }

        // Đổi trạng thái đơn hàng thành REFUNDING (đang hoàn tiền)
        order.setOrderStatus(OrderStatus.REFUNDING);
        order.setUpdatedAt(System.currentTimeMillis());
        orderRepository.save(order);

        // Update refund request status
        request.setStatus(RefundStatus.COMPLETED);
        request.setCompletedAt(System.currentTimeMillis());
        request.setUpdatedAt(System.currentTimeMillis());

        RefundRequest savedRequest = refundRequestRepository.save(request);

        log.info("✅ REFUND PROCESSED (voucher restored only): id={}, orderId={}, adminId={}", 
                 refundRequestId, request.getOrder().getId(), adminId);
        log.info("⚠️  STOCK NOT RESTORED YET - Admin must change order status to GOODS_RETURNED_TO_WAREHOUSE to restore stock");

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
        
        // ✅ THÊM MỚI: Thông tin từ chối
        response.setRejectReason(request.getRejectReason());
        response.setRejectReasonDisplay(request.getRejectReasonDisplay());
        response.setSuggestedAction(request.getSuggestedAction());
        response.setRejectedAt(request.getRejectedAt());
        
        if (request.getApprovedBy() != null) {
            response.setApprovedByName(request.getApprovedBy().getFullName());
        }
        
        return response;
    }
    
    /**
     * ✅ Trừ sold count cho hoàn trả toàn bộ (KHÔNG cộng stock)
     */
    private void deductSoldCountForFullRefund(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            deductSoldCountForOrderDetail(detail, detail.getQuantity());
        }
        log.info("✅ Deducted sold count for full refund: order={}", order.getCode());
    }
    
    /**
     * ✅ Trừ sold count cho hoàn trả một phần (KHÔNG cộng stock)
     */
    private void deductSoldCountForPartialRefund(Order order, List<RefundItem> refundItems) {
        for (RefundItem refundItem : refundItems) {
            OrderDetail orderDetail = orderDetailRepository.findByOrderIdAndBookId(
                order.getId(), refundItem.getBook().getId());
            if (orderDetail != null) {
                deductSoldCountForOrderDetail(orderDetail, refundItem.getRefundQuantity());
            }
        }
        log.info("✅ Deducted sold count for partial refund: order={}, items={}", 
                 order.getCode(), refundItems.size());
    }
    
    /**
     * ✅ Trừ sold count cho một OrderDetail cụ thể
     */
    private void deductSoldCountForOrderDetail(OrderDetail detail, Integer quantity) {
        if (detail.getFlashSaleItem() != null) {
            // Flash sale item
            FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
            int currentSoldCount = flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0;
            flashSaleItem.setSoldCount(Math.max(0, currentSoldCount - quantity));
            flashSaleItemRepository.save(flashSaleItem);
            
            // Cũng trừ sold count cho book gốc
            Book book = detail.getBook();
            int currentBookSoldCount = book.getSoldCount() != null ? book.getSoldCount() : 0;
            book.setSoldCount(Math.max(0, currentBookSoldCount - quantity));
            bookRepository.save(book);
            
            log.info("Deducted sold count: FlashSale item {} and Book {} by {}", 
                     flashSaleItem.getId(), book.getId(), quantity);
        } else {
            // Book thông thường
            Book book = detail.getBook();
            int currentSoldCount = book.getSoldCount() != null ? book.getSoldCount() : 0;
            book.setSoldCount(Math.max(0, currentSoldCount - quantity));
            bookRepository.save(book);
            
            log.info("Deducted sold count: Book {} by {}", book.getId(), quantity);
        }
    }
}
