package org.datn.bookstation.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.service.TrendingCacheService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 🔥 TRENDING CACHE SCHEDULER
 * Tự động rebuild cache và maintenance
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrendingCacheScheduler {
    
    private final TrendingCacheService trendingCacheService;
    
    /**
     * Rebuild cache mỗi 6 giờ
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 giờ
    public void rebuildTrendingCache() {
        try {
            log.info("🔥 SCHEDULED: Starting trending cache rebuild...");
            trendingCacheService.checkAndRebuildCacheIfNeeded();
            log.info("🔥 SCHEDULED: Trending cache rebuild completed");
        } catch (Exception e) {
            log.error("Error during scheduled cache rebuild", e);
        }
    }
    
    /**
     * Log cache statistics mỗi giờ
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 giờ
    public void logCacheStatistics() {
        try {
            String stats = trendingCacheService.getCacheStatistics();
            log.info("🔥 CACHE STATS:\n{}", stats);
        } catch (Exception e) {
            log.error("Error logging cache statistics", e);
        }
    }
}
