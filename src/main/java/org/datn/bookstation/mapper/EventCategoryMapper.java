package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.EventCategoryRequest;
import org.datn.bookstation.entity.EventCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventCategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "events", ignore = true)
    EventCategory toEventCategory(EventCategoryRequest request);
}
