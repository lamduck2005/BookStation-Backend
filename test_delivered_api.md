# Test API chuyển trạng thái DELIVERED

## Vấn đề đã sửa
Frontend gọi API `/api/orders/{orderId}/status-transition` để chuyển từ SHIPPED → DELIVERED nhưng sold count không được cộng.

## Nguyên nhân
Trong `OrderStatusTransitionServiceImpl.handleStockImpact()`:
- ✅ Có logic cộng sold count cho FlashSaleItem
- ❌ THIẾU logic cộng sold count cho Book thông thường

## Đã sửa
```java
case DELIVERED:
    // ✅ CỘNG SOLD COUNT KHI GIAO THÀNH CÔNG
    if (detail.getFlashSaleItem() != null) {
        // Flash sale item
        FlashSaleItem flashSaleItem = detail.getFlashSaleItem();
        flashSaleItem.setSoldCount(flashSaleItem.getSoldCount() + quantity);
        flashSaleItemRepository.save(flashSaleItem);
    } else {
        // ✅ SỬA LỖI: Cộng sold count cho book thông thường
        book.setSoldCount(book.getSoldCount() + quantity);
        bookRepository.save(book);
    }
```

## Test Case
```bash
# API Call từ Frontend
POST /api/orders/80/status-transition
{
    "currentStatus": "SHIPPED",
    "newStatus": "DELIVERED", 
    "notes": "Thực hiện bởi admin ID: 1",
    "orderId": 80,
    "performedBy": 1,
    "reason": "Chuyển trạng thái từ Đang giao hàng thành Đã giao hàng",
    "staffId": 1
}
```

## Kết quả mong đợi
- Trạng thái order: SHIPPED → DELIVERED
- Book soldCount: tăng theo quantity của order detail
- Flash sale item soldCount: tăng theo quantity (nếu có)

## Kiểm tra
1. Gọi API status-transition
2. Kiểm tra book API: soldCount phải tăng
3. Kiểm tra log: thấy "SOLD_COUNT_INCREASED"
