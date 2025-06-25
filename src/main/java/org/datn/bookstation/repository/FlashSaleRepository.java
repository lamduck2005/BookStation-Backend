package org.datn.bookstation.repository;

import org.datn.bookstation.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {
    boolean existsByName(String name);
}
