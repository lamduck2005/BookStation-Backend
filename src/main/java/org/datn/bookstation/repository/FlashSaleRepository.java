package org.datn.bookstation.repository;

import org.datn.bookstation.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer>, JpaSpecificationExecutor<FlashSale> {
    boolean existsByName(String name);
}
