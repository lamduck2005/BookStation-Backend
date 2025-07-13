package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.CartItemRequest;
import org.datn.bookstation.dto.request.BatchCartItemRequest;
import org.datn.bookstation.dto.request.SmartCartItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.CartItem;

import java.util.List;

public interface CartItemService {
    
    /**
     * Lấy tất cả items trong giỏ hàng của user
     */
    List<CartItemResponse> getCartItemsByUserId(Integer userId);
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     * - Nếu đã tồn tại (cùng book + flashSale): tăng quantity
     * - Nếu chưa tồn tại: tạo mới
     */
    ApiResponse<CartItemResponse> addItemToCart(CartItemRequest request);
    
    /**
     * Cập nhật số lượng CartItem
     */
    ApiResponse<CartItemResponse> updateCartItem(Integer cartItemId, Integer quantity);
    
    /**
     * Xóa CartItem khỏi giỏ hàng
     */
    ApiResponse<String> removeCartItem(Integer cartItemId);
    
    /**
     * Thêm nhiều sản phẩm cùng lúc (batch)
     */
    ApiResponse<List<CartItemResponse>> addItemsToCartBatch(BatchCartItemRequest request);
    
    /**
     * Xóa tất cả CartItems của một cart
     */
    ApiResponse<String> clearCartItems(Integer cartId);
    
    /**
     * Kiểm tra và cập nhật trạng thái các CartItems
     * - Flash sale hết hạn -> chuyển về regular
     * - Sách hết hàng -> đánh dấu warning
     */
    ApiResponse<List<CartItemResponse>> validateAndUpdateCartItems(Integer userId);
    
    /**
     * Lấy CartItem entity by ID
     */
    CartItem getCartItemById(Integer cartItemId);
    
    /**
     * Kiểm tra CartItem có thuộc về user không
     */
    boolean isCartItemBelongsToUser(Integer cartItemId, Integer userId);

    /**
     * Cập nhật trạng thái chọn/bỏ CartItem
     */
    ApiResponse<CartItemResponse> updateCartItemSelected(Integer cartItemId, Boolean selected);

    /**
     * Đảo trạng thái chọn/bỏ CartItem
     */
    ApiResponse<CartItemResponse> toggleCartItemSelected(Integer cartItemId);

    /**
     * Thêm sản phẩm vào giỏ hàng thông minh (tự động chọn flash sale)
     * @param request SmartCartItemRequest
     * @return ApiResponse<CartItemResponse>
     */
    ApiResponse<CartItemResponse> addSmartItemToCart(SmartCartItemRequest request);
    
    /**
     * Xử lý các CartItem có flash sale đã hết hạn
     * Tự động chuyển về giá gốc khi flash sale expire
     * @return Số lượng cart items đã được cập nhật
     */
    int handleExpiredFlashSalesInCart();
    
    /**
     * Xử lý CartItem của một flash sale cụ thể đã hết hạn
     * Được gọi bởi dynamic scheduler tại thời điểm flash sale kết thúc
     * @param flashSaleId ID của flash sale đã hết hạn
     * @return Số lượng cart items đã được cập nhật
     */
    int handleExpiredFlashSaleInCart(Integer flashSaleId);
    
    /**
     * Xử lý batch CartItems của nhiều flash sales đã hết hạn cùng lúc
     * Optimized cho trường hợp nhiều flash sales cùng expire
     * @param flashSaleIds List các flash sale IDs đã hết hạn
     * @return Số lượng cart items đã được cập nhật
     */
    int handleExpiredFlashSalesInCartBatch(List<Integer> flashSaleIds);

    /**
     * 🔥 NEW: Sync cart items khi flash sale được gia hạn/cập nhật
     * Tự động apply flash sale mới cho cart items của sản phẩm tương ứng
     * @param flashSaleId ID của flash sale được gia hạn
     * @return Số lượng cart items đã được sync
     */
    int syncCartItemsWithUpdatedFlashSale(Integer flashSaleId);

    /**
     * 🔥 NEW: Sync cart items khi tạo flash sale mới
     * Tự động apply flash sale cho cart items đã có của sản phẩm đó
     * @param flashSaleId ID của flash sale mới tạo
     * @return Số lượng cart items đã được sync
     */
    int syncCartItemsWithNewFlashSale(Integer flashSaleId);

    /**
     * 🧹 CLEANUP: Merge duplicate cart items cho cùng book
     * @param userId User ID để cleanup
     * @return Số lượng items đã được merge
     */
    int mergeDuplicateCartItemsForUser(Integer userId);
}
