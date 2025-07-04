package org.datn.bookstation.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.datn.bookstation.entity.FlashSale;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.mapper.FlashSaleMapper;
import org.datn.bookstation.repository.FlashSaleRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.scheduled.FlashSaleExpirationScheduler;
import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.datn.bookstation.dto.response.FlashSaleInfoResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.specification.FlashSaleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleServiceImpl implements FlashSaleService {

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    @Autowired
    private FlashSaleMapper flashSaleMapper;
    
    @Autowired
    private FlashSaleExpirationScheduler flashSaleExpirationScheduler;

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
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllWithFilter(int page, int size, String name, Long from, Long to, Byte status) {
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
            FlashSale flashSale = flashSaleMapper.toFlashSale(request);
            flashSale.setCreatedAt(System.currentTimeMillis());
            flashSale.setUpdatedAt(System.currentTimeMillis());
            // flashSale.setCreatedBy(1L);
            // flashSale.setUpdatedBy(1L);
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
            FlashSale flashSale = flashSaleRepository.findById(id).orElse(null);
            if (flashSale == null) {
                return new ApiResponse<>(404, "Flash sale không tồn tại", null);
            }
            
            // Cancel scheduled task cũ trước khi update
            cancelFlashSaleExpirationSchedule(id);
            
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
            
            return new ApiResponse<>(200, "Cập nhật flash sale thành công", flashSaleMapper.toResponse(updatedFlashSale));
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
            
            // Chỉ schedule lại nếu status = 1 và chưa hết hạn
            if (updatedFlashSale.getStatus() == 1 && updatedFlashSale.getEndTime() > System.currentTimeMillis()) {
                scheduleFlashSaleExpiration(updatedFlashSale.getId(), updatedFlashSale.getEndTime());
            }
            
            return new ApiResponse<>(200, "Cập nhật trạng thái flash sale thành công", flashSaleMapper.toResponse(updatedFlashSale));
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
        Optional<FlashSaleItem> flashSaleItem = flashSaleItemRepository.findActiveFlashSaleItemById(flashSaleItemId, now);
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
            // Tích hợp với FlashSaleExpirationScheduler để batch processing
            flashSaleExpirationScheduler.scheduleFlashSaleExpiration(flashSaleId, endTime);
        } catch (Exception e) {
            // Log error nhưng không throw exception để không ảnh hưởng business logic
            System.err.println("⚠️ WARNING: Failed to schedule flash sale expiration for ID " + flashSaleId + ": " + e.getMessage());
        }
    }
    
    @Override
    public void cancelFlashSaleExpirationSchedule(Integer flashSaleId) {
        try {
            flashSaleExpirationScheduler.cancelScheduledTask(flashSaleId);
        } catch (Exception e) {
            // Log error nhưng không throw exception
            System.err.println("⚠️ WARNING: Failed to cancel flash sale schedule for ID " + flashSaleId + ": " + e.getMessage());
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
                .startTime(LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(flashSaleItem.getFlashSale().getStartTime()), java.time.ZoneId.systemDefault()))
                .endTime(LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(flashSaleItem.getFlashSale().getEndTime()), java.time.ZoneId.systemDefault()))
                .remainingSeconds(remainingSeconds)
                .isActive(flashSaleItem.getStatus() == 1 && flashSaleItem.getFlashSale().getStatus() == 1)
                .status(flashSaleItem.getStatus() == 1 ? "ACTIVE" : "INACTIVE")
                .build();
    }
}
