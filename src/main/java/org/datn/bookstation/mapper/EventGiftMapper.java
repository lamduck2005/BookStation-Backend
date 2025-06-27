package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.EventGiftRequest;
import org.datn.bookstation.entity.EventGift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventGiftMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "voucher", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "remainingQuantity", ignore = true)
    EventGift toEventGift(EventGiftRequest request);
}
