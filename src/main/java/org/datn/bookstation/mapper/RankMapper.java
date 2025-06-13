package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.entity.Rank;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RankMapper {
    Rank toRank(RankRequest request);
}
