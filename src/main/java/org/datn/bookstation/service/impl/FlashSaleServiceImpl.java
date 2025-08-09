package org.datn.bookstation.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleDisplayResponse;
import org.datn.bookstation.dto.response.FlashSaleInfoResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.FlashSale;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.mapper.FlashSaleCustomMapper;
import org.datn.bookstation.mapper.FlashSaleMapper;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.FlashSaleRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.service.CartItemService;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.specification.FlashSaleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlashSaleServiceImpl implements FlashSaleService {

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    @Autowired
    private FlashSaleMapper flashSaleMapper;

    @Autowired
    private FlashSaleCustomMapper flashSaleCustomMapper;
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    @Lazy
    private CartItemService cartItemService;

    @Override
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllFlashSaleWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlashSale> flashSaleList = flashSaleRepository.findAll(pageable);
        List<FlashSaleResponse> flashSaleResponses = flashSaleList.getContent()
                .stream()
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());
        PaginationResponse<FlashSaleResponse> paginationResponse = new PaginationResponse<>(flashSaleResponses, page,
                size, flashSaleList.getTotalElements(), flashSaleList.getTotalPages());
        return new ApiResponse<>(200, "Lấy danh sách flash sale thành công", paginationResponse);
    }

    @Override
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllWithFilter(int page, int size, String name,
            Long from, Long to, Byte status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<FlashSale> specification = FlashSaleSpecification.filterBy(name, from, to, status);
        Page<FlashSale> flashSalePage = flashSaleRepository.findAll(specification, pageable);

        List<FlashSaleResponse> responses = flashSalePage.getContent()
                .stream()
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());

        PaginationResponse<FlashSaleResponse> pagination = PaginationResponse.<FlashSaleResponse>builder()
                .content(responses)
                .pageNumber(flashSalePage.getNumber())
                .pageSize(flashSalePage.getSize())
                .totalElements(flashSalePage.getTotalElements())
                .totalPages(flashSalePage.getTotalPages())
                .build();

        return new ApiResponse<>(200, "Lấy danh sách flash sale thành công", pagination);
    }

    @Override
    public ApiResponse<FlashSaleResponse> createFlashSale(FlashSaleRequest request) {
        try {
            // ✅ Validate tên không được rỗng
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return new ApiResponse<>(400, "Tên flash sale không được để trống", null);
            }
            if (request.getName().length() > 100) {
                return new ApiResponse<>(400, "Tên flash sale không được vượt quá 100 ký tự", null);
            }

            // ✅ Validate thời gian
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return new ApiResponse<>(400, "Thời gian bắt đầu/kết thúc không được để trống", null);
            }
            if (request.getStartTime() >= request.getEndTime()) {
                return new ApiResponse<>(400, "Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc", null);
            }
            if (request.getEndTime() < System.currentTimeMillis()) {
                return new ApiResponse<>(400, "Thời gian kết thúc phải lớn hơn hiện tại", null);
            }

            // ✅ Validate status
            if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
                return new ApiResponse<>(400, "Trạng thái chỉ được là 0 hoặc 1", null);
            }

            // ✅ Kiểm tra trùng thời gian flash sale
            List<FlashSale> overlaps = flashSaleRepository.findOverlappingFlashSales(request.getStartTime(),
                    request.getEndTime());
            if (!overlaps.isEmpty()) {
                return new ApiResponse<>(400, "Đã có sự kiện flash sale diễn ra trong khoảng thời gian này!", null);
            }

            FlashSale flashSale = flashSaleMapper.toFlashSale(request);
            flashSale.setCreatedAt(System.currentTimeMillis());
            flashSale.setUpdatedAt(System.currentTimeMillis());

            FlashSale savedFlashSale = flashSaleRepository.save(flashSale);

            // 🔥 AUTO SCHEDULE: Tự động schedule expiration task khi tạo flash sale
            if (savedFlashSale.getStatus() == 1 && savedFlashSale.getEndTime() > System.currentTimeMillis()) {
                scheduleFlashSaleExpiration(savedFlashSale.getId(), savedFlashSale.getEndTime());
            }

            return new ApiResponse<>(200, "Tạo flash sale thành công", flashSaleMapper.toResponse(savedFlashSale));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi tạo flash sale: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<FlashSaleResponse> updateFlashSale(FlashSaleRequest request, Integer id) {
        try {
            // Validate ID
            if (id == null || id <= 0) {
                return new ApiResponse<>(400, "ID flash sale không hợp lệ", null);
            }

            FlashSale flashSale = flashSaleRepository.findById(id).orElse(null);
            if (flashSale == null) {
                return new ApiResponse<>(404, "Flash sale không tồn tại", null);
            }

            // Validate các trường như trên
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return new ApiResponse<>(400, "Tên flash sale không được để trống", null);
            }
            if (request.getName().length() > 100) {
                return new ApiResponse<>(400, "Tên flash sale không được vượt quá 100 ký tự", null);
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                return new ApiResponse<>(400, "Thời gian bắt đầu/kết thúc không được để trống", null);
            }
            if (request.getStartTime() >= request.getEndTime()) {
                return new ApiResponse<>(400, "Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc", null);
            }
            if (request.getEndTime() < System.currentTimeMillis()) {
                return new ApiResponse<>(400, "Thời gian kết thúc phải lớn hơn hiện tại", null);
            }
            if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
                return new ApiResponse<>(400, "Trạng thái chỉ được là 0 hoặc 1", null);
            }

            List<FlashSale> overlaps = flashSaleRepository.findOverlappingFlashSales(request.getStartTime(),
                    request.getEndTime());
            boolean hasOverlap = overlaps.stream().anyMatch(fs -> !fs.getId().equals(id));
            if (hasOverlap) {
                return new ApiResponse<>(400, "Đã có sự kiện flash sale diễn ra trong khoảng thời gian này!", null);
            }

            // Update các trường
            flashSale.setName(request.getName());
            flashSale.setStartTime(request.getStartTime());
            flashSale.setEndTime(request.getEndTime());
            flashSale.setStatus(request.getStatus());
            flashSale.setUpdatedAt(System.currentTimeMillis());

            FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);

            // Schedule lại task mới với thời gian mới
            if (updatedFlashSale.getStatus() == 1 && updatedFlashSale.getEndTime() > System.currentTimeMillis()) {
                scheduleFlashSaleExpiration(updatedFlashSale.getId(), updatedFlashSale.getEndTime());
            }

            // ✅ AUTO-UPDATE status của flash sale items dựa trên thời gian mới
            try {
                int statusUpdatedCount = autoUpdateFlashSaleItemsStatus(updatedFlashSale.getId());
                System.out.println("🔄 FLASH SALE STATUS UPDATE: Updated " + statusUpdatedCount
                        + " items status for flash sale " + id);
            } catch (Exception e) {
                System.err.println(
                        "⚠️ WARNING: Failed to update status for flash sale items " + id + ": " + e.getMessage());
            }

            return new ApiResponse<>(200, "Cập nhật flash sale thành công",
                    flashSaleMapper.toResponse(updatedFlashSale));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật flash sale: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<FlashSaleResponse> toggleStatus(Integer id) {
        try {
            FlashSale flashSale = flashSaleRepository.findById(id).orElse(null);

            if (flashSale == null) {
                return new ApiResponse<>(404, "Flash sale không tồn tại", null);
            }

            // Cancel scheduled task trước khi toggle status
            cancelFlashSaleExpirationSchedule(id);

            flashSale.setStatus((byte) (flashSale.getStatus() == 1 ? 0 : 1));
            flashSale.setUpdatedAt(System.currentTimeMillis());
            FlashSale updatedFlashSale = flashSaleRepository.save(flashSale);

            // ✅ AUTO-UPDATE status của flash sale items sau khi toggle
            try {
                int statusUpdatedCount = autoUpdateFlashSaleItemsStatus(updatedFlashSale.getId());
                log.info("🔄 TOGGLE STATUS: Updated {} flash sale items for flash sale {}", statusUpdatedCount, id);
            } catch (Exception e) {
                log.warn("⚠️ WARNING: Failed to update flash sale items status after toggle: {}", e.getMessage());
            }

            // Chỉ schedule lại nếu status = 1 và chưa hết hạn
            if (updatedFlashSale.getStatus() == 1 && updatedFlashSale.getEndTime() > System.currentTimeMillis()) {
                scheduleFlashSaleExpiration(updatedFlashSale.getId(), updatedFlashSale.getEndTime());
            }

            return new ApiResponse<>(200, "Cập nhật trạng thái flash sale thành công",
                    flashSaleMapper.toResponse(updatedFlashSale));
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), null);
        }
    }

    // ================== METHODS HỖ TRỢ CART AUTO-DETECTION ==================

    @Override
    public Optional<FlashSaleItem> findActiveFlashSaleForBook(Long bookId) {
        long now = System.currentTimeMillis();
        // Business rule: chỉ có 1 flash sale active per book per time
        // Chỉ cần tìm flash sale đang hoạt động, không cần "chọn tốt nhất"
        List<FlashSaleItem> activeFlashSales = flashSaleItemRepository.findActiveFlashSalesByBookId(bookId, now);
        return activeFlashSales.stream()
                .filter(item -> item.getStockQuantity() > 0)
                .findFirst(); // Lấy cái đầu tiên vì chỉ có 1 active
    }

    @Override
    public FlashSaleInfoResponse getActiveFlashSaleInfo(Long bookId) {
        Optional<FlashSaleItem> flashSaleItem = findActiveFlashSaleForBook(bookId);

        if (flashSaleItem.isPresent()) {
            return convertToFlashSaleInfoResponse(flashSaleItem.get());
        }

        return null;
    }

    @Override
    public boolean isFlashSaleValid(Long flashSaleItemId) {
        long now = System.currentTimeMillis();
        Optional<FlashSaleItem> flashSaleItem = flashSaleItemRepository.findActiveFlashSaleItemById(flashSaleItemId,
                now);
        return flashSaleItem.isPresent();
    }

    @Override
    public boolean hasEnoughStock(Long flashSaleItemId, Integer quantity) {
        Optional<FlashSaleItem> flashSaleItem = flashSaleItemRepository.findById(flashSaleItemId);

        if (flashSaleItem.isPresent()) {
            return flashSaleItem.get().getStockQuantity() >= quantity;
        }

        return false;
    }

    // ================== SCHEDULER INTEGRATION METHODS ==================

    @Override
    public void scheduleFlashSaleExpiration(Integer flashSaleId, Long endTime) {
        try {
            // ✅ Sử dụng ApplicationContext để tránh circular dependency
            var scheduler = applicationContext.getBean("flashSaleExpirationScheduler",
                    org.datn.bookstation.scheduled.FlashSaleExpirationScheduler.class);
            scheduler.scheduleFlashSaleExpiration(flashSaleId, endTime);
        } catch (Exception e) {
            // Log error nhưng không throw exception để không ảnh hưởng business logic
            System.err.println("⚠️ WARNING: Failed to schedule flash sale expiration for ID " + flashSaleId + ": "
                    + e.getMessage());
        }
    }

    @Override
    public void cancelFlashSaleExpirationSchedule(Integer flashSaleId) {
        try {
            // ✅ Sử dụng ApplicationContext để tránh circular dependency
            var scheduler = applicationContext.getBean("flashSaleExpirationScheduler",
                    org.datn.bookstation.scheduled.FlashSaleExpirationScheduler.class);
            scheduler.cancelScheduledTask(flashSaleId);
        } catch (Exception e) {
            // Log error nhưng không throw exception
            System.err.println(
                    "⚠️ WARNING: Failed to cancel flash sale schedule for ID " + flashSaleId + ": " + e.getMessage());
        }
    }

    // ================== PRIVATE HELPER METHODS ==================

    private FlashSaleInfoResponse convertToFlashSaleInfoResponse(FlashSaleItem flashSaleItem) {
        long now = System.currentTimeMillis();
        long endTime = flashSaleItem.getFlashSale().getEndTime();

        long remainingSeconds = 0;
        if (endTime > now) {
            remainingSeconds = (endTime - now) / 1000;
        }

        // Lấy giá gốc từ book
        BigDecimal originalPrice = flashSaleItem.getBook().getPrice();

        return FlashSaleInfoResponse.builder()
                .flashSaleItemId(flashSaleItem.getId().longValue())
                .flashSaleId(flashSaleItem.getFlashSale().getId().longValue())
                .flashSaleName(flashSaleItem.getFlashSale().getName())
                .originalPrice(originalPrice)
                .discountPrice(flashSaleItem.getDiscountPrice())
                .discountAmount(originalPrice.subtract(flashSaleItem.getDiscountPrice()))
                .discountPercentage(flashSaleItem.getDiscountPercentage().doubleValue())
                .stockQuantity(flashSaleItem.getStockQuantity())
                .startTime(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(flashSaleItem.getFlashSale().getStartTime()),
                        java.time.ZoneId.systemDefault()))
                .endTime(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(flashSaleItem.getFlashSale().getEndTime()),
                        java.time.ZoneId.systemDefault()))
                .remainingSeconds(remainingSeconds)
                .isActive(flashSaleItem.getStatus() == 1 && flashSaleItem.getFlashSale().getStatus() == 1)
                .status(flashSaleItem.getStatus() == 1 ? "ACTIVE" : "INACTIVE")
                .build();
    }

    /**
     * ✅ NEW: Disable flash sale items instead of setting cart items to null
     * This preserves data integrity and allows re-enabling
     */
    @Override
    public int disableFlashSaleItems(Integer flashSaleId) {
        try {
            List<FlashSaleItem> flashSaleItems = flashSaleItemRepository.findByFlashSaleId(flashSaleId);

            long currentTime = System.currentTimeMillis();
            for (FlashSaleItem item : flashSaleItems) {
                item.setStatus((byte) 0); // Disable
                item.setUpdatedAt(currentTime);
                item.setUpdatedBy(1L); // System user
            }

            flashSaleItemRepository.saveAll(flashSaleItems);
            return flashSaleItems.size();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * ✅ NEW: Enable flash sale items when flash sale is extended
     */
    @Override
    public int enableFlashSaleItems(Integer flashSaleId) {
        try {
            List<FlashSaleItem> flashSaleItems = flashSaleItemRepository.findByFlashSaleId(flashSaleId);

            long currentTime = System.currentTimeMillis();
            for (FlashSaleItem item : flashSaleItems) {
                item.setStatus((byte) 1); // Enable
                item.setUpdatedAt(currentTime);
                item.setUpdatedBy(1L); // System user
            }

            flashSaleItemRepository.saveAll(flashSaleItems);
            return flashSaleItems.size();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * ✅ AUTO-UPDATE: Cập nhật status của FlashSaleItems dựa trên priority rules
     * 
     * PRIORITY RULES:
     * 1. flashSale.status = 0 → flashSaleItem.status = 0 (HIGHEST PRIORITY - Admin
     * override)
     * 2. flashSale.status = 1 + time valid → flashSaleItem.status = 1
     * 3. flashSale.status = 1 + time invalid → flashSaleItem.status = 0
     * 
     * CHỈ GỌI KHI ADMIN CẬP NHẬT FLASH SALE - KHÔNG SCHEDULED
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int autoUpdateFlashSaleItemsStatus() {
        try {
            long currentTime = System.currentTimeMillis();

            // ✅ FIX: Sử dụng JOIN FETCH để tránh LazyInitializationException
            List<FlashSaleItem> allItems = flashSaleItemRepository.findAllWithFlashSale();

            int updatedCount = 0;
            for (FlashSaleItem item : allItems) {
                FlashSale flashSale = item.getFlashSale();
                if (flashSale == null)
                    continue;

                Byte newStatus;
                String reason;

                // ✅ PRIORITY 1: Flash sale status = 0 → Force disable (Admin override)
                if (flashSale.getStatus() == 0) {
                    newStatus = (byte) 0;
                    reason = "flash sale disabled by admin";
                } else {
                    // ✅ PRIORITY 2: Flash sale status = 1 → Check time validity
                    boolean isTimeValid = (flashSale.getStartTime() <= currentTime) &&
                            (currentTime <= flashSale.getEndTime());

                    newStatus = isTimeValid ? (byte) 1 : (byte) 0;
                    reason = isTimeValid ? "active (valid time)"
                            : (currentTime < flashSale.getStartTime() ? "not started yet" : "expired");
                }

                if (!newStatus.equals(item.getStatus())) {
                    item.setStatus(newStatus);
                    item.setUpdatedAt(currentTime);
                    item.setUpdatedBy(1L); // System user
                    flashSaleItemRepository.save(item);
                    updatedCount++;

                    log.info("🔄 AUTO-UPDATE: FlashSaleItem {} status = {} ({})",
                            item.getId(), newStatus, reason);
                }
            }

            return updatedCount;
        } catch (Exception e) {
            log.error("❌ ERROR: autoUpdateFlashSaleItemsStatus failed", e);
            return 0;
        }
    }

    /**
     * ✅ AUTO-UPDATE: Cập nhật status cho một flash sale cụ thể dựa trên thời gian
     * hiệu lực VÀ status flash sale
     * - Nếu flashSale.status = 0: Bắt buộc flashSaleItem.status = 0 (admin tắt khẩn
     * cấp)
     * - Nếu flashSale.status = 1: Kiểm tra thời gian hiệu lực
     * + startTime <= currentTime <= endTime: status = 1 (active - có hiệu lực)
     * + currentTime < startTime: status = 0 (chưa bắt đầu)
     * + currentTime > endTime: status = 0 (đã hết hạn)
     * 
     * CHỈ GỌI KHI ADMIN CẬP NHẬT FLASH SALE HOẶC KHI HẾT HẠN
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public int autoUpdateFlashSaleItemsStatus(Integer flashSaleId) {
        try {
            FlashSale flashSale = flashSaleRepository.findById(flashSaleId).orElse(null);
            if (flashSale == null) {
                log.warn("⚠️ FlashSale {} not found", flashSaleId);
                return 0;
            }

            long currentTime = System.currentTimeMillis();

            // ✅ FIX: Sử dụng custom query để tránh LazyInitializationException
            List<FlashSaleItem> items = flashSaleItemRepository.findByFlashSaleIdWithFlashSale(flashSaleId);

            Byte newStatus;
            String reason;

            // ✅ PRIORITY 1: Nếu admin tắt flash sale → tắt hết flash sale items
            if (flashSale.getStatus() == 0) {
                newStatus = (byte) 0;
                reason = "admin tắt flash sale";
            } else {
                // ✅ PRIORITY 2: Kiểm tra thời gian hiệu lực
                boolean isValid = (flashSale.getStartTime() <= currentTime) &&
                        (currentTime <= flashSale.getEndTime());

                newStatus = isValid ? (byte) 1 : (byte) 0;
                reason = currentTime < flashSale.getStartTime() ? "chưa bắt đầu"
                        : currentTime > flashSale.getEndTime() ? "đã hết hạn" : "đang hiệu lực";
            }

            int updatedCount = 0;
            for (FlashSaleItem item : items) {
                if (!newStatus.equals(item.getStatus())) {
                    item.setStatus(newStatus);
                    item.setUpdatedAt(currentTime);
                    item.setUpdatedBy(1L); // System user
                    flashSaleItemRepository.save(item);
                    updatedCount++;
                }
            }

            // Log kết quả update
            log.info("🔄 AUTO-UPDATE: FlashSale {} → {} items updated, status = {} ({})",
                    flashSaleId, updatedCount, newStatus, reason);

            return updatedCount;
        } catch (Exception e) {
            log.error("❌ ERROR: autoUpdateFlashSaleItemsStatus({}) failed", flashSaleId, e);
            return 0;
        }
    }

    @Override
    public ApiResponse<FlashSaleDisplayResponse> findFlashSalesByDate() {
        Long dateMillis = System.currentTimeMillis();
        FlashSale flashSale = flashSaleRepository
                .findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(dateMillis, dateMillis)
                .stream()
                .findFirst()
                .orElse(null);

        if (flashSale == null) {
            return new ApiResponse<>(404, "Không có flash sale nào trong ngày này", null);
        }
        return new ApiResponse<>(200, "Thành công", flashSaleCustomMapper.toDisplayResponse(flashSale));
    }

    /**
     * ✅ FIX: Kiểm tra user đã mua bao nhiêu flash sale item này
     * Tính từ OrderDetail với order DELIVERED trừ đi GOODS_RECEIVED_FROM_CUSTOMER
     */
    @Override
    public int getUserPurchasedQuantity(Long flashSaleItemId, Integer userId) {
        try {
            // Tính từ OrderDetail: DELIVERED - GOODS_RECEIVED_FROM_CUSTOMER
            return orderDetailRepository.calculateUserPurchasedQuantityForFlashSaleItem(flashSaleItemId.intValue(),
                    userId);
        } catch (Exception e) {
            log.error("Error getting user purchased quantity for flashSaleItem {} user {}: {}",
                    flashSaleItemId, userId, e.getMessage());
            return 0;
        }
    }

    /**
     * ✅ THÊM: Validate user có thể mua thêm số lượng này không
     */
    @Override
    public boolean canUserPurchaseMore(Long flashSaleItemId, Integer userId, Integer requestQuantity) {
        try {
            Optional<FlashSaleItem> flashSaleOpt = flashSaleItemRepository.findById(flashSaleItemId);
            if (flashSaleOpt.isEmpty()) {
                return false;
            }

            FlashSaleItem flashSaleItem = flashSaleOpt.get();
            // Nếu không có giới hạn per user thì cho phép mua
            if (flashSaleItem.getMaxPurchasePerUser() == null) {
                return true;
            }

            int alreadyPurchased = getUserPurchasedQuantity(flashSaleItemId, userId);
            int totalAfterPurchase = alreadyPurchased + requestQuantity;
            boolean canPurchase = totalAfterPurchase <= flashSaleItem.getMaxPurchasePerUser();

            log.info(
                    "Flash sale limit check - Item: {}, User: {}, Already: {}, Request: {}, Limit: {}, CanPurchase: {}",
                    flashSaleItemId, userId, alreadyPurchased, requestQuantity, flashSaleItem.getMaxPurchasePerUser(),
                    canPurchase);

            return canPurchase;
        } catch (Exception e) {
            log.error("Error checking user purchase limit for flashSaleItem {} user {}: {}",
                    flashSaleItemId, userId, e.getMessage());
            return false;
        }
    }
}
