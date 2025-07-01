# Order System Quick Start Guide

## 🚀 Tóm Tắt Hệ Thống

BookStation Order System là hệ thống quản lý đơn hàng e-commerce hiện đại với các tính năng:

- ✅ **Flash Sale** - Khuyến mãi giới hạn thời gian và số lượng
- ✅ **Voucher Stacking** - Kết hợp tối đa 2 voucher (1 thường + 1 freeship)  
- ✅ **Mixed Orders** - Sản phẩm thường + flash sale trong cùng 1 đơn
- ✅ **Auto Calculation** - Tự động tính subtotal, discount, shipping, total
- ✅ **Smart Validation** - Kiểm tra đầy đủ các rule nghiệp vụ

## 📚 Tài Liệu Đầy Đủ

| File | Mục Đích | Đối Tượng |
|------|----------|-----------|
| `ORDER_API_DOCUMENTATION.md` | API reference, database schema, endpoints | Backend Dev, Frontend Dev |
| `ORDER_BUSINESS_LOGIC_GUIDE.md` | Business logic, training, examples | Developer mới, QA, BA |
| `ORDER_SYSTEM_STATUS.md` | Current status, completed features | Team Lead, PM |

## 🏗️ Kiến Trúc Chính

```
┌───────────┐    ┌──────────────┐    ┌─────────────┐
│ OrderController │ → │ OrderServiceImpl │ → │ OrderRepository │
└────────────┘    └──────────────┘    └─────────────┘
       │                  │
       ▼                  ▼
┌─────────────┐    ┌─────────────────┐
│ Validation   │    │ VoucherCalculation │
│ - Flash Sale │    │ Service            │
│ - Stock      │    │ - Discount logic   │
│ - User       │    │ - Stacking rules   │
└─────────────┘    └─────────────────┘
```

## 🎯 Business Rules Quan Trọng

### Flash Sale
- ⏰ **Time-based**: Chỉ hoạt động trong khoảng thời gian nhất định
- 📦 **Stock-limited**: Giới hạn số lượng có sẵn
- 👤 **User-limited**: Giới hạn số lượng mua per user (nếu có)
- 💰 **Price**: Sử dụng `FlashSaleItem.discountPrice` thay vì `Book.price`

### Voucher Stacking
- 🔢 **Max 2 vouchers** per order
- 🏷️ **Max 1 regular** voucher (PERCENTAGE/FIXED_AMOUNT)
- 🚚 **Max 1 freeship** voucher (FREE_SHIPPING)
- ✅ **Valid combination**: 1 regular + 1 freeship

### Price Calculation Order
```
1. Calculate Subtotal (per product: flash sale price OR regular price)
2. Apply Product Discount (from regular vouchers)
3. Add Shipping Fee
4. Apply Shipping Discount (from freeship vouchers) 
5. Final Total = Subtotal + Shipping - Product Discount - Shipping Discount
```

## 🚨 Common Errors & Solutions

| Error | Cause | Solution |
|-------|--------|----------|
| Flash sale hết hạn | Current time outside flash sale period | Check time validity |
| Hết stock flash sale | Requested > available stock | Reduce quantity or wait restock |
| Quá nhiều voucher | > 2 vouchers applied | Use max 2 vouchers |
| Sai loại voucher | 2 regular hoặc 2 freeship | Use 1 regular + 1 freeship |
| Order calculation sai | Logic error in calculation | Check calculation order |

## 🛠️ Quick Development Tips

### 1. Tạo Order Mới
```java
// Auto-detect flash sale và calculate prices
OrderRequest request = OrderRequest.builder()
    .userId(1L)
    .addressId(1L)  
    .orderDetails(orderDetails) // System tự detect flash sale
    .voucherIds(Arrays.asList(101L, 201L)) // Max 2 vouchers
    .notes("Đơn hàng test")
    .build();

OrderResponse response = orderService.createOrder(request);
```

### 2. Validation Flow
- OrderServiceImpl tự động validate ALL rules
- Throw exceptions với message rõ ràng
- Frontend chỉ cần handle exceptions

### 3. Testing Scenarios
- ✅ Regular order (no flash sale, no voucher)
- ✅ Flash sale only
- ✅ Voucher only  
- ✅ Flash sale + voucher stacking
- ✅ Mixed order (regular + flash sale products)
- ❌ Error cases (expired, insufficient stock, etc.)

## 🎓 Onboarding Checklist for New Developers

- [ ] Đọc `ORDER_API_DOCUMENTATION.md` - Hiểu schema và API
- [ ] Đọc `ORDER_BUSINESS_LOGIC_GUIDE.md` - Hiểu nghiệp vụ
- [ ] Chạy application: `./mvnw spring-boot:run`
- [ ] Test API với Postman/curl
- [ ] Chạy unit tests: `./mvnw test`
- [ ] Thử các scenarios trong business guide
- [ ] Debug qua IDE để hiểu flow code

## 📋 Key Files to Know

### Entity Layer
- `Order.java` - Main order entity
- `OrderDetail.java` - Order line items  
- `Voucher.java` - Voucher master data
- `OrderVoucher.java` - Applied vouchers tracking
- `FlashSaleItem.java` - Flash sale products

### Service Layer  
- `OrderServiceImpl.java` - Main business logic
- `VoucherCalculationServiceImpl.java` - Voucher calculation logic

### DTO Layer
- `OrderRequest.java` - Create order input
- `OrderResponse.java` - Order output with calculated fields

## 🔍 Debugging Commands

```bash
# Start application
./mvnw spring-boot:run

# Test order creation
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"addressId":1,"orderDetails":[...]}'

# Check order by ID  
curl http://localhost:8080/api/orders/1

# Check voucher validation
curl -X POST http://localhost:8080/api/vouchers/validate \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"voucherIds":[101,201],"orderValue":200000}'
```

## 🎉 Success Criteria

Một developer mới được coi là hiểu hệ thống khi có thể:

1. ✅ Giải thích được flow tính toán giá 1 đơn hàng
2. ✅ Tạo được test case cho flash sale + voucher stacking  
3. ✅ Debug được các lỗi thường gặp
4. ✅ Implement được 1 feature mới theo pattern hiện tại
5. ✅ Review code của người khác một cách có ý nghĩa

---

**Next Steps**: Sau khi nắm được basics, hãy đọc sâu hơn về performance optimization, security best practices, và advanced scenarios trong business logic guide.
