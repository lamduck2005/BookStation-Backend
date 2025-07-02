package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.service.TrendingCacheService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 🔥 REAL-TIME TRENDING CACHE SERVICE IMPLEMENTATION
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrendingCacheServiceImpl implements TrendingCacheService {
    
    private final CacheManager cacheManager;
    
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
    @Async
    public void invalidateCacheOnNewOrder(Integer bookId, Integer quantity) {
        try {
            log.info("🔥 REAL-TIME: New order detected - BookId: {}, Quantity: {}", bookId, quantity);
            
            // Increment counter
            int totalOrders = orderInvalidations.incrementAndGet();
            
            // 🚀 SMART INVALIDATION: Chỉ invalidate khi có đủ đơn hàng quan trọng
            if (quantity >= MIN_ORDERS_FOR_INVALIDATION || totalOrders % 10 == 0) {
                invalidateAllTrendingCache();
                log.info("🔥 Cache invalidated due to significant order activity. Total orders: {}", totalOrders);
            } else {
                log.debug("Order not significant enough for cache invalidation. Quantity: {}, Total: {}", 
                         quantity, totalOrders);
            }
            
            updateLastInvalidationTime();
            
        } catch (Exception e) {
            log.error("Error invalidating cache on new order", e);
        }
    }
    
    @Override
    @Async
    public void invalidateCacheOnNewReview(Integer bookId, Double rating) {
        try {
            log.info("🔥 REAL-TIME: New review detected - BookId: {}, Rating: {}", bookId, rating);
            
            int totalReviews = reviewInvalidations.incrementAndGet();
            
            // 🚀 SMART INVALIDATION: Invalidate nếu là review quan trọng
            if (rating >= 4.0 || rating <= 2.0 || totalReviews % MIN_REVIEWS_FOR_INVALIDATION == 0) {
                invalidateAllTrendingCache();
                log.info("🔥 Cache invalidated due to important review. Rating: {}, Total reviews: {}", 
                        rating, totalReviews);
            }
            
            updateLastInvalidationTime();
            
        } catch (Exception e) {
            log.error("Error invalidating cache on new review", e);
        }
    }
    
    @Override
    @Async
    public void invalidateCacheOnFlashSaleChange(Integer bookId, boolean isStarted) {
        try {
            log.info("🔥 REAL-TIME: Flash sale change detected - BookId: {}, Started: {}", bookId, isStarted);
            
            // Flash sale luôn invalidate ngay lập tức vì impact lớn
            invalidateAllTrendingCache();
            flashSaleInvalidations.incrementAndGet();
            
            log.info("🔥 Cache invalidated due to flash sale change");
            updateLastInvalidationTime();
            
        } catch (Exception e) {
            log.error("Error invalidating cache on flash sale change", e);
        }
    }
    
    @Override
    @CacheEvict(value = {"trending-books", "trending-books-by-category"}, allEntries = true)
    public void invalidateAllTrendingCache() {
        log.info("🔥 All trending cache invalidated at {}", LocalDateTime.now());
        
        // Clear Spring cache safely
        try {
            var trendingBooksCache = cacheManager.getCache("trending-books");
            if (trendingBooksCache != null) {
                trendingBooksCache.clear();
            }
            
            var trendingBooksByCategoryCache = cacheManager.getCache("trending-books-by-category");
            if (trendingBooksByCategoryCache != null) {
                trendingBooksByCategoryCache.clear();
            }
        } catch (Exception e) {
            log.warn("Error clearing cache manually: {}", e.getMessage());
        }
    }
    
    @Override
    @CacheEvict(value = "trending-books-by-category", key = "#categoryId")
    public void invalidateCacheByCategory(Integer categoryId) {
        log.info("🔥 Trending cache invalidated for category: {}", categoryId);
    }
    
    @Override
    public void checkAndRebuildCacheIfNeeded() {
        long currentTime = System.currentTimeMillis();
        long lastInvalidation = lastInvalidationTime.get();
        
        // Nếu đã quá 6 giờ chưa có invalidation nào, trigger rebuild
        if (currentTime - lastInvalidation > CACHE_REBUILD_INTERVAL_MS) {
            log.info("🔥 Triggering scheduled cache rebuild - Last invalidation: {} hours ago", 
                    (currentTime - lastInvalidation) / (60 * 60 * 1000));
            invalidateAllTrendingCache();
            updateLastInvalidationTime();
        }
    }
    
    @Override
    public String getCacheStatistics() {
        return String.format(
            "📊 Trending Cache Statistics:\n" +
            "- Order invalidations: %d\n" +
            "- Review invalidations: %d\n" +
            "- Flash sale invalidations: %d\n" +
            "- Last invalidation: %s\n" +
            "- Cache rebuild interval: %d hours",
            orderInvalidations.get(),
            reviewInvalidations.get(), 
            flashSaleInvalidations.get(),
            lastInvalidationTime.get() > 0 ? 
                LocalDateTime.now().minusSeconds((System.currentTimeMillis() - lastInvalidationTime.get()) / 1000) : 
                "Never",
            CACHE_REBUILD_INTERVAL_MS / (60 * 60 * 1000)
        );
    }
    
    private void updateLastInvalidationTime() {
        lastInvalidationTime.set(System.currentTimeMillis());
    }
}
