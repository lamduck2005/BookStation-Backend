package org.datn.bookstation.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.CheckoutSessionRequest;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.request.OrderDetailRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CheckoutSessionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.mapper.CheckoutSessionMapper;
import org.datn.bookstation.mapper.CheckoutSessionResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.CheckoutSessionService;
import org.datn.bookstation.service.CartItemService;
import org.datn.bookstation.service.CartService;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.datn.bookstation.specification.CheckoutSessionSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckoutSessionServiceImpl implements CheckoutSessionService {

    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionMapper checkoutSessionMapper;
    private final CheckoutSessionResponseMapper checkoutSessionResponseMapper;
    private final CartItemService cartItemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final VoucherCalculationService voucherCalculationService;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final VoucherRepository voucherRepository;
    private final AddressRepository addressRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ApiResponse<CheckoutSessionResponse> createCheckoutSession(Integer userId, CheckoutSessionRequest request) {
        try {
            log.info("Creating checkout session for user: {}", userId);

            // Validate request
            validateCheckoutSessionRequest(request);

            // Create entity
            CheckoutSession session = checkoutSessionMapper.toEntity(userId, request);

            // Gán địa chỉ mặc định nếu request không truyền addressId
            if (request.getAddressId() == null) {
                Optional<Address> defaultAddressOpt = addressRepository.findDefaultByUserId(userId);
                defaultAddressOpt.ifPresent(session::setAddress);
            } else {
                Address address = new Address();
                address.setId(request.getAddressId());
                session.setAddress(address);
            }

            // Calculate pricing
            calculateSessionPricing(session, request);

            // Save session
            CheckoutSession savedSession = checkoutSessionRepository.save(session);

            // Convert to response
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(savedSession);

            log.info("Successfully created checkout session with ID: {}", savedSession.getId());
            return new ApiResponse<>(201, "Tạo checkout session thành công", response);
            
        } catch (Exception e) {
            log.error("Error creating checkout session for user: {}", userId, e);
            return new ApiResponse<>(400, "Lỗi khi tạo checkout session: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> updateCheckoutSession(Integer sessionId, Integer userId, CheckoutSessionRequest request) {
        try {
            log.info("Updating checkout session: {} for user: {}", sessionId, userId);

            // Find existing session
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            CheckoutSession existingSession = sessionOpt.get();
            
            // Check if session is still active
            if (!existingSession.isActive()) {
                return new ApiResponse<>(400, "Checkout session đã hết hạn", null);
            }

            // Update entity
            checkoutSessionMapper.updateEntity(existingSession, request);
            
            // Recalculate pricing
            calculateSessionPricing(existingSession, request);

            // Save session
            CheckoutSession savedSession = checkoutSessionRepository.save(existingSession);
            
            // Convert to response
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(savedSession);
            
            log.info("Successfully updated checkout session: {}", sessionId);
            return new ApiResponse<>(200, "Cập nhật checkout session thành công", response);
            
        } catch (Exception e) {
            log.error("Error updating checkout session: {}", sessionId, e);
            return new ApiResponse<>(400, "Lỗi khi cập nhật checkout session: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> getCheckoutSessionById(Integer sessionId, Integer userId) {
        try {
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            CheckoutSession session = sessionOpt.get();
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(session);
            
            return new ApiResponse<>(200, "Lấy checkout session thành công", response);
            
        } catch (Exception e) {
            log.error("Error getting checkout session: {}", sessionId, e);
            return new ApiResponse<>(500, "Lỗi khi lấy checkout session: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> getLatestCheckoutSession(Integer userId) {
        try {
            long currentTime = System.currentTimeMillis();
            List<CheckoutSession> sessions = checkoutSessionRepository.findLatestActiveByUserId(userId, currentTime);
            if (sessions.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session nào", null);
            }
            CheckoutSession session = sessions.get(0);
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(session);
            return new ApiResponse<>(200, "Lấy checkout session mới nhất thành công", response);
        } catch (Exception e) {
            log.error("Error getting latest checkout session for user: {}", userId, e);
            return new ApiResponse<>(500, "Lỗi khi lấy checkout session: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<PaginationResponse<CheckoutSessionResponse>> getUserCheckoutSessions(Integer userId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Specification<CheckoutSession> spec = CheckoutSessionSpecification.hasUserId(userId);
            
            Page<CheckoutSession> sessionPage = checkoutSessionRepository.findAll(spec, pageable);
            
            List<CheckoutSessionResponse> responses = sessionPage.getContent().stream()
                    .map(checkoutSessionResponseMapper::toResponse)
                    .toList();
            
            PaginationResponse<CheckoutSessionResponse> paginationResponse = PaginationResponse.<CheckoutSessionResponse>builder()
                    .content(responses)
                    .pageNumber(sessionPage.getNumber())
                    .pageSize(sessionPage.getSize())
                    .totalElements(sessionPage.getTotalElements())
                    .totalPages(sessionPage.getTotalPages())
                    .build();
            
            return new ApiResponse<>(200, "Lấy danh sách checkout sessions thành công", paginationResponse);
            
        } catch (Exception e) {
            log.error("Error getting user checkout sessions for user: {}", userId, e);
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách checkout sessions: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<PaginationResponse<CheckoutSessionResponse>> getAllCheckoutSessions(int page, int size, Integer userId, Byte status, Long startDate, Long endDate) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            Specification<CheckoutSession> spec = null;
            
            if (userId != null) {
                spec = spec == null ? CheckoutSessionSpecification.hasUserId(userId) : spec.and(CheckoutSessionSpecification.hasUserId(userId));
            }
            if (status != null) {
                spec = spec == null ? CheckoutSessionSpecification.hasStatus(status) : spec.and(CheckoutSessionSpecification.hasStatus(status));
            }
            if (startDate != null && endDate != null) {
                spec = spec == null ? CheckoutSessionSpecification.createdBetween(startDate, endDate) : spec.and(CheckoutSessionSpecification.createdBetween(startDate, endDate));
            }
            
            Page<CheckoutSession> sessionPage;
            if (spec != null) {
                sessionPage = checkoutSessionRepository.findAll(spec, pageable);
            } else {
                sessionPage = checkoutSessionRepository.findAll(pageable);
            }
            
            List<CheckoutSessionResponse> responses = sessionPage.getContent().stream()
                    .map(checkoutSessionResponseMapper::toResponse)
                    .toList();
            
            PaginationResponse<CheckoutSessionResponse> paginationResponse = PaginationResponse.<CheckoutSessionResponse>builder()
                    .content(responses)
                    .pageNumber(sessionPage.getNumber())
                    .pageSize(sessionPage.getSize())
                    .totalElements(sessionPage.getTotalElements())
                    .totalPages(sessionPage.getTotalPages())
                    .build();
            
            return new ApiResponse<>(200, "Lấy tất cả checkout sessions thành công", paginationResponse);
            
        } catch (Exception e) {
            log.error("Error getting all checkout sessions", e);
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách checkout sessions: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> deleteCheckoutSession(Integer sessionId, Integer userId) {
        try {
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            checkoutSessionRepository.delete(sessionOpt.get());
            
            log.info("Successfully deleted checkout session: {}", sessionId);
            return new ApiResponse<>(200, "Xóa checkout session thành công", "OK");
            
        } catch (Exception e) {
            log.error("Error deleting checkout session: {}", sessionId, e);
            return new ApiResponse<>(500, "Lỗi khi xóa checkout session: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> markSessionCompleted(Integer sessionId, Integer userId) {
        try {
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            CheckoutSession session = sessionOpt.get();
            session.setStatus((byte) 2); // Completed
            
            CheckoutSession savedSession = checkoutSessionRepository.save(session);
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(savedSession);
            
            log.info("Successfully marked session as completed: {}", sessionId);
            return new ApiResponse<>(200, "Đánh dấu session hoàn thành thành công", response);
            
        } catch (Exception e) {
            log.error("Error marking session as completed: {}", sessionId, e);
            return new ApiResponse<>(500, "Lỗi khi đánh dấu session hoàn thành: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> recalculateSessionPricing(Integer sessionId, Integer userId) {
        try {
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            CheckoutSession session = sessionOpt.get();
            
            if (!session.isActive()) {
                return new ApiResponse<>(400, "Checkout session đã hết hạn", null);
            }

            // Parse checkout items
            List<CheckoutSessionRequest.BookQuantity> items = parseCheckoutItems(session.getCheckoutItems());
            if (items == null) {
                return new ApiResponse<>(400, "Không thể parse checkout items", null);
            }

            // Recalculate pricing
            CheckoutSessionRequest request = new CheckoutSessionRequest();
            request.setItems(items);
            request.setSelectedVoucherIds(checkoutSessionMapper.parseVoucherIds(session.getSelectedVoucherIds()));
            
            calculateSessionPricing(session, request);
            
            CheckoutSession savedSession = checkoutSessionRepository.save(session);
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(savedSession);
            
            log.info("Successfully recalculated pricing for session: {}", sessionId);
            return new ApiResponse<>(200, "Tính lại giá thành công", response);
            
        } catch (Exception e) {
            log.error("Error recalculating session pricing: {}", sessionId, e);
            return new ApiResponse<>(500, "Lỗi khi tính lại giá: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> validateSession(Integer sessionId, Integer userId) {
        try {
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            CheckoutSession session = sessionOpt.get();
            
            // Check expiry
            if (!session.isActive()) {
                return new ApiResponse<>(400, "Checkout session đã hết hạn", null);
            }

            // Validate items (stock, flash sale still valid, etc.)
            List<String> validationErrors = validateSessionItems(session);
            if (!validationErrors.isEmpty()) {
                return new ApiResponse<>(400, "Session validation failed: " + String.join(", ", validationErrors), null);
            }

            // Validate vouchers
            List<Integer> voucherIds = checkoutSessionMapper.parseVoucherIds(session.getSelectedVoucherIds());
            if (voucherIds != null && !voucherIds.isEmpty()) {
                List<String> voucherErrors = validateSessionVouchers(voucherIds, userId);
                if (!voucherErrors.isEmpty()) {
                    return new ApiResponse<>(400, "Voucher validation failed: " + String.join(", ", voucherErrors), null);
                }
            }

            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(session);
            
            log.info("Successfully validated session: {}", sessionId);
            return new ApiResponse<>(200, "Validate session thành công", response);
            
        } catch (Exception e) {
            log.error("Error validating session: {}", sessionId, e);
            return new ApiResponse<>(500, "Lỗi khi validate session: " + e.getMessage(), null);
        }
    }

    
    @Override
    public ApiResponse<String> createOrderFromSession(Integer sessionId, Integer userId) {
        try {
            log.info("🛒 Creating order from checkout session: {} for user: {}", sessionId, userId);

            // 1. KIỂM TRA SESSION TỒN TẠI VÀ QUYỀN TRUY CẬP
            CheckoutSession session = getSessionEntity(sessionId, userId);
            if (session == null) {
                log.error("❌ Session {} not found for user {}", sessionId, userId);
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            // 2. VALIDATE TOÀN BỘ SESSION TRƯỚC KHI TẠO ORDER
            ApiResponse<String> validationResult = performComprehensiveSessionValidation(session, userId);
            if (validationResult.getStatus() != 200) {
                log.error("❌ Session validation failed for session {}: {}", sessionId, validationResult.getMessage());
                return new ApiResponse<>(validationResult.getStatus(), validationResult.getMessage(), null);
            }

            // 3. LOCK TẠM THỜI KHI TẠO ORDER (để tránh race condition)
            //synchronized  : tránh việc tạo nhiều đơn hàng từ cùng một session do các request chạy song song.
            synchronized (this) {
                // Double-check session vẫn còn active
                session = getSessionEntity(sessionId, userId);
                if (session == null || !session.isActive()) {
                    log.error("❌ Session {} no longer active or available for user {}", sessionId, userId);
                    return new ApiResponse<>(400, "Session đã hết hạn hoặc không khả dụng", null);
                }

                // 4. TẠO ORDER REQUEST VỚI ENHANCED VALIDATION
                OrderRequest orderRequest;
                try {
                    orderRequest = buildOrderRequestFromSession(session);
                    log.info("🔄 Built order request with {} items, {} vouchers", 
                        orderRequest.getOrderDetails().size(), 
                        orderRequest.getVoucherIds() != null ? orderRequest.getVoucherIds().size() : 0);
                } catch (Exception buildEx) {
                    log.error("❌ Failed to build order request from session {}: {}", sessionId, buildEx.getMessage(), buildEx);
                    return new ApiResponse<>(400, "Lỗi khi xây dựng đơn hàng: " + buildEx.getMessage(), null);
                }

                // 5. GỌI ORDER SERVICE VỚI ERROR HANDLING CHI TIẾT
                ApiResponse<org.datn.bookstation.dto.response.OrderResponse> orderResponse;
                try {
                    log.info("🔄 Calling orderService.create for session {}", sessionId);
                    orderResponse = orderService.create(orderRequest);
                    log.info("🔄 OrderService.create returned status: {} for session {}", 
                        orderResponse != null ? orderResponse.getStatus() : "null", sessionId);
                } catch (Exception ex) {
                    log.error("❌ Exception khi gọi orderService.create for session {}: {}", sessionId, ex.getMessage(), ex);
                    
                    // Phân tích chi tiết lỗi từ exception
                    String detailedError = analyzeExceptionForUserMessage(ex);
                    return new ApiResponse<>(500, detailedError, null);
                }

                if (orderResponse == null) {
                    log.error("❌ OrderService.create returned null response for session {}", sessionId);
                    return new ApiResponse<>(500, "Không nhận được phản hồi từ hệ thống tạo đơn hàng", null);
                }

                if (orderResponse.getStatus() != 201) {
                    log.error("❌ Order creation failed for session {}: status={}, message={}", 
                        sessionId, orderResponse.getStatus(), orderResponse.getMessage());
                    String detailedMessage = analyzeOrderCreationError(orderResponse.getMessage(), session);
                    return new ApiResponse<>(orderResponse.getStatus(), detailedMessage, null);
                }

                // 6. MARK SESSION COMPLETED VÀ CLEANUP
                try {
                    markSessionCompleted(sessionId, userId);
                    log.info("✅ Session {} marked as completed", sessionId);
                } catch (Exception markEx) {
                    log.warn("⚠️ Failed to mark session {} as completed, but order was created successfully: {}", 
                        sessionId, markEx.getMessage());
                }

                // 7. 🔄 CLEAR CART AFTER SUCCESSFUL ORDER CREATION
                try {
                    cartService.clearCart(userId);
                    log.info("✅ Cart cleared for user {} after successful order creation", userId);
                } catch (Exception cartEx) {
                    // Không throw error vì order đã tạo thành công, chỉ log warning
                    log.warn("⚠️ Failed to clear cart for user {} after order creation: {}", userId, cartEx.getMessage());
                }

                String orderCode = orderResponse.getData().getCode();
                log.info("✅ Successfully created order: {} from session: {}", orderCode, sessionId);

                return new ApiResponse<>(201, "Đặt hàng thành công! Mã đơn hàng: " + orderCode, orderCode);
            }

        } catch (Exception e) {
            log.error("💥 Critical error creating order from session {}: {}", sessionId, e.getMessage(), e);
            
            // Phân tích chi tiết lỗi cho user
            String userFriendlyError = analyzeExceptionForUserMessage(e);
            return new ApiResponse<>(500, userFriendlyError, null);
        }
    }

    /**
     * Validate toàn bộ session trước khi tạo order - bắt tất cả edge case
     */
    private ApiResponse<String> performComprehensiveSessionValidation(CheckoutSession session, Integer userId) {
        List<String> errors = new ArrayList<>();
        
        try {
            // 1. Kiểm tra session còn active, frontend cho chuyển về cart để handle again
            if (!session.isActive()) {
                if (session.isExpired()) {
                    errors.add("Phiên checkout đã hết hạn. Vui lòng tạo phiên mới.");
                } else if (session.getStatus() == 2) {
                    errors.add("Phiên checkout này đã được sử dụng để tạo đơn hàng. Vui lòng tạo phiên checkout mới.");
                } else {
                    errors.add("Phiên checkout không hợp lệ.");
                }
            }

            // 2. Kiểm tra địa chỉ giao hàng
            if (session.getAddress() == null) {
                errors.add("Chưa chọn địa chỉ giao hàng.");
            }

            // 3. Parse và validate items
            List<CheckoutSessionRequest.BookQuantity> items = parseCheckoutItems(session.getCheckoutItems());
            if (items == null || items.isEmpty()) {
                errors.add("Không có sản phẩm nào để đặt hàng.");
            } else {
                // Validate từng item chi tiết
                List<String> itemErrors = validateSessionItemsForOrder(items, userId);
                errors.addAll(itemErrors);
            }

            // 4. Validate vouchers nếu có
            List<Integer> voucherIds = checkoutSessionMapper.parseVoucherIds(session.getSelectedVoucherIds());
            if (voucherIds != null && !voucherIds.isEmpty()) {
                List<String> voucherErrors = validateVouchersForOrder(voucherIds, userId, session);
                errors.addAll(voucherErrors);
            }

            // 5. Kiểm tra tổng tiền hợp lệ
            if (session.getTotalAmount() == null || session.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Tổng tiền đơn hàng không hợp lệ.");
            }

            if (errors.isEmpty()) {
                return new ApiResponse<>(200, "Validation passed", "OK");
            } else {
                String errorMessage = "❌ Có lỗi khi kiểm tra đơn hàng: " + String.join("; ", errors);
                log.warn("Session validation failed for session {}: {}", session.getId(), errorMessage);
                return new ApiResponse<>(400, errorMessage, null);
            }
            
        } catch (Exception e) {
            log.error("Error during session validation: ", e);
            return new ApiResponse<>(500, "Lỗi khi kiểm tra thông tin đơn hàng", null);
        }
    }

    /**
     * Validate items với các edge case thực tế
     */
private List<String> validateSessionItemsForOrder(List<CheckoutSessionRequest.BookQuantity> items, Integer userId) {
        List<String> errors = new ArrayList<>();
        
        for (CheckoutSessionRequest.BookQuantity item : items) {
            try {
                Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
                if (bookOpt.isEmpty()) {
                    errors.add("Sách ID " + item.getBookId() + " không tồn tại");
                    continue;
                }
                Book book = bookOpt.get();
                if (book.getStatus() == null || book.getStatus() != 1) {
                    errors.add("Sách '" + book.getBookName() + "' đã ngừng bán");
                    continue;
                }
                if (book.getStockQuantity() < item.getQuantity()) {
                    errors.add("Sách '" + book.getBookName() + "' chỉ còn " + book.getStockQuantity() + " cuốn trong kho");
                    continue;
                }
                // Backend sẽ tự kiểm tra flash sale khi tạo đơn
            } catch (Exception e) {
                log.error("Error validating item {}: ", item.getBookId(), e);
                errors.add("Lỗi khi kiểm tra sản phẩm ID " + item.getBookId() + ": " + e.getMessage());
            }
        }
        return errors;
    }

    // TODO: Remove unused flash sale validation methods after DTO refactor

    /**
     * Validate vouchers với edge case thực tế  
     */
    private List<String> validateVouchersForOrder(List<Integer> voucherIds, Integer userId, CheckoutSession session) {
        List<String> errors = new ArrayList<>();
        
        try {
            for (Integer voucherId : voucherIds) {
                Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
                if (voucherOpt.isEmpty()) {
                    errors.add("Voucher ID " + voucherId + " không tồn tại");
                    continue;
                }

                Voucher voucher = voucherOpt.get();
                long currentTime = System.currentTimeMillis();

                // Kiểm tra voucher active
                if (voucher.getStatus() != 1) {
                    errors.add("Voucher '" + voucher.getCode() + "' đã bị vô hiệu hóa");
                    continue;
                }

                // Kiểm tra thời gian voucher - EDGE CASE: Hết hạn đúng lúc đặt hàng
                if (currentTime < voucher.getStartTime()) {
                    errors.add("Voucher '" + voucher.getCode() + "' chưa có hiệu lực");
                } else if (currentTime > voucher.getEndTime()) {
                    errors.add("Voucher '" + voucher.getCode() + "' đã hết hạn");
                }

                // Kiểm tra đơn tối thiểu
                if (voucher.getMinOrderValue() != null && 
                    session.getSubtotal().compareTo(voucher.getMinOrderValue()) < 0) {
                    errors.add("Voucher '" + voucher.getCode() + "' yêu cầu đơn hàng tối thiểu " + voucher.getMinOrderValue());
                }

                // Kiểm tra số lượng sử dụng
                if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                    errors.add("Voucher '" + voucher.getCode() + "' đã hết lượt sử dụng");
                }

                // Kiểm tra limit per user
                if (!voucherCalculationService.canUserUseVoucher(userId, voucherId)) {
                    errors.add("Bạn đã sử dụng hết lượt cho voucher '" + voucher.getCode() + "'");
                }
            }
            
        } catch (Exception e) {
            log.error("Error validating vouchers: ", e);
            errors.add("Lỗi khi kiểm tra voucher");
        }
        
        return errors;
    }

    /**
     * Phân tích lỗi từ order creation để trả về message cụ thể
     */
    private String analyzeOrderCreationError(String originalError, CheckoutSession session) {
        if (originalError == null) return "Lỗi không xác định khi tạo đơn hàng";
        
        String lowerError = originalError.toLowerCase();
        
        // Stock issues
        if (lowerError.contains("stock") || lowerError.contains("kho") || lowerError.contains("hết hàng")) {
            return "❌ Một số sản phẩm đã hết hàng. Vui lòng cập nhật lại giỏ hàng.";
        }
        
        // Flash sale issues  
        if (lowerError.contains("flash sale") || lowerError.contains("khuyến mãi")) {
            return "❌ Flash sale đã kết thúc hoặc hết hàng. Vui lòng kiểm tra lại.";
        }
        
        // Voucher issues
        if (lowerError.contains("voucher") || lowerError.contains("mã giảm giá")) {
            return "❌ Voucher không hợp lệ hoặc đã hết hạn. Vui lòng kiểm tra lại.";
        }
        
        // Price issues
        if (lowerError.contains("price") || lowerError.contains("giá")) {
            return "❌ Giá sản phẩm đã thay đổi. Vui lòng cập nhật lại đơn hàng.";
        }
        
        return "❌ " + originalError;
    }

    /**
     * Lấy giá flash sale hiện tại với validation chi tiết
     */
    private BigDecimal getCurrentFlashSalePrice(Integer flashSaleItemId) {
        if (flashSaleItemId == null) {
            log.warn("getCurrentFlashSalePrice called with null flashSaleItemId");
            return null;
        }
        
        try {
            Optional<FlashSaleItem> flashSaleOpt = flashSaleItemRepository.findById(flashSaleItemId);
            if (flashSaleOpt.isEmpty()) {
                log.warn("FlashSaleItem not found for ID: {}", flashSaleItemId);
                return null;
            }
            
            FlashSaleItem item = flashSaleOpt.get();
            FlashSale flashSale = item.getFlashSale();
            
            if (flashSale == null) {
                log.warn("FlashSale is null for FlashSaleItem ID: {}", flashSaleItemId);
                return null;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // Kiểm tra flash sale còn active
            if (flashSale.getStatus() != 1) {
                log.debug("FlashSale is not active (status={}) for item ID: {}", flashSale.getStatus(), flashSaleItemId);
                return null;
            }
            
            if (currentTime < flashSale.getStartTime()) {
                log.debug("FlashSale has not started yet for item ID: {}", flashSaleItemId);
                return null;
            }
            
            if (currentTime > flashSale.getEndTime()) {
                log.debug("FlashSale has ended for item ID: {}", flashSaleItemId);
                return null;
            }
            
            // Kiểm tra flash sale item status
            if (item.getStatus() != 1) {
                log.debug("FlashSaleItem is not active (status={}) for item ID: {}", item.getStatus(), flashSaleItemId);
                return null;
            }
            
            // Kiểm tra stock
            if (item.getStockQuantity() <= 0) {
                log.debug("FlashSaleItem is out of stock for item ID: {}", flashSaleItemId);
                return null;
            }
            
            BigDecimal discountPrice = item.getDiscountPrice();
            if (discountPrice == null || discountPrice.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("Invalid discount price for FlashSaleItem ID: {}", flashSaleItemId);
                return null;
            }
            
            return discountPrice;
            
        } catch (Exception e) {
            log.error("Error getting current flash sale price for item ID {}: {}", flashSaleItemId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int cleanupExpiredSessions() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Mark expired sessions as expired
            int markedExpired = checkoutSessionRepository.markExpiredSessions(currentTime);
            
            // Delete sessions that have been expired for more than 7 days
            long sevenDaysAgo = currentTime - (7 * 24 * 60 * 60 * 1000L);
            int deletedOld = checkoutSessionRepository.deleteOldExpiredSessions(sevenDaysAgo);
            
            log.info("Cleanup completed: {} sessions marked as expired, {} old sessions deleted", markedExpired, deletedOld);
            return markedExpired + deletedOld;
            
        } catch (Exception e) {
            log.error("Error during session cleanup", e);
            return 0;
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> extendSessionExpiry(Integer sessionId, Integer userId, Long additionalMinutes) {
        try {
            Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findByIdAndUserId(sessionId, userId);
            if (sessionOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy checkout session", null);
            }

            CheckoutSession session = sessionOpt.get();
            
            // Extend expiry time
            long additionalMillis = additionalMinutes * 60 * 1000L;
            session.setExpiresAt(session.getExpiresAt() + additionalMillis);
            
            CheckoutSession savedSession = checkoutSessionRepository.save(session);
            CheckoutSessionResponse response = checkoutSessionResponseMapper.toResponse(savedSession);
            
            log.info("Successfully extended session expiry: {} by {} minutes", sessionId, additionalMinutes);
            return new ApiResponse<>(200, "Gia hạn session thành công", response);
            
        } catch (Exception e) {
            log.error("Error extending session expiry: {}", sessionId, e);
            return new ApiResponse<>(500, "Lỗi khi gia hạn session: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CheckoutSessionResponse> createSessionFromCart(Integer userId) {
        try {
            log.info("Creating checkout session from cart for user: {}", userId);

            // Get cart items
            List<CartItemResponse> cartItems = cartItemService.getCartItemsByUserId(userId);
            if (cartItems.isEmpty()) {
                return new ApiResponse<>(400, "Giỏ hàng trống, không thể tạo checkout session", null);
            }

            // Convert cart items to checkout items (chỉ lấy selected=true)
            List<CheckoutSessionRequest.BookQuantity> checkoutItems = new ArrayList<>();
            for (CartItemResponse cartItem : cartItems) {
                if (Boolean.FALSE.equals(cartItem.getSelected())) {
                    continue; // Bỏ qua sản phẩm chưa chọn
                }
                if (cartItem.isOutOfStock()) {
                    continue; // Bỏ qua sản phẩm hết hàng
                }

                log.info("🔍 DEBUGGING Cart Item: bookId={}, flashSaleItemId={}, itemType={}, unitPrice={}, flashSalePrice={}", 
                    cartItem.getBookId(), cartItem.getFlashSaleItemId(), cartItem.getItemType(), 
                    cartItem.getUnitPrice(), cartItem.getFlashSalePrice());

                CheckoutSessionRequest.BookQuantity item = new CheckoutSessionRequest.BookQuantity();
                item.setBookId(cartItem.getBookId());
                item.setQuantity(cartItem.getQuantity());
                
                // Note: BookQuantity chỉ cần bookId và quantity
                // Flash sale logic và pricing sẽ được xử lý trong calculateSessionPricing()
                
                log.info("🎯 Added checkout item: bookId={}, quantity={}", 
                    item.getBookId(), item.getQuantity());

                checkoutItems.add(item);
            }

            if (checkoutItems.isEmpty()) {
                return new ApiResponse<>(400, "Không có sản phẩm nào được chọn để checkout", null);
            }

            // Create checkout session request
            CheckoutSessionRequest request = new CheckoutSessionRequest();
            request.setItems(checkoutItems);

            // Create session
            return createCheckoutSession(userId, request);
            
        } catch (Exception e) {
            log.error("Error creating session from cart for user: {}", userId, e);
            return new ApiResponse<>(500, "Lỗi khi tạo session từ giỏ hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public CheckoutSession getSessionEntity(Integer sessionId, Integer userId) {
        return checkoutSessionRepository.findByIdAndUserId(sessionId, userId).orElse(null);
    }

    // Private helper methods

    private void validateCheckoutSessionRequest(CheckoutSessionRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm không được để trống");
        }

        for (CheckoutSessionRequest.BookQuantity item : request.getItems()) {
            if (item.getBookId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ");
            }
        }
    }

    private void calculateSessionPricing(CheckoutSession session, CheckoutSessionRequest request) {
        // � BACKEND TỰ TÍNH TOÁN MỌI THỨ - KHÔNG TIN FRONTEND
        log.info("🔄 Backend recalculating session pricing for {} items", 
            request.getItems() != null ? request.getItems().size() : 0);
        
        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("No items to calculate pricing for session");
            return;
        }
        
        BigDecimal calculatedSubtotal = BigDecimal.ZERO;
        
        // 🔥 TỰ TÍNH GIÁ CHO TỪNG ITEM - KHÔNG TIN FRONTEND
        for (CheckoutSessionRequest.BookQuantity item : request.getItems()) {
            try {
                // 1. Validate book exists
                Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
                if (bookOpt.isEmpty()) {
                    throw new RuntimeException("Sách ID " + item.getBookId() + " không tồn tại");
                }
                Book book = bookOpt.get();
                
                // 2. TỰ ĐỘNG TÌM FLASH SALE TỐT NHẤT
                BigDecimal unitPrice = book.getPrice(); // Default price
                Optional<FlashSaleItem> bestFlashSaleOpt = flashSaleItemRepository
                    .findActiveFlashSalesByBookId(item.getBookId().longValue(), System.currentTimeMillis())
                    .stream()
                    .filter(fs -> fs.getStockQuantity() >= item.getQuantity())
                    .findFirst();
                
                if (bestFlashSaleOpt.isPresent()) {
                    FlashSaleItem flashSale = bestFlashSaleOpt.get();
                    unitPrice = flashSale.getDiscountPrice();
                    log.info("✅ Applied flash sale for book {}: regular={}, flash={}", 
                        item.getBookId(), book.getPrice(), unitPrice);
                } else {
                    log.info("💰 Using regular price for book {}: {}", item.getBookId(), unitPrice);
                }
                
                // 3. TÍNH TỔNG TIỀN CHO ITEM
                BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                calculatedSubtotal = calculatedSubtotal.add(itemTotal);
                
                log.debug("Item {}: quantity={}, unitPrice={}, itemTotal={}", 
                    item.getBookId(), item.getQuantity(), unitPrice, itemTotal);
                    
            } catch (Exception e) {
                log.error("Error calculating price for item {}: {}", item.getBookId(), e.getMessage());
                throw new RuntimeException("Lỗi tính giá sản phẩm ID " + item.getBookId() + ": " + e.getMessage());
            }
        }
        
        session.setSubtotal(calculatedSubtotal);
        log.info("🔄 Calculated subtotal: {}", calculatedSubtotal);
        
        // 4. TỰ TÍNH SHIPPING FEE (không tin frontend)
        BigDecimal shippingFee = BigDecimal.ZERO;
        // TODO: Implement shipping calculation logic based on address/weight
        // For now, use default shipping fee or calculate based on business rules
        session.setShippingFee(shippingFee);
        
        // 5. VALIDATE VÀ TÍNH VOUCHER DISCOUNT  
        // TODO: Add voucher calculation when DTO supports it
        session.setTotalDiscount(BigDecimal.ZERO);
        
        // 6. TÍNH TỔNG CUỐI CÙNG
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalAmount = calculatedSubtotal.add(shippingFee).subtract(totalDiscount);
        session.setTotalAmount(totalAmount.max(BigDecimal.ZERO));
        
        log.info("🔄 Final pricing: subtotal={}, shipping={}, total={}", 
            calculatedSubtotal, shippingFee, session.getTotalAmount());
    }
    
    // TODO: Remove unused methods after DTO refactor

    // TODO: Update parseCheckoutItems to work with new DTO structure
    private List<CheckoutSessionRequest.BookQuantity> parseCheckoutItems(String checkoutItemsJson) {
        if (checkoutItemsJson == null || checkoutItemsJson.trim().isEmpty()) {
            log.error("parseCheckoutItems called with null or empty JSON");
            return null;
        }
        
        try {
            List<CheckoutSessionRequest.BookQuantity> items = objectMapper.readValue(
                checkoutItemsJson, 
                new TypeReference<List<CheckoutSessionRequest.BookQuantity>>() {}
            );
            
            if (items == null) {
                log.error("Parsed checkout items is null");
                return null;
            }
            
            log.debug("Successfully parsed {} checkout items", items.size());
            
            // Validate basic structure of parsed items
            for (int i = 0; i < items.size(); i++) {
                CheckoutSessionRequest.BookQuantity item = items.get(i);
                if (item == null) {
                    log.error("Checkout item at index {} is null", i);
                    continue;
                }
                if (item.getBookId() == null) {
                    log.error("Checkout item at index {} has null bookId", i);
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    log.error("Checkout item at index {} has invalid quantity: {}", i, item.getQuantity());
                }
            }
            
            return items;
            
        } catch (JsonProcessingException e) {
            log.error("Error parsing checkout items JSON: {}", e.getMessage());
            log.debug("Failed JSON content: {}", checkoutItemsJson);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error parsing checkout items: {}", e.getMessage(), e);
            return null;
        }
    }

    private List<String> validateSessionItems(CheckoutSession session) {
        List<String> errors = new ArrayList<>();
        
        try {
            List<CheckoutSessionRequest.BookQuantity> items = parseCheckoutItems(session.getCheckoutItems());
            if (items == null) {
                errors.add("Không thể parse checkout items");
                return errors;
            }

            for (CheckoutSessionRequest.BookQuantity item : items) {
                // Check book stock
                Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
                if (bookOpt.isEmpty()) {
                    errors.add("Sách ID " + item.getBookId() + " không tồn tại");
                    continue;
                }

                Book book = bookOpt.get();
                if (book.getStatus() != 1) {
                    errors.add("Sách '" + book.getBookName() + "' đã ngừng bán");
                }

                if (book.getStockQuantity() < item.getQuantity()) {
                    errors.add("Sách '" + book.getBookName() + "' chỉ còn " + book.getStockQuantity() + " cuốn");
                }

                // Backend sẽ tự kiểm tra flash sale khi cần, không cần validate ở đây
            }
            
        } catch (Exception e) {
            errors.add("Lỗi khi validate items: " + e.getMessage());
        }
        
        return errors;
    }

    private List<String> validateSessionVouchers(List<Integer> voucherIds, Integer userId) {
        List<String> errors = new ArrayList<>();
        
        try {
            for (Integer voucherId : voucherIds) {
                Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
                if (voucherOpt.isEmpty()) {
                    errors.add("Voucher ID " + voucherId + " không tồn tại");
                    continue;
                }

                Voucher voucher = voucherOpt.get();
                
                // Check voucher validity
                long currentTime = System.currentTimeMillis();
                if (voucher.getStatus() != 1) {
                    errors.add("Voucher " + voucher.getCode() + " đã bị vô hiệu hóa");
                }
                
                if (currentTime < voucher.getStartTime() || currentTime > voucher.getEndTime()) {
                    errors.add("Voucher " + voucher.getCode() + " đã hết hạn");
                }
                
                // Check usage limits
                if (!voucherCalculationService.canUserUseVoucher(userId, voucherId)) {
                    errors.add("Bạn đã sử dụng hết lượt cho voucher " + voucher.getCode());
                }
            }
            
        } catch (Exception e) {
            errors.add("Lỗi khi validate vouchers: " + e.getMessage());
        }
        
        return errors;
    }

    /**
     * Phân tích exception để trả về message thân thiện với user
     */
    private String analyzeExceptionForUserMessage(Exception ex) {
        if (ex == null) return "Lỗi không xác định";
        
        String exMessage = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        String exType = ex.getClass().getSimpleName().toLowerCase();
        
        log.error("Analyzing exception: type={}, message={}", exType, exMessage);
        
        // Transaction rollback issues
        if (exMessage.contains("rollback") || exMessage.contains("transaction")) {
            return "❌ Có lỗi trong quá trình xử lý đơn hàng. Vui lòng kiểm tra lại thông tin và thử lại.";
        }
        
        // Data integrity issues
        if (exMessage.contains("constraint") || exMessage.contains("foreign key") || exMessage.contains("unique")) {
            return "❌ Có xung đột dữ liệu. Vui lòng làm mới trang và thử lại.";
        }
        
        // Stock/inventory issues
        if (exMessage.contains("stock") || exMessage.contains("inventory") || exMessage.contains("quantity")) {
            return "❌ Sản phẩm đã hết hàng hoặc không đủ số lượng. Vui lòng cập nhật lại giỏ hàng.";
        }
        
        // Flash sale issues
        if (exMessage.contains("flash sale") || exMessage.contains("flashsale")) {
            return "❌ Flash sale đã kết thúc hoặc có vấn đề. Vui lòng kiểm tra lại.";
        }
        
        // Voucher issues
        if (exMessage.contains("voucher") || exMessage.contains("discount")) {
            return "❌ Có lỗi với voucher/mã giảm giá. Vui lòng bỏ voucher và thử lại.";
        }
        
        // Price/calculation issues
        if (exMessage.contains("price") || exMessage.contains("calculation") || exMessage.contains("amount")) {
            return "❌ Có vấn đề với tính toán giá. Vui lòng cập nhật lại đơn hàng.";
        }
        
        // User/permission issues
        if (exMessage.contains("user") || exMessage.contains("permission") || exMessage.contains("unauthorized")) {
            return "❌ Có vấn đề với tài khoản. Vui lòng đăng nhập lại.";
        }
        
        // Address issues
        if (exMessage.contains("address") || exMessage.contains("shipping")) {
            return "❌ Có vấn đề với địa chỉ giao hàng. Vui lòng kiểm tra lại địa chỉ.";
        }
        
        // Database connection issues
        if (exMessage.contains("connection") || exMessage.contains("timeout") || exMessage.contains("database")) {
            return "❌ Lỗi kết nối hệ thống. Vui lòng thử lại sau ít phút.";
        }
        
        // JSON/parsing issues
        if (exMessage.contains("json") || exMessage.contains("parse") || exMessage.contains("format")) {
            return "❌ Có lỗi dữ liệu. Vui lòng làm mới trang và thử lại.";
        }
        
        // Null pointer issues
        if (exType.contains("nullpointer") || exMessage.contains("null")) {
            return "❌ Thiếu thông tin bắt buộc. Vui lòng kiểm tra lại thông tin đơn hàng.";
        }
        
        // Runtime issues
        if (exType.contains("runtime") || exType.contains("illegal")) {
            return "❌ Có lỗi xử lý. Vui lòng kiểm tra lại thông tin và thử lại.";
        }
        
        // Generic fallback with partial original message for debugging
        String shortMessage = ex.getMessage();
        if (shortMessage != null && shortMessage.length() > 100) {
            shortMessage = shortMessage.substring(0, 100) + "...";
        }
        
        return "❌ Lỗi hệ thống: " + (shortMessage != null ? shortMessage : "Vui lòng thử lại sau");
    }

    private OrderRequest buildOrderRequestFromSession(CheckoutSession session) {
        log.info("🔄 Building OrderRequest from session: {}", session.getId());
        
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setUserId(session.getUser().getId());
        
        // Validate and set address
        if (session.getAddress() == null || session.getAddress().getId() == null) {
            throw new RuntimeException("Thiếu thông tin địa chỉ giao hàng");
        }
        orderRequest.setAddressId(session.getAddress().getId());

        // Ensure shippingFee is not null
        BigDecimal shippingFee = session.getShippingFee();
        if (shippingFee == null) {
            shippingFee = BigDecimal.ZERO;
            log.warn("ShippingFee was null for session {}, setting to ZERO", session.getId());
        }
        orderRequest.setShippingFee(shippingFee);

        // Set subtotal and totalAmount from session to avoid validation errors
        if (session.getSubtotal() != null) {
            orderRequest.setSubtotal(session.getSubtotal());
        } else {
            log.warn("Subtotal is null for session {}, setting to ZERO", session.getId());
            orderRequest.setSubtotal(BigDecimal.ZERO);
        }
        if (session.getTotalAmount() != null) {
            orderRequest.setTotalAmount(session.getTotalAmount());
        } else {
            log.warn("TotalAmount is null for session {}, setting to ZERO", session.getId());
            orderRequest.setTotalAmount(BigDecimal.ZERO);
        }

        orderRequest.setOrderType("ONLINE"); // Default order type for checkout sessions
        orderRequest.setNotes(session.getNotes());

        // Log session data for debugging
        log.debug("Building OrderRequest from session {}: subtotal={}, totalAmount={}, shippingFee={}", 
            session.getId(), session.getSubtotal(), session.getTotalAmount(), shippingFee);

        // Set voucher IDs
        List<Integer> voucherIds = checkoutSessionMapper.parseVoucherIds(session.getSelectedVoucherIds());
        orderRequest.setVoucherIds(voucherIds);

        // Convert checkout items to order details with BACKEND-DRIVEN validation
        List<CheckoutSessionRequest.BookQuantity> checkoutItems = parseCheckoutItems(session.getCheckoutItems());
        if (checkoutItems == null || checkoutItems.isEmpty()) {
            throw new RuntimeException("Không có sản phẩm nào để đặt hàng");
        }
        
        List<OrderDetailRequest> orderDetails = new ArrayList<>();

        for (CheckoutSessionRequest.BookQuantity item : checkoutItems) {
            try {
                OrderDetailRequest detail = new OrderDetailRequest();
                detail.setBookId(item.getBookId());
                detail.setQuantity(item.getQuantity());

                // 🔄 ENHANCED: Handle flash sale logic properly
                BigDecimal unitPrice = null;
                
                // Validate book exists first
                Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
                if (bookOpt.isEmpty()) {
                    throw new RuntimeException("Không tìm thấy sản phẩm ID: " + item.getBookId());
                }
                Book book = bookOpt.get();

                // 🔥 BACKEND TỰ QUYẾT ĐỊNH FLASH SALE - KHÔNG TIN FRONTEND
                log.info("🔍 AUTO-DETECTING flash sale for book: {}", item.getBookId());
                
                // Tự động tìm flash sale tốt nhất cho book này
                Optional<FlashSaleItem> bestFlashSaleOpt = flashSaleItemRepository
                    .findActiveFlashSalesByBookId(item.getBookId().longValue(), System.currentTimeMillis())
                    .stream()
                    .filter(fs -> fs.getStockQuantity() >= item.getQuantity())
                    .findFirst();
                
                if (bestFlashSaleOpt.isPresent()) {
                    // Có flash sale active và đủ stock
                    FlashSaleItem flashSaleItem = bestFlashSaleOpt.get();
                    unitPrice = flashSaleItem.getDiscountPrice();
                    detail.setFlashSaleItemId(flashSaleItem.getId());
                    log.info("✅ Applied flash sale for book {}: price={}, flashSaleItemId={}", 
                        item.getBookId(), unitPrice, flashSaleItem.getId());
                } else {
                    // Không có flash sale hoặc hết stock - dùng giá gốc
                    unitPrice = book.getPrice();
                    detail.setFlashSaleItemId(null);
                    log.info("💰 Using regular price for book {}: price={}", item.getBookId(), unitPrice);
                }

                // Final price validation
                if (unitPrice == null) {
                    throw new RuntimeException("Không thể xác định giá cho sản phẩm: " + book.getBookName());
                }
                
                // 🔥 BACKEND TỰ TÍNH GIÁ - KHÔNG TIN Frontend unitPrice
                // LUÔN LUÔN dùng giá backend tính toán, không tin frontend
                detail.setUnitPrice(unitPrice);

                orderDetails.add(detail);

                log.debug("Added order detail: bookId={}, quantity={}, unitPrice={}, flashSaleItemId={}", 
                    item.getBookId(), item.getQuantity(), detail.getUnitPrice(), detail.getFlashSaleItemId());
                    
            } catch (Exception itemEx) {
                log.error("Error processing checkout item bookId={} in session {}: {}", 
                    item.getBookId(), session.getId(), itemEx.getMessage(), itemEx);
                throw new RuntimeException("Lỗi xử lý sản phẩm ID " + item.getBookId() + ": " + itemEx.getMessage());
            }
        }

        orderRequest.setOrderDetails(orderDetails);
        log.info("✅ Successfully built OrderRequest with {} order details for session {}", 
            orderDetails.size(), session.getId());

        return orderRequest;
    }
}
