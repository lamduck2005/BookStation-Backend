package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.EventRequest;
import org.datn.bookstation.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventCategory", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "eventGifts", ignore = true)
    @Mapping(target = "eventParticipants", ignore = true)
    @Mapping(target = "currentParticipants", ignore = true)
    Event toEvent(EventRequest request);
}
