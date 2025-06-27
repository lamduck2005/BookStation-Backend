package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.EventParticipantRequest;
import org.datn.bookstation.entity.EventParticipant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventParticipantMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "joinedAt", ignore = true)
    @Mapping(target = "giftReceived", ignore = true)
    @Mapping(target = "giftClaimedAt", ignore = true)
    EventParticipant toEventParticipant(EventParticipantRequest request);
}
