package org.datn.bookstation.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.datn.bookstation.dto.request.FlashSaleItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleItemResponse;
import org.datn.bookstation.dto.response.FlashSaleItemStatsResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSale;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.mapper.BookFlashSaleMapper;
import org.datn.bookstation.mapper.FlashSaleItemMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.FlashSaleRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.service.FlashSaleItemService;
import org.datn.bookstation.service.CartItemService;
import org.datn.bookstation.specification.FlashSaleItemSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FlashSaleItemServiceImpl implements FlashSaleItemService {

    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private FlashSaleItemMapper flashSaleItemMapper;

    @Autowired
    @Lazy
    private CartItemService cartItemService;

    // ✅ THÊM dependency OrderDetailRepository
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public FlashSaleItem findActiveFlashSaleByBook(Integer bookId) {
        // Giả sử có phương thức findActiveFlashSaleByBook trong repository
        return flashSaleItemRepository.findActiveFlashSaleByBook(bookId);
    }

    @Override
    public ApiResponse<PaginationResponse<FlashSaleItemResponse>> getAllWithFilter(int page, int size,
            Integer flashSaleId, String bookName, Byte status,
            BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minPercent, BigDecimal maxPercent, Integer minQuantity,
            Integer maxQuantity) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<FlashSaleItem> spec = FlashSaleItemSpecification.filterBy(flashSaleId, bookName, status, minPrice,
                maxPrice, minPercent, maxPercent, minQuantity, maxQuantity);
        Page<FlashSaleItem> itemPage = flashSaleItemRepository.findAll(spec, pageable);

        List<FlashSaleItemResponse> content = itemPage.getContent().stream()
                .map(flashSaleItemMapper::toResponse)
                .collect(Collectors.toList());

        PaginationResponse<FlashSaleItemResponse> pagination = PaginationResponse.<FlashSaleItemResponse>builder()
                .content(content)
                .pageNumber(itemPage.getNumber())
                .pageSize(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .build();

        return new ApiResponse<>(200, "Lấy danh sách flash sale item thành công", pagination);
    }

    @Override
    public ApiResponse<FlashSaleItemResponse> create(FlashSaleItemRequest request) {
        // Validate đầu vào
        if (request.getFlashSaleId() == null || request.getBookId() == null) {
            return new ApiResponse<>(400, "Thiếu flashSaleId hoặc bookId", null);
        }
        if (request.getDiscountPrice() == null || request.getDiscountPrice().compareTo(BigDecimal.ZERO) < 0) {
            return new ApiResponse<>(400, "Giá khuyến mãi không hợp lệ", null);
        }
        if (request.getDiscountPercentage() != null
                && (request.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0
                        || request.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0)) {
            return new ApiResponse<>(400, "Phần trăm giảm giá phải từ 0 đến 100", null);
        }
        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            return new ApiResponse<>(400, "Số lượng tồn kho không hợp lệ", null);
        }
        if (request.getMaxPurchasePerUser() != null && request.getMaxPurchasePerUser() < 0) {
            return new ApiResponse<>(400, "Giới hạn mua mỗi user không hợp lệ", null);
        }

        // Kiểm tra flash sale và book tồn tại
        FlashSale flashSale = flashSaleRepository.findById(request.getFlashSaleId()).orElse(null);
        if (flashSale == null) {
            return new ApiResponse<>(404, "Flash sale không tồn tại", null);
        }
        Book book = bookRepository.findById(request.getBookId()).orElse(null);
        if (book == null) {
            return new ApiResponse<>(404, "Sách không tồn tại", null);
        }

        // ✅ VALIDATION MỚI: Kiểm tra số lượng flash sale không được lớn hơn tồn kho
        // sách
        if (request.getStockQuantity() > book.getStockQuantity()) {
            return new ApiResponse<>(400,
                    String.format("Số lượng flash sale (%d) không được lớn hơn tồn kho sách (%d)",
                            request.getStockQuantity(), book.getStockQuantity()),
                    null);
        }

        boolean exists = flashSaleItemRepository.existsByFlashSaleIdAndBookId(request.getFlashSaleId(),
                request.getBookId());
        if (exists) {
            return new ApiResponse<>(400, "Sách này đã có trong flash sale này!", null);
        }
        FlashSaleItem item = flashSaleItemMapper.toEntity(request);
        item.setFlashSale(flashSale);
        item.setBook(book);
        FlashSaleItem savedItem = flashSaleItemRepository.save(item);

        // 🔥 AUTO-SYNC: Tự động đồng bộ cart items khi tạo flash sale item mới
        try {
            int syncedCartCount = cartItemService.syncCartItemsWithNewFlashSale(flashSale.getId());
            log.info("🔄 AUTO-SYNC CART: Created flash sale item {} for book {}, synced {} cart items",
                    savedItem.getId(), book.getId(), syncedCartCount);
        } catch (Exception e) {
            log.warn("⚠️ WARNING: Failed to sync cart items after creating flash sale item {}: {}",
                    savedItem.getId(), e.getMessage());
        }

        return new ApiResponse<>(201, "Tạo flash sale item thành công", flashSaleItemMapper.toResponse(savedItem));
    }

    @Override
    public ApiResponse<FlashSaleItemResponse> update(Integer id, FlashSaleItemRequest request) {
        FlashSaleItem existing = flashSaleItemRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Flash sale item không tồn tại", null);
        }

        // Validate đầu vào
        if (request.getDiscountPrice() != null && request.getDiscountPrice().compareTo(BigDecimal.ZERO) < 0) {
            return new ApiResponse<>(400, "Giá khuyến mãi không hợp lệ", null);
        }
        if (request.getDiscountPercentage() != null
                && (request.getDiscountPercentage().compareTo(BigDecimal.ZERO) < 0
                        || request.getDiscountPercentage().compareTo(BigDecimal.valueOf(100)) > 0)) {
            return new ApiResponse<>(400, "Phần trăm giảm giá phải từ 0 đến 100", null);
        }
        if (request.getStockQuantity() != null && request.getStockQuantity() <= 0) {
            return new ApiResponse<>(400, "Số lượng tồn kho không hợp lệ", null);
        }
        if (request.getMaxPurchasePerUser() != null && request.getMaxPurchasePerUser() < 0) {
            return new ApiResponse<>(400, "Giới hạn mua mỗi user không hợp lệ", null);
        }

        // Lấy thông tin book hiện tại hoặc book mới (nếu thay đổi)
        Book targetBook = existing.getBook(); // Book hiện tại
        if (request.getBookId() != null) {
            Book newBook = bookRepository.findById(request.getBookId()).orElse(null);
            if (newBook == null) {
                return new ApiResponse<>(404, "Sách không tồn tại", null);
            }
            targetBook = newBook; // Dùng book mới nếu thay đổi
        }

        // ✅ VALIDATION MỚI: Kiểm tra số lượng flash sale không được lớn hơn tồn kho
        // sách
        Integer newStockQuantity = request.getStockQuantity() != null ? request.getStockQuantity()
                : existing.getStockQuantity();

        if (newStockQuantity > targetBook.getStockQuantity()) {
            return new ApiResponse<>(400,
                    String.format("Số lượng flash sale (%d) không được lớn hơn tồn kho sách '%s' (%d)",
                            newStockQuantity, targetBook.getBookName(), targetBook.getStockQuantity()),
                    null);
        }

        // Kiểm tra duplicate (nếu thay đổi flashSaleId hoặc bookId)
        Integer flashSaleId = request.getFlashSaleId() != null ? request.getFlashSaleId()
                : existing.getFlashSale().getId();
        Integer bookId = request.getBookId() != null ? request.getBookId() : existing.getBook().getId();
        boolean exists = flashSaleItemRepository.existsByFlashSaleIdAndBookIdAndIdNot(flashSaleId, bookId, id);
        if (exists) {
            return new ApiResponse<>(400, "Sách này đã có trong flash sale này!", null);
        }
        if (request.getFlashSaleId() != null) {
            FlashSale flashSale = flashSaleRepository.findById(request.getFlashSaleId()).orElse(null);
            if (flashSale == null) {
                return new ApiResponse<>(404, "Flash sale không tồn tại", null);
            }
            existing.setFlashSale(flashSale);
        }
        if (request.getBookId() != null) {
            Book book = bookRepository.findById(request.getBookId()).orElse(null);
            if (book == null) {
                return new ApiResponse<>(404, "Sách không tồn tại", null);
            }
            existing.setBook(book);
        }
        if (request.getDiscountPrice() != null) {
            existing.setDiscountPrice(request.getDiscountPrice());
        }
        if (request.getDiscountPercentage() != null) {
            existing.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getStockQuantity() != null) {
            existing.setStockQuantity(request.getStockQuantity());
        }
        if (request.getMaxPurchasePerUser() != null) {
            existing.setMaxPurchasePerUser(request.getMaxPurchasePerUser());
        }
        if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }
        existing.setUpdatedAt(System.currentTimeMillis());
        FlashSaleItem updatedItem = flashSaleItemRepository.save(existing);

        // 🔥 AUTO-SYNC: Đồng bộ cart nếu admin thay đổi bookId hoặc flashSaleId
        if (request.getBookId() != null || request.getFlashSaleId() != null) {
            try {
                int syncedCartCount = cartItemService.syncCartItemsWithNewFlashSale(flashSaleId);
                log.info(
                        "🔄 AUTO-SYNC CART: Updated flash sale item {} (flashSale: {}, book: {}), synced {} cart items",
                        id, flashSaleId, bookId, syncedCartCount);
            } catch (Exception e) {
                log.warn("⚠️ WARNING: Failed to sync cart items after updating flash sale item {}: {}",
                        id, e.getMessage());
            }
        }

        return new ApiResponse<>(200, "Cập nhật flash sale item thành công",
                flashSaleItemMapper.toResponse(updatedItem));
    }

    @Override
    public ApiResponse<FlashSaleItemResponse> toggleStatus(Integer id) {
        FlashSaleItem item = flashSaleItemRepository.findById(id).orElse(null);
        if (item == null) {
            return new ApiResponse<>(404, "Flash sale item không tồn tại", null);
        }
        item.setStatus(item.getStatus() != null && item.getStatus() == 1 ? (byte) 0 : (byte) 1);
        item.setUpdatedAt(System.currentTimeMillis());
        flashSaleItemRepository.save(item);
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", flashSaleItemMapper.toResponse(item));
    }

    @Override
    public ApiResponse<List<FlashSaleItemBookRequest>> findAllBooksInActiveFlashSale() {
        Long now = System.currentTimeMillis();
        Long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);
        List<Object[]> books = flashSaleItemRepository.findAllBookFlashSaleDTO(now, thirtyDaysAgo);

        List<FlashSaleItemBookRequest> dtoList = books.stream()
                .map(data -> BookFlashSaleMapper.mapToFlashSaleItemBookRequest(data, flashSaleItemRepository,
                        orderDetailRepository))
                .collect(Collectors.toList());

        return new ApiResponse<>(200, "Lấy được list sản phẩm FlashSale", dtoList);
    }

    @Override
    public ApiResponse<FlashSaleItemStatsResponse> getFlashSaleStats() {
        long totalBooksInFlashSale = flashSaleItemRepository.countTotalBooksInFlashSale();
        long totalBooksSoldInFlashSale = flashSaleItemRepository.countTotalBooksSoldInFlashSale();
        long totalFlashSaleStock = flashSaleItemRepository.countTotalFlashSaleStock();
        List<String> topBooks = flashSaleItemRepository
                .findTopSellingBookName(org.springframework.data.domain.PageRequest.of(0, 1));
        String topSellingBookName = topBooks.isEmpty() ? null : topBooks.get(0);

        FlashSaleItemStatsResponse stats = FlashSaleItemStatsResponse.builder()
                .totalBooksInFlashSale(totalBooksInFlashSale)
                .totalBooksSoldInFlashSale(totalBooksSoldInFlashSale)
                .topSellingBookName(topSellingBookName)
                .totalFlashSaleStock(totalFlashSaleStock)
                .build();

        return new ApiResponse<>(200, "Thành công", stats);
    }
}