package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.AuthorResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.entity.AuthorBook;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TrendingBookMapper {
    
    /**
     * Chuyển đổi từ Object[] query result sang TrendingBookResponse
     */
    public TrendingBookResponse mapToTrendingBookResponse(Object[] data, int rank, Map<Integer, List<AuthorBook>> authorsMap) {
        TrendingBookResponse response = new TrendingBookResponse();
        
        // Basic book info - mapping theo thứ tự trong query
        response.setId((Integer) data[0]); // bookId
        response.setBookName((String) data[1]); // bookName
        response.setDescription((String) data[2]); // description
        response.setPrice((BigDecimal) data[3]); // price
        response.setOriginalPrice((BigDecimal) data[3]); // originalPrice = price initially
        response.setStockQuantity((Integer) data[4]); // stockQuantity
        response.setBookCode((String) data[5]); // bookCode
        response.setPublicationDate((Long) data[6]); // publicationDate
        response.setCreatedAt((Long) data[7]); // createdAt
        response.setUpdatedAt((Long) data[8]); // updatedAt
        
        // Category info
        response.setCategoryId((Integer) data[9]); // categoryId
        response.setCategoryName((String) data[10]); // categoryName
        
        // Supplier info
        response.setSupplierId((Integer) data[11]); // supplierId
        response.setSupplierName((String) data[12]); // supplierName
        
        // Sales data
        Long soldCountLong = (Long) data[13]; // soldCount
        Long orderCountLong = (Long) data[14]; // orderCount
        response.setSoldCount(soldCountLong != null ? soldCountLong.intValue() : 0);
        response.setOrderCount(orderCountLong != null ? orderCountLong.intValue() : 0);
        
        // Review data
        Double avgRating = (Double) data[15]; // avgRating
        Long reviewCountLong = (Long) data[16]; // reviewCount
        response.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        response.setReviewCount(reviewCountLong != null ? reviewCountLong.intValue() : 0);
        
        // Flash sale info
        Boolean isInFlashSale = (Boolean) data[17]; // isInFlashSale
        BigDecimal flashSalePrice = (BigDecimal) data[18]; // flashSalePrice
        Integer flashSaleStockQuantity = (Integer) data[19]; // flashSaleStockQuantity
        Integer flashSaleSoldCount = (Integer) data[20]; // flashSaleSoldCount
        
        response.setIsInFlashSale(isInFlashSale != null ? isInFlashSale : false);
        response.setFlashSalePrice(flashSalePrice);
        response.setFlashSaleStockQuantity(flashSaleStockQuantity);
        response.setFlashSaleSoldCount(flashSaleSoldCount != null ? flashSaleSoldCount : 0);
        
        // Calculate discount percentage if in flash sale
        if (response.getIsInFlashSale() && flashSalePrice != null && response.getPrice() != null) {
            BigDecimal discount = response.getPrice().subtract(flashSalePrice);
            BigDecimal discountPercentage = discount.divide(response.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setDiscountPercentage(discountPercentage.intValue());
            // Giá gốc vẫn giữ nguyên, price sẽ là giá sau discount
            response.setPrice(flashSalePrice);
            response.setDiscountActive(true);
        } else {
            response.setDiscountPercentage(0);
            response.setDiscountActive(false);
        }
        
        // Trending info
        response.setTrendingRank(rank);
        response.setTrendingScore(calculateTrendingScore(response));
        
        // Set authors if available
        List<AuthorBook> bookAuthors = authorsMap.get(response.getId());
        if (bookAuthors != null && !bookAuthors.isEmpty()) {
            List<AuthorResponse> authors = bookAuthors.stream()
                .map(this::mapToAuthorResponse)
                .collect(Collectors.toList());
            response.setAuthors(authors);
        }
        
        // Set images (nhiều ảnh)
        String imagesStr = (String) data[21]; // images (đã chuyển từ data[20] thành data[21])
        if (imagesStr != null && !imagesStr.isEmpty()) {
            List<String> images = java.util.Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            response.setImages(images);
        } else {
            response.setImages(java.util.Collections.emptyList());
        }
        
        return response;
    }
    
    /**
     * Tính điểm trending dựa trên công thức business
     */
    private Double calculateTrendingScore(TrendingBookResponse book) {
        // Sales Score (40%)
        double salesScore = (book.getSoldCount() * 0.5 + book.getOrderCount() * 0.3 + 
                           (book.getOrderCount() > 0 ? 5 : 0) * 0.2) / 10.0; // Normalize to 0-10
        
        // Review Score (30%)  
        double reviewScore = (book.getRating() * 0.6 + Math.min(book.getReviewCount() / 10.0, 5) * 0.4);
        
        // Recency Score (20%) - sách mới có điểm cao hơn
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        double recencyScore = book.getCreatedAt() > thirtyDaysAgo ? 8.0 : 3.0;
        
        // Flash Sale Bonus (10%)
        double flashSaleBonus = book.getIsInFlashSale() ? 10.0 : 0.0;
        
        double totalScore = (salesScore * 0.4) + (reviewScore * 0.3) + 
                          (recencyScore * 0.2) + (flashSaleBonus * 0.1);
        
        return Math.round(totalScore * 10.0) / 10.0; // Round to 1 decimal place
    }
    
    /**
     * Chuyển đổi AuthorBook sang AuthorResponse
     */
    private AuthorResponse mapToAuthorResponse(AuthorBook authorBook) {
        AuthorResponse response = new AuthorResponse();
        response.setId(authorBook.getAuthor().getId());
        response.setAuthorName(authorBook.getAuthor().getAuthorName());
        response.setBiography(authorBook.getAuthor().getBiography());
        response.setBirthDate(authorBook.getAuthor().getBirthDate());
        response.setStatus(authorBook.getAuthor().getStatus());
        return response;
    }

    /**
     * 🔥 FALLBACK: Map fallback books (chưa có đủ dữ liệu trending)
     * Gán điểm trending thấp hơn nhưng vẫn đảm bảo có sản phẩm hiển thị
     */
    public TrendingBookResponse mapToFallbackTrendingBookResponse(Object[] data, int rank, Map<Integer, List<AuthorBook>> authorsMap) {
        TrendingBookResponse response = new TrendingBookResponse();
        
        // Basic book info - mapping theo thứ tự trong fallback query
        response.setId((Integer) data[0]); // bookId
        response.setBookName((String) data[1]); // bookName
        response.setDescription((String) data[2]); // description
        response.setPrice((BigDecimal) data[3]); // price
        response.setOriginalPrice((BigDecimal) data[3]); // originalPrice = price
        response.setStockQuantity((Integer) data[4]); // stockQuantity
        response.setBookCode((String) data[5]); // bookCode
        response.setPublicationDate((Long) data[6]); // publicationDate
        response.setCreatedAt((Long) data[7]); // createdAt
        response.setUpdatedAt((Long) data[8]); // updatedAt
        
        // Category info
        response.setCategoryId((Integer) data[9]); // categoryId
        response.setCategoryName((String) data[10]); // categoryName
        
        // Supplier info
        response.setSupplierId((Integer) data[11]); // supplierId
        response.setSupplierName((String) data[12]); // supplierName
        
        // Sales data (fallback = 0)
        response.setSoldCount(0);
        response.setOrderCount(0);
        
        // Review data (fallback = 0)
        response.setRating(0.0);
        response.setReviewCount(0);
        
        // Flash sale info (fallback = false)
        response.setIsInFlashSale(false);
        response.setFlashSalePrice(null);
        response.setFlashSaleStockQuantity(null);
        response.setFlashSaleSoldCount(0); // ✅ Fallback flash sale sold count
        response.setDiscountPercentage(0);
        
        // 🔥 FALLBACK TRENDING SCORE: Dựa trên độ mới và các yếu tố khác
        response.setTrendingRank(rank);
        response.setTrendingScore(calculateFallbackTrendingScore(response));
        
        // Set authors if available
        List<AuthorBook> bookAuthors = authorsMap.get(response.getId());
        if (bookAuthors != null && !bookAuthors.isEmpty()) {
            List<AuthorResponse> authors = bookAuthors.stream()
                .map(this::mapToAuthorResponse)
                .collect(Collectors.toList());
            response.setAuthors(authors);
        }
        
        // Set images (nhiều ảnh)
        String imagesStr = (String) data[21]; // images (đã chuyển từ data[20] thành data[21])
        if (imagesStr != null && !imagesStr.isEmpty()) {
            List<String> images = java.util.Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            response.setImages(images);
        } else {
            response.setImages(java.util.Collections.emptyList());
        }
        
        return response;
    }

    /**
     * 🔥 FALLBACK: Tính trending score cho sách chưa có đủ dữ liệu
     * Dựa trên: Độ mới (60%) + Giá cả (20%) + Stock (20%)
     */
    private Double calculateFallbackTrendingScore(TrendingBookResponse book) {
        // Recency Score (60%) - Ưu tiên sách mới
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        
        double recencyScore;
        if (book.getCreatedAt() > sevenDaysAgo) {
            recencyScore = 8.0; // Sách mới trong 7 ngày
        } else if (book.getCreatedAt() > thirtyDaysAgo) {
            recencyScore = 6.0; // Sách mới trong 30 ngày
        } else {
            recencyScore = 3.0; // Sách cũ
        }
        
        // Price Score (20%) - Giá hợp lý có điểm cao hơn
        double priceScore = 5.0; // Điểm trung bình
        if (book.getPrice() != null) {
            // Giá từ 50k-200k: điểm cao
            // Giá < 50k hoặc > 500k: điểm thấp
            BigDecimal price = book.getPrice();
            if (price.compareTo(BigDecimal.valueOf(50000)) >= 0 && 
                price.compareTo(BigDecimal.valueOf(200000)) <= 0) {
                priceScore = 7.0;
            } else if (price.compareTo(BigDecimal.valueOf(500000)) > 0) {
                priceScore = 3.0;
            }
        }
        
        // Stock Score (20%) - Stock nhiều có điểm cao hơn
        double stockScore = 5.0; // Điểm trung bình
        if (book.getStockQuantity() != null) {
            if (book.getStockQuantity() >= 50) {
                stockScore = 8.0;
            } else if (book.getStockQuantity() >= 20) {
                stockScore = 6.0;
            } else if (book.getStockQuantity() >= 5) {
                stockScore = 4.0;
            } else {
                stockScore = 2.0;
            }
        }
        
        // Tổng điểm fallback (thấp hơn trending thực)
        double totalScore = (recencyScore * 0.6) + (priceScore * 0.2) + (stockScore * 0.2);
        
        // Điểm fallback luôn < 5.0 để phân biệt với trending thực
        totalScore = Math.min(totalScore, 4.9);
        
        return Math.round(totalScore * 10.0) / 10.0;
    }
}
