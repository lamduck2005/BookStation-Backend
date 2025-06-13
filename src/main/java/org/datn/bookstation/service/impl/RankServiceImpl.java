package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Rank;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.repository.RankRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.RankService;
import org.springframework.stereotype.Service;
import org.datn.bookstation.dto.request.RankRequest;
import org.datn.bookstation.mapper.RankMapper;
import org.datn.bookstation.dto.response.ApiResponse;

import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
public class RankServiceImpl implements RankService {
    private final RankRepository rankRepository;
    private final RankMapper rankMapper;
    private final UserRepository userRepository;

    @Override
    public List<Rank> getAll() {
        return rankRepository.findAll();
    }

    @Override
    public Rank getById(Integer id) {
        return rankRepository.findById(id).orElse(null);
    }

    @Override
    public ApiResponse<Rank> add(RankRequest rankRequest) {
        if (rankRequest.getEmail() == null || rankRequest.getEmail().isEmpty()) {
            return new ApiResponse<>(404, "Email is required", null);
        }
        User user = userRepository.findByEmail(rankRequest.getEmail()).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "User with email does not exist", null);
        }
        Rank rank = rankMapper.toRank(rankRequest);
        rank.setUser(user);
        rank.setCreatedAt(java.time.Instant.now());
        Rank saved = rankRepository.save(rank);
        return new ApiResponse<>(201, "Created", saved);
    }

    @Override
    public Rank update(Rank rank, Integer id) {
        Rank existing = rankRepository.findById(id).orElseThrow(() -> new RuntimeException("Rank not found"));
        rank.setId(id);
        rank.setCreatedAt(existing.getCreatedAt());
        rank.setUpdatedAt(Instant.now());
        return rankRepository.save(rank);
    }

    @Override
    public void delete(Integer id) {
        rankRepository.deleteById(id);
    }
}
