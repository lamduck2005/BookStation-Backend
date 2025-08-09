package org.datn.bookstation.repository;

import org.datn.bookstation.entity.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer>, JpaSpecificationExecutor<FlashSale> {
    boolean existsByName(String name);

    @Query("""
                SELECT fl FROM FlashSale fl
                WHERE fl.startTime < :endTime AND fl.endTime > :startTime
            """)
    List<FlashSale> findOverlappingFlashSales(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    List<FlashSale> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long dateMillis, Long dateMillis1);
}
