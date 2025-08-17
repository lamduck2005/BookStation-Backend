package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.service.TrendingCacheService;
// import org.springframework.cache.CacheManager; // DISABLED - Cache manager không cần thiết nữa
// import org.springframework.cache.annotation.CacheEvict; // DISABLED
// import org.springframework.scheduling.annotation.Async; // DISABLED
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🔥 REAL-TIME TRENDING CACHE SERVICE IMPLEMENTATION
 * DISABLED - Cache đã được tắt theo yêu cầu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrendingCacheServiceImpl implements TrendingCacheService {
    
    // private final CacheManager cacheManager; // DISABLED - Cache manager không cần thiết nữa
    
    // Statistics
    private final AtomicInteger orderInvalidations = new AtomicInteger(0);
    private final AtomicInteger reviewInvalidations = new AtomicInteger(0);
    private final AtomicInteger flashSaleInvalidations = new AtomicInteger(0);
    private final AtomicLong lastInvalidationTime = new AtomicLong();
    
    // Thresholds cho smart invalidation
    private static final int MIN_ORDERS_FOR_INVALIDATION = 5; // Tối thiểu 5 đơn hàng mới invalidate
    private static final int MIN_REVIEWS_FOR_INVALIDATION = 3; // Tối thiểu 3 review mới invalidate
    private static final long CACHE_REBUILD_INTERVAL_MS = 6 * 60 * 60 * 1000L; // 6 giờ
    
    @Override
    // @Async // DISABLED
    public void invalidateCacheOnNewOrder(Integer bookId, Integer quantity) {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache invalidation disabled - Order: BookId={}, Quantity={}", bookId, quantity);
    }
    
    @Override
    // @Async // DISABLED
    public void invalidateCacheOnNewReview(Integer bookId, Double rating) {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache invalidation disabled - Review: BookId={}, Rating={}", bookId, rating);
    }
    
    @Override
    // @Async // DISABLED
    public void invalidateCacheOnFlashSaleChange(Integer bookId, boolean isStarted) {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache invalidation disabled - FlashSale: BookId={}, Started={}", bookId, isStarted);
    }
    
    @Override
    // @CacheEvict(value = {"trending-books", "trending-books-by-category"}, allEntries = true) // DISABLED
    public void invalidateAllTrendingCache() {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache invalidation disabled - All trending cache would be cleared");
    }
    
    @Override
    // @CacheEvict(value = "trending-books-by-category", key = "#categoryId") // DISABLED
    public void invalidateCacheByCategory(Integer categoryId) {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache invalidation disabled - Category: {}", categoryId);
    }
    
    @Override
    public void checkAndRebuildCacheIfNeeded() {
        // Cache đã được tắt - không làm gì cả
        log.debug("Cache rebuild disabled");
    }
    
    @Override
    public String getCacheStatistics() {
        return "📊 Cache Statistics: DISABLED - Cache đã được tắt theo yêu cầu";
    }
    
    private void updateLastInvalidationTime() {
        lastInvalidationTime.set(System.currentTimeMillis());
    }
}
