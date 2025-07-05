# Flash Sale Batch Expiration Handling

## 🎯 Problem: Multiple Flash Sales Expiring Simultaneously

### Scenario
```
Flash Sale A: endTime = "2025-07-04 15:00:00"
Flash Sale B: endTime = "2025-07-04 15:00:30" 
Flash Sale C: endTime = "2025-07-04 15:01:15"
Flash Sale D: endTime = "2025-07-04 15:01:45"

=> Có thể có hàng chục/trăm flash sales expire cùng lúc
=> Cần xử lý hiệu quả mà không tạo quá nhiều tasks
```

## ✅ Solution: Batch Processing với Time Grouping

### 1. Time Normalization
```java
// Round thời gian về nearest minute
long normalizedTime = (endTime / 60000) * 60000;

Example:
- 15:00:00 -> 15:00:00 (group 1)
- 15:00:30 -> 15:00:00 (group 1) 
- 15:01:15 -> 15:01:00 (group 2)
- 15:01:45 -> 15:01:00 (group 2)

=> Chỉ cần 2 tasks thay vì 4 tasks
```

### 2. Data Structure
```java
// Group flash sales theo thời điểm
Map<Long, Set<Integer>> flashSalesByTime = {
    1720083600000L: [1, 2],    // 15:00:00 -> flash sales 1,2
    1720083660000L: [3, 4]     // 15:01:00 -> flash sales 3,4
}

// Track scheduled tasks theo thời điểm
Map<Long, ScheduledFuture<?>> scheduledTasksByTime = {
    1720083600000L: ScheduledTask1,
    1720083660000L: ScheduledTask2
}
```

### 3. Scheduling Flow
```java
public void scheduleFlashSaleExpiration(Integer flashSaleId, Long endTime) {
    long normalizedTime = (endTime / 60000) * 60000;
    
    // Add vào group
    flashSalesByTime.computeIfAbsent(normalizedTime, k -> new HashSet<>())
                   .add(flashSaleId);
    
    // Nếu chưa có task cho time này, tạo mới
    if (!scheduledTasksByTime.containsKey(normalizedTime)) {
        ScheduledFuture<?> task = taskScheduler.schedule(
            () -> handleBatchFlashSaleExpiration(normalizedTime),
            Instant.ofEpochMilli(normalizedTime)
        );
        scheduledTasksByTime.put(normalizedTime, task);
    }
}
```

### 4. Batch Execution
```java
private void handleBatchFlashSaleExpiration(Long normalizedTime) {
    Set<Integer> expiredFlashSales = flashSalesByTime.get(normalizedTime);
    
    // Batch update tất cả cart items 1 lần
    List<Integer> flashSaleIds = expiredFlashSales.stream().toList();
    int totalUpdated = cartItemService.handleExpiredFlashSalesInCartBatch(flashSaleIds);
    
    log.info("Updated {} cart items for {} flash sales", totalUpdated, flashSaleIds.size());
}
```

## 🚀 Performance Benefits

### Memory & CPU
```
❌ Individual Tasks:
- 1000 flash sales = 1000 ScheduledTasks
- 1000 memory objects
- 1000 separate database calls

✅ Batch Approach:
- 1000 flash sales ≈ 16-17 ScheduledTasks (1 per minute)
- 16-17 memory objects  
- 16-17 batch database calls
- 98% reduction in memory usage
```

### Database Performance
```java
// ❌ Individual approach
for (Integer flashSaleId : expiredFlashSales) {
    // N database calls
    cartItemRepository.updateByFlashSaleId(flashSaleId);
}

// ✅ Batch approach  
// 1 database call
cartItemRepository.updateByFlashSaleIds(flashSaleIds);
```

### Example Batch Query
```sql
-- Single batch update thay vì N individual updates
UPDATE cart_item 
SET flash_sale_item_id = NULL, 
    updated_at = ?, 
    updated_by = ?
WHERE flash_sale_item_id IN (
    SELECT id FROM flash_sale_item 
    WHERE flash_sale_id IN (1, 2, 3, 4, 5)
);
```

## 🔧 Implementation Details

### 1. CartItemService Batch Method
```java
@Override
public int handleExpiredFlashSalesInCartBatch(List<Integer> flashSaleIds) {
    if (flashSaleIds.isEmpty()) return 0;
    
    // Single query để update tất cả cart items
    int updatedCount = cartItemRepository.batchUpdateExpiredFlashSales(flashSaleIds);
    
    log.info("Batch updated {} cart items for flash sales: {}", updatedCount, flashSaleIds);
    return updatedCount;
}
```

### 2. Repository Batch Query
```java
@Modifying
@Query("UPDATE CartItem ci SET ci.flashSaleItem = NULL, ci.updatedAt = :now " +
       "WHERE ci.flashSaleItem.id IN (" +
       "  SELECT fsi.id FROM FlashSaleItem fsi " +
       "  WHERE fsi.flashSale.id IN :flashSaleIds" +
       ")")
int batchUpdateExpiredFlashSales(@Param("flashSaleIds") List<Integer> flashSaleIds, 
                                @Param("now") Long now);
```

### 3. Cancellation Handling
```java
public void cancelScheduledTask(Integer flashSaleId) {
    synchronized (this) {
        for (var entry : flashSalesByTime.entrySet()) {
            if (entry.getValue().remove(flashSaleId)) {
                // Nếu group trống, cancel task
                if (entry.getValue().isEmpty()) {
                    cancelAndRemoveTask(entry.getKey());
                }
                break;
            }
        }
    }
}
```

## 📊 Real-world Scenarios

### Black Friday Example
```
Scenario: 500 flash sales kết thúc từ 23:59:00 đến 23:59:59

❌ Without Batch:
- 500 ScheduledTasks
- 500 database calls at ~same time
- Potential database overload
- Memory overhead

✅ With Batch:
- 1 ScheduledTask (normalized to 23:59:00)
- 1 database call
- Smooth performance
- Minimal memory usage
```

### Peak Hour Example
```
Scenario: 50 flash sales mỗi phút trong peak hour

❌ Without Batch:
- 50 tasks/minute × 60 minutes = 3000 tasks
- High scheduler overhead

✅ With Batch:
- 1 task/minute × 60 minutes = 60 tasks  
- 98% reduction in scheduler load
```

## ⚙️ Configuration Options

### Time Granularity
```java
// Current: Round to minute
long normalizedTime = (endTime / 60000) * 60000;

// Alternative: Round to 30 seconds (more precise)
long normalizedTime = (endTime / 30000) * 30000;

// Alternative: Round to 5 minutes (more batching)
long normalizedTime = (endTime / 300000) * 300000;
```

### Batch Size Limits
```java
// Optional: Split very large batches
public void handleBatchFlashSaleExpiration(Long normalizedTime) {
    Set<Integer> expiredFlashSales = flashSalesByTime.get(normalizedTime);
    
    // Process in chunks nếu quá lớn
    final int BATCH_SIZE = 100;
    List<Integer> flashSaleIds = new ArrayList<>(expiredFlashSales);
    
    for (int i = 0; i < flashSaleIds.size(); i += BATCH_SIZE) {
        List<Integer> batch = flashSaleIds.subList(i, 
            Math.min(i + BATCH_SIZE, flashSaleIds.size()));
        cartItemService.handleExpiredFlashSalesInCartBatch(batch);
    }
}
```

---

**Kết luận**: Batch processing approach giải quyết hiệu quả vấn đề nhiều flash sales cùng expire, với performance improvement đáng kể và resource usage tối ưu.
