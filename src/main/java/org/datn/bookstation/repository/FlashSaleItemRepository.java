package org.datn.bookstation.repository;

import org.datn.bookstation.entity.FlashSaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Integer>, JpaSpecificationExecutor<FlashSaleItem> {
    
    @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId")
    List<FlashSaleItem> findByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);
    
    @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.book.id = :bookId")
    List<FlashSaleItem> findByBookId(@Param("bookId") Integer bookId);
    
    @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId AND fsi.status = 1")
    List<FlashSaleItem> findActiveByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);

    boolean existsByFlashSaleIdAndBookId(Integer flashSaleId, Integer bookId);
    boolean existsByFlashSaleIdAndBookIdAndIdNot(Integer flashSaleId, Integer bookId, Integer id);
}
