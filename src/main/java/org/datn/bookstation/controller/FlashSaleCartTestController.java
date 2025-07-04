package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.service.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 🧪 Testing Controller for Flash Sale Cart Integration
 * Remove this in production - chỉ dùng để test
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/test/flash-sale-cart")
public class FlashSaleCartTestController {
    
    private final CartItemService cartItemService;

    /**
     * Test case: Merge duplicate cart items for user
     */
    @PostMapping("/merge-duplicates/{userId}")
    public ResponseEntity<ApiResponse<String>> testMergeDuplicates(@PathVariable Integer userId) {
        try {
            int mergedCount = cartItemService.mergeDuplicateCartItemsForUser(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, 
                "✅ Test completed: Merged " + mergedCount + " duplicate items for user " + userId, 
                "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "❌ Test failed: " + e.getMessage(), null));
        }
    }

    /**
     * Test case: Sync cart items with flash sale
     */
    @PostMapping("/sync-flash-sale/{flashSaleId}")
    public ResponseEntity<ApiResponse<String>> testSyncFlashSale(@PathVariable Integer flashSaleId) {
        try {
            int syncedCount = cartItemService.syncCartItemsWithUpdatedFlashSale(flashSaleId);
            return ResponseEntity.ok(new ApiResponse<>(200, 
                "✅ Test completed: Synced " + syncedCount + " cart items with flash sale " + flashSaleId, 
                "SUCCESS"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "❌ Test failed: " + e.getMessage(), null));
        }
    }

    /**
     * Test scenario: Simulate flash sale extension issue
     */
    @PostMapping("/simulate-issue")
    public ResponseEntity<ApiResponse<String>> simulateFlashSaleIssue(
            @RequestParam Integer userId,
            @RequestParam Integer bookId,
            @RequestParam Integer quantity) {
        try {
            StringBuilder result = new StringBuilder();
            
            // Step 1: Add item to cart (will auto-detect flash sale if available)
            var addRequest = org.datn.bookstation.dto.request.CartItemRequest.builder()
                .userId(userId)
                .bookId(bookId)
                .quantity(quantity)
                .build();
            
            var addResponse = cartItemService.addItemToCart(addRequest);
            result.append("Step 1 - Add to cart: ").append(addResponse.getMessage()).append("\n");
            
            // Step 2: Add same item again (should merge, not duplicate)
            var addResponse2 = cartItemService.addItemToCart(addRequest);
            result.append("Step 2 - Add again: ").append(addResponse2.getMessage()).append("\n");
            
            // Step 3: Check for duplicates and merge if found
            int mergedCount = cartItemService.mergeDuplicateCartItemsForUser(userId);
            result.append("Step 3 - Merge duplicates: Merged ").append(mergedCount).append(" items\n");
            
            return ResponseEntity.ok(new ApiResponse<>(200, 
                "✅ Simulation completed", 
                result.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "❌ Simulation failed: " + e.getMessage(), null));
        }
    }
}
