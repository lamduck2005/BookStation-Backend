
## Nghiệp vụ trạng thái (`status`) của FlashSaleItem

- **status = 1 (Active):**
  - Sản phẩm xuất hiện trong danh sách Flash Sale.
  - Người dùng có thể mua sản phẩm này trong thời gian diễn ra Flash Sale.

- **status = 0 (Inactive):**
  - Sản phẩm **không xuất hiện** trong danh sách Flash Sale.
  - Người dùng **không thể mua mới** sản phẩm này trong Flash Sale.
  - Nếu người dùng đã cho vào giỏ hàng nhưng chưa thanh toán, khi thanh toán sẽ bị từ chối với thông báo:  
    > "Sản phẩm này đã ngừng bán trong flash sale, vui lòng chọn sản phẩm khác."
  - **Các đơn hàng đã tạo thành công trước khi tắt status vẫn giữ nguyên hiệu lực** (không bị huỷ).

- **Chỉ được phép thay đổi trạng thái khi Flash Sale còn hiệu lực** (nếu muốn kiểm soát chặt chẽ).

---

**Tóm lại:**  
- Bật status = 1: Cho phép mua.
- Tắt status = 0: Không cho phép mua mới, không ảnh hưởng đơn đã mua trước đó.
