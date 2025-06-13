package org.datn.bookstation.service;

import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.dto.response.ApiResponse;

import java.util.List;

public interface RankService {
    List<Rank> getAll();
    Rank getById(Integer id);
    ApiResponse<Rank> add(RankRequest rankRequest);
    Rank update(Rank rank, Integer id);
    void delete(Integer id);
}
