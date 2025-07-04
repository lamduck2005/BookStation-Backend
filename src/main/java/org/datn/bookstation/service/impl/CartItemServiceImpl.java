package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.CartItemRequest;
import org.datn.bookstation.dto.request.BatchCartItemRequest;
import org.datn.bookstation.dto.request.SmartCartItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.mapper.CartItemMapper;
import org.datn.bookstation.mapper.CartItemResponseMapper;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.CartItemService;
import org.datn.bookstation.service.FlashSaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {
    
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemResponseMapper cartItemResponseMapper;
    private final FlashSaleService flashSaleService;
    private final CartRepository cartRepository;

    public CartItemServiceImpl(
            CartItemRepository cartItemRepository,
            BookRepository bookRepository,
            UserRepository userRepository,
            CartItemMapper cartItemMapper,
            CartItemResponseMapper cartItemResponseMapper,
            FlashSaleService flashSaleService,
            CartRepository cartRepository) {
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.cartItemMapper = cartItemMapper;
        this.cartItemResponseMapper = cartItemResponseMapper;
        this.flashSaleService = flashSaleService;
        this.cartRepository = cartRepository;
    }

    @Override
    public List<CartItemResponse> getCartItemsByUserId(Integer userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream()
                .map(cartItemResponseMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 🔥 ENHANCED: Thêm cart item với AUTO-DETECTION và validation toàn diện
     */
    @Override
    public ApiResponse<CartItemResponse> addItemToCart(CartItemRequest request) {
        try {
            // 1. Validate user
            Optional<User> userOpt = userRepository.findById(request.getUserId());
            if (userOpt.isEmpty()) {
                return new ApiResponse<>(404, "User không tồn tại", null);
            }
            
            // 2. Validate book
            Optional<Book> bookOpt = bookRepository.findById(request.getBookId());
            if (bookOpt.isEmpty()) {
                return new ApiResponse<>(404, "Sách không tồn tại", null);
            }
            
            Book book = bookOpt.get();
            if (book.getStatus() != 1) {
                return new ApiResponse<>(400, "Sách đã ngừng bán", null);
            }
            
            // 3. 🔥 AUTO-DETECT: Tìm flash sale tốt nhất cho sách này
            FlashSaleItem flashSaleItem = null;
            String flashSaleMessage = "";
            Optional<FlashSaleItem> activeFlashSaleOpt = flashSaleService.findActiveFlashSaleForBook(request.getBookId().longValue());
            
            if (activeFlashSaleOpt.isPresent()) {
                FlashSaleItem candidate = activeFlashSaleOpt.get();
                
                // Validate flash sale stock
                if (request.getQuantity() <= candidate.getStockQuantity()) {
                    flashSaleItem = candidate;
                    flashSaleMessage = " 🔥 Đã áp dụng flash sale!";
                } else {
                    flashSaleMessage = " ⚠️ Flash sale không đủ hàng, đã áp dụng giá gốc";
                }
            }
            
            // 4. Validate stock comprehensive
            ApiResponse<String> stockValidation = validateStock(book, flashSaleItem, request.getQuantity());
            if (stockValidation.getStatus() != 200) {
                return new ApiResponse<>(stockValidation.getStatus(), stockValidation.getMessage(), null);
            }
            
            // 5. Get or create cart directly
            Cart cart = getOrCreateCart(request.getUserId());
            
            // 6. Check existing cart item
            Integer flashSaleItemId = flashSaleItem != null ? flashSaleItem.getId() : null;
            Optional<CartItem> existingItemOpt = cartItemRepository.findExistingCartItem(
                cart.getId(), request.getBookId(), flashSaleItemId);
                
            CartItem cartItem;
            if (existingItemOpt.isPresent()) {
                // Update existing item với validation
                cartItem = existingItemOpt.get();
                int newQuantity = cartItem.getQuantity() + request.getQuantity();
                
                // Re-validate stock for new total quantity
                ApiResponse<String> updateStockValidation = validateStock(book, flashSaleItem, newQuantity);
                if (updateStockValidation.getStatus() != 200) {
                    return new ApiResponse<>(updateStockValidation.getStatus(), 
                        "Bạn đã có " + cartItem.getQuantity() + " trong giỏ. " + updateStockValidation.getMessage(), null);
                }
                
                cartItem.setQuantity(newQuantity);
                cartItem.setUpdatedBy(request.getUserId());
                cartItem.setUpdatedAt(System.currentTimeMillis());
            } else {
                // Create new item
                cartItem = cartItemMapper.toEntity(request);
                cartItem.setCart(cart);
                cartItem.setBook(book);
                cartItem.setFlashSaleItem(flashSaleItem);
                cartItem.setCreatedBy(request.getUserId());
                cartItem.setCreatedAt(System.currentTimeMillis());
                cartItem.setUpdatedAt(System.currentTimeMillis());
            }
            
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemResponse response = cartItemResponseMapper.toResponse(savedItem);
            
            return new ApiResponse<>(200, "Thêm sản phẩm vào giỏ hàng thành công" + flashSaleMessage, response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi thêm sản phẩm: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CartItemResponse> updateCartItem(Integer cartItemId, Integer quantity) {
        try {
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                return new ApiResponse<>(404, "CartItem không tồn tại", null);
            }
            
            CartItem cartItem = cartItemOpt.get();
            
            // Validate quantity
            if (quantity <= 0) {
                // Delete cart item
                cartItemRepository.delete(cartItem);
                return new ApiResponse<>(200, "Xóa sản phẩm khỏi giỏ hàng thành công", null);
            }
            
            // Validate stock
            ApiResponse<String> stockValidation = validateStock(cartItem.getBook(), cartItem.getFlashSaleItem(), quantity);
            if (stockValidation.getStatus() != 200) {
                return new ApiResponse<>(stockValidation.getStatus(), stockValidation.getMessage(), null);
            }
            
            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(System.currentTimeMillis());
            CartItem savedItem = cartItemRepository.save(cartItem);
            CartItemResponse response = cartItemResponseMapper.toResponse(savedItem);
            
            return new ApiResponse<>(200, "Cập nhật số lượng thành công", response);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> removeCartItem(Integer cartItemId) {
        try {
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
            if (cartItemOpt.isEmpty()) {
                return new ApiResponse<>(404, "CartItem không tồn tại", null);
            }
            
            cartItemRepository.deleteById(cartItemId);
            return new ApiResponse<>(200, "Xóa sản phẩm khỏi giỏ hàng thành công", "OK");
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi xóa sản phẩm: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<CartItemResponse>> addItemsToCartBatch(BatchCartItemRequest request) {
        try {
            List<CartItemResponse> results = new ArrayList<>();
            
            for (CartItemRequest itemRequest : request.getItems()) {
                // Set userId từ batch request
                itemRequest.setUserId(request.getUserId());
                
                ApiResponse<CartItemResponse> addResult = addItemToCart(itemRequest);
                if (addResult.getStatus() == 200 && addResult.getData() != null) {
                    results.add(addResult.getData());
                }
                // Note: Có thể xử lý lỗi riêng lẻ hoặc fail toàn bộ batch
            }
            
            return new ApiResponse<>(200, "Thêm " + results.size() + "/" + request.getItems().size() + " sản phẩm thành công", results);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi thêm batch: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> clearCartItems(Integer cartId) {
        try {
            cartItemRepository.deleteByCartId(cartId);
            return new ApiResponse<>(200, "Xóa toàn bộ items thành công", "OK");
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi xóa items: " + e.getMessage(), null);
        }
    }

    /**
     * 🔥 ENHANCED: Validate cart với nhiều business rules
     */
    @Override
    public ApiResponse<List<CartItemResponse>> validateAndUpdateCartItems(Integer userId) {
        try {
            long currentTime = System.currentTimeMillis();
            int totalUpdated = 0;
            List<String> warnings = new ArrayList<>();
            
            // 1. Xử lý flash sale hết hạn
            List<CartItem> expiredItems = cartItemRepository.findExpiredFlashSaleItems(userId, currentTime);
            for (CartItem item : expiredItems) {
                item.setFlashSaleItem(null);
                item.setUpdatedBy(userId);
                item.setUpdatedAt(currentTime);
                cartItemRepository.save(item);
                totalUpdated++;
                warnings.add("Flash sale \"" + item.getBook().getBookName() + "\" đã hết hạn, đã chuyển về giá gốc");
            }
            
            // 2. Kiểm tra stock vượt quá
            List<CartItem> exceededItems = cartItemRepository.findCartItemsExceedingStock(userId);
            for (CartItem item : exceededItems) {
                int availableStock = item.getFlashSaleItem() != null ? 
                    item.getFlashSaleItem().getStockQuantity() : item.getBook().getStockQuantity();
                warnings.add("Sách \"" + item.getBook().getBookName() + "\" trong giỏ hàng (" + item.getQuantity() + ") vượt quá tồn kho (" + availableStock + ")");
            }
            
            // 3. Warning flash sale sắp hết hạn (còn 5 phút)
            long warningTime = currentTime + (5 * 60 * 1000); // 5 phút sau
            List<CartItem> aboutToExpireItems = cartItemRepository.findFlashSaleItemsAboutToExpire(currentTime, warningTime);
            for (CartItem item : aboutToExpireItems) {
                long remainingMinutes = (item.getFlashSaleItem().getFlashSale().getEndTime() - currentTime) / (60 * 1000);
                warnings.add("Flash sale \"" + item.getBook().getBookName() + "\" sẽ hết hạn trong " + remainingMinutes + " phút");
            }
            
            // 4. Lấy danh sách items mới nhất
            List<CartItemResponse> updatedItems = getCartItemsByUserId(userId);
            
            String message = buildValidationMessage(totalUpdated, warnings);
            return new ApiResponse<>(200, message, updatedItems);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi validate giỏ hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public CartItem getCartItemById(Integer cartItemId) {
        return cartItemRepository.findById(cartItemId).orElse(null);
    }

    @Override
    public boolean isCartItemBelongsToUser(Integer cartItemId, Integer userId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            return false;
        }
        
        CartItem cartItem = cartItemOpt.get();
        return cartItem.getCart() != null && 
               cartItem.getCart().getUser() != null && 
               cartItem.getCart().getUser().getId().equals(userId);
    }

    @Override
    @Transactional
    public ApiResponse<CartItemResponse> addSmartItemToCart(SmartCartItemRequest request) {
        // SmartCartItemRequest giờ đây chỉ là wrapper, logic auto-detect đã được tích hợp vào addItemToCart
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .userId(request.getUserId().intValue())
                .bookId(request.getBookId().intValue())
                .quantity(request.getQuantity())
                .build();
        
        // Gọi method add với auto-detect logic
        return addItemToCart(cartItemRequest);
    }

    @Override
    public int handleExpiredFlashSalesInCart() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // Tìm tất cả cart items có flash sale đã hết hạn
            List<CartItem> expiredItems = cartItemRepository.findAllExpiredFlashSaleItems(currentTime);
            
            // Update chúng về regular price
            for (CartItem item : expiredItems) {
                item.setFlashSaleItem(null);
                item.setUpdatedAt(currentTime);
                cartItemRepository.save(item);
            }
            
            return expiredItems.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int handleExpiredFlashSaleInCart(Integer flashSaleId) {
        try {
            // Tìm cart items của flash sale cụ thể này
            List<CartItem> expiredItems = cartItemRepository.findByFlashSaleId(flashSaleId);
            
            // Update chúng về regular price
            long currentTime = System.currentTimeMillis();
            for (CartItem item : expiredItems) {
                item.setFlashSaleItem(null);
                item.setUpdatedAt(currentTime);
                cartItemRepository.save(item);
            }
            
            return expiredItems.size();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int handleExpiredFlashSalesInCartBatch(List<Integer> flashSaleIds) {
        try {
            if (flashSaleIds == null || flashSaleIds.isEmpty()) {
                return 0;
            }
            
            // Batch update tất cả cart items của các flash sales này
            int updatedCount = cartItemRepository.batchUpdateExpiredFlashSales(flashSaleIds, System.currentTimeMillis());
            
            // Log để tracking
            if (updatedCount > 0) {
                System.out.println("🔥 BATCH EXPIRATION: Updated " + updatedCount + " cart items for flash sales: " + flashSaleIds);
            }
            
            return updatedCount;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // ================== PRIVATE HELPER METHODS ==================
    
    /**
     * Validate stock cho book hoặc flash sale
     */
    private ApiResponse<String> validateStock(Book book, FlashSaleItem flashSaleItem, Integer requestedQuantity) {
        if (flashSaleItem != null) {
            // Using flash sale stock
            if (requestedQuantity > flashSaleItem.getStockQuantity()) {
                return new ApiResponse<>(400, "Flash sale không đủ hàng. Còn lại: " + flashSaleItem.getStockQuantity(), null);
            }
        } else {
            // Using regular book stock  
            if (requestedQuantity > book.getStockQuantity()) {
                return new ApiResponse<>(400, "Không đủ hàng tồn kho. Còn lại: " + book.getStockQuantity(), null);
            }
        }
        return new ApiResponse<>(200, "Stock OK", "OK");
    }
    
    /**
     * Get or create cart for user
     */
    private Cart getOrCreateCart(Integer userId) {
        Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
        
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        
        // Tạo cart mới
        Cart newCart = new Cart();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        newCart.setUser(user);
        newCart.setCreatedAt(System.currentTimeMillis());
        newCart.setCreatedBy(userId);
        newCart.setStatus((byte) 1); // Active
        
        return cartRepository.save(newCart);
    }
    
    /**
     * Build validation message từ results
     */
    private String buildValidationMessage(int updatedCount, List<String> warnings) {
        if (updatedCount == 0 && warnings.isEmpty()) {
            return "Giỏ hàng đã được kiểm tra - Không có vấn đề";
        }
        
        StringBuilder message = new StringBuilder();
        if (updatedCount > 0) {
            message.append("Đã cập nhật ").append(updatedCount).append(" sản phẩm flash sale hết hạn. ");
        }
        if (!warnings.isEmpty()) {
            message.append("Cảnh báo: ").append(String.join("; ", warnings));
        }
        
        return message.toString();
    }
}
