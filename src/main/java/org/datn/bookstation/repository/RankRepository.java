package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankRepository extends JpaRepository<Rank, Integer> {
}
