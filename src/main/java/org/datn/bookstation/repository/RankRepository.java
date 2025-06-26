package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;

public interface RankRepository extends JpaRepository<Rank, Integer>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Rank> {
    boolean existsByRankName(String rankName);
}
