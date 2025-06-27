package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventGiftRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventGiftResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.EventGift;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.mapper.EventGiftMapper;
import org.datn.bookstation.mapper.EventGiftResponseMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.EventGiftRepository;
import org.datn.bookstation.repository.EventRepository;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.EventGiftService;
import org.datn.bookstation.specification.EventGiftSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventGiftServiceImpl implements EventGiftService {
    private final EventGiftRepository eventGiftRepository;
    private final EventRepository eventRepository;
    private final BookRepository bookRepository;
    private final VoucherRepository voucherRepository;
    private final EventGiftMapper eventGiftMapper;
    private final EventGiftResponseMapper eventGiftResponseMapper;

    @Override
    public PaginationResponse<EventGiftResponse> getAllWithPagination(int page, int size, String giftName, 
            Integer eventId, String giftType, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<EventGift> specification = EventGiftSpecification.filterBy(giftName, eventId, giftType, isActive);
        Page<EventGift> giftPage = eventGiftRepository.findAll(specification, pageable);
        
        List<EventGiftResponse> giftResponses = giftPage.getContent().stream()
                .map(eventGiftResponseMapper::toResponse)
                .collect(Collectors.toList());
                
        return PaginationResponse.<EventGiftResponse>builder()
                .content(giftResponses)
                .pageNumber(giftPage.getNumber())
                .pageSize(giftPage.getSize())
                .totalElements(giftPage.getTotalElements())
                .totalPages(giftPage.getTotalPages())
                .build();
    }

    @Override
    public List<EventGift> getAll() {
        return eventGiftRepository.findAll();
    }

    @Override
    public List<EventGift> getByEventId(Integer eventId) {
        return eventGiftRepository.findByEventId(eventId);
    }

    @Override
    public List<EventGift> getActiveByEventId(Integer eventId) {
        return eventGiftRepository.findActiveByEventId(eventId);
    }

    @Override
    public EventGift getById(Integer id) {
        return eventGiftRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<EventGift> add(EventGiftRequest request) {
        // Validate event exists
        Event event = eventRepository.findById(request.getEventId()).orElse(null);
        if (event == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        EventGift gift = eventGiftMapper.toEventGift(request);
        gift.setEvent(event);
        
        // Set book if provided
        if (request.getBookId() != null) {
            Book book = bookRepository.findById(request.getBookId()).orElse(null);
            if (book == null) {
                return new ApiResponse<>(404, "Không tìm thấy sách", null);
            }
            gift.setBook(book);
        }
        
        // Set voucher if provided
        if (request.getVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(request.getVoucherId()).orElse(null);
            if (voucher == null) {
                return new ApiResponse<>(404, "Không tìm thấy voucher", null);
            }
            gift.setVoucher(voucher);
        }
        
        gift.setCreatedAt(Instant.now().toEpochMilli());
        EventGift saved = eventGiftRepository.save(gift);
        return new ApiResponse<>(201, "Tạo mới thành công", saved);
    }

    @Override
    public ApiResponse<EventGift> update(EventGiftRequest request, Integer id) {
        EventGift existing = eventGiftRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Không tìm thấy quà tặng", null);
        }
        
        // Validate event exists
        Event event = eventRepository.findById(request.getEventId()).orElse(null);
        if (event == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        existing.setEvent(event);
        existing.setGiftName(request.getGiftName());
        existing.setDescription(request.getDescription());
        existing.setGiftValue(request.getGiftValue());
        existing.setQuantity(request.getQuantity());
        existing.setImageUrl(request.getImageUrl());
        existing.setGiftType(request.getGiftType());
        existing.setPointValue(request.getPointValue());
        existing.setIsActive(request.getIsActive());
        
        // Set book if provided
        if (request.getBookId() != null) {
            Book book = bookRepository.findById(request.getBookId()).orElse(null);
            if (book == null) {
                return new ApiResponse<>(404, "Không tìm thấy sách", null);
            }
            existing.setBook(book);
        } else {
            existing.setBook(null);
        }
        
        // Set voucher if provided
        if (request.getVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(request.getVoucherId()).orElse(null);
            if (voucher == null) {
                return new ApiResponse<>(404, "Không tìm thấy voucher", null);
            }
            existing.setVoucher(voucher);
        } else {
            existing.setVoucher(null);
        }
        
        EventGift saved = eventGiftRepository.save(existing);
        return new ApiResponse<>(200, "Cập nhật thành công", saved);
    }

    @Override
    public void delete(Integer id) {
        eventGiftRepository.deleteById(id);
    }

    @Override
    public ApiResponse<EventGift> toggleStatus(Integer id) {
        EventGift gift = eventGiftRepository.findById(id).orElse(null);
        if (gift == null) {
            return new ApiResponse<>(404, "Không tìm thấy quà tặng", null);
        }
        
        gift.setIsActive(gift.getIsActive() == null || !gift.getIsActive());
        eventGiftRepository.save(gift);
        
        return new ApiResponse<>(200, "Cập nhật trạng thái thành công", gift);
    }
}
