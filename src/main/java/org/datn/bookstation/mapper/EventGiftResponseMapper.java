package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.EventGiftResponse;
import org.datn.bookstation.entity.EventGift;
import org.springframework.stereotype.Component;

@Component
public class EventGiftResponseMapper {
    public EventGiftResponse toResponse(EventGift gift) {
        if (gift == null) return null;
        
        EventGiftResponse response = new EventGiftResponse();
        response.setId(gift.getId());
        response.setEventId(gift.getEvent() != null ? gift.getEvent().getId() : null);
        response.setEventName(gift.getEvent() != null ? gift.getEvent().getEventName() : null);
        response.setGiftName(gift.getGiftName());
        response.setDescription(gift.getDescription());
        response.setGiftValue(gift.getGiftValue());
        response.setQuantity(gift.getQuantity());
        response.setRemainingQuantity(gift.getRemainingQuantity());
        response.setImageUrl(gift.getImageUrl());
        response.setGiftType(gift.getGiftType());
        response.setBookId(gift.getBook() != null ? gift.getBook().getId() : null);
        response.setBookTitle(gift.getBook() != null ? gift.getBook().getBookName() : null);
        response.setVoucherId(gift.getVoucher() != null ? gift.getVoucher().getId() : null);
        response.setVoucherCode(gift.getVoucher() != null ? gift.getVoucher().getCode() : null);
        response.setPointValue(gift.getPointValue());
        response.setIsActive(gift.getIsActive());
        response.setCreatedAt(gift.getCreatedAt());
        
        return response;
    }
}
