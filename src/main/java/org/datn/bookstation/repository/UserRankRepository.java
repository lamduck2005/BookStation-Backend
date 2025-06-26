package org.datn.bookstation.repository;

import org.datn.bookstation.entity.UserRank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserRankRepository extends JpaRepository<UserRank, Integer>, JpaSpecificationExecutor<UserRank> {
    List<UserRank> findByRankId(Integer rankId);

    Page<UserRank> findByRankIdAndUserEmailContainingIgnoreCaseAndUserFullNameContainingIgnoreCase(
        Integer rankId, String email, String fullName, Pageable pageable);
}
