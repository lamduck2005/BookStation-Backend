package org.datn.bookstation.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.datn.bookstation.dto.request.FlashSaleItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleItemResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSale;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.mapper.FlashSaleItemMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.FlashSaleRepository;
import org.datn.bookstation.service.FlashSaleItemService;
import org.datn.bookstation.specification.FlashSaleItemSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleItemServiceImpl implements FlashSaleItemService {

    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private FlashSaleItemMapper flashSaleItemMapper;

    @Override
    public ApiResponse<PaginationResponse<FlashSaleItemResponse>> getAllWithFilter(int page, int size, Integer flashSaleId, Integer bookId, Byte status,
            BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minPercent, BigDecimal maxPercent, Integer minQuantity, Integer maxQuantity) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<FlashSaleItem> spec = FlashSaleItemSpecification.filterBy(flashSaleId, bookId, status, minPrice, maxPrice, minPercent, maxPercent, minQuantity, maxQuantity);
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
        FlashSale flashSale = flashSaleRepository.findById(request.getFlashSaleId()).orElse(null);
        if (flashSale == null) {
            return new ApiResponse<>(404, "Flash sale không tồn tại", null);
        }
        Book book = bookRepository.findById(request.getBookId()).orElse(null);
        if (book == null) {
            return new ApiResponse<>(404, "Sách không tồn tại", null);
        }
        boolean exists = flashSaleItemRepository.existsByFlashSaleIdAndBookId(request.getFlashSaleId(), request.getBookId());
        if (exists) {
            return new ApiResponse<>(400, "Sách này đã có trong flash sale này!", null);
        }
        FlashSaleItem item = flashSaleItemMapper.toEntity(request);
        item.setFlashSale(flashSale);
        item.setBook(book);
        flashSaleItemRepository.save(item);
        return new ApiResponse<>(201, "Tạo flash sale item thành công", flashSaleItemMapper.toResponse(item));
    }

    @Override
    public ApiResponse<FlashSaleItemResponse> update(Integer id, FlashSaleItemRequest request) {
        FlashSaleItem existing = flashSaleItemRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Flash sale item không tồn tại", null);
        }
        Integer flashSaleId = request.getFlashSaleId() != null ? request.getFlashSaleId() : existing.getFlashSale().getId();
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
        flashSaleItemRepository.save(existing);
        return new ApiResponse<>(200, "Cập nhật flash sale item thành công", flashSaleItemMapper.toResponse(existing));
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
} 