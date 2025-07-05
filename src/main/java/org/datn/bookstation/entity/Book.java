package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "book")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // ✅ THÊM MỚI: Giảm giá trực tiếp cho book (không phải flash sale)
    @Column(name = "discount_value", precision = 10, scale = 2)
    private BigDecimal discountValue; // Giảm theo giá trị (VD: giảm 50,000 VND)

    @Column(name = "discount_percent")
    private Integer discountPercent; // Giảm giá theo phần trăm (VD: giảm 20%)

    @ColumnDefault("0")
    @Column(name = "discount_active")
    private Boolean discountActive; // Trạng thái kích hoạt discount

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "publication_date")
    private Long publicationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // ✅ THÊM MỚI: Nhà xuất bản  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    // ✅ THÊM MỚI: Ảnh bìa sách
    @Size(max = 2000)
    @Nationalized
    @Column(name = "cover_image_url", length = 2000)
    private String coverImageUrl;

    // ✅ THÊM MỚI: Người dịch
    @Size(max = 255)
    @Nationalized
    @Column(name = "translator")
    private String translator;

    // ✅ THÊM MỚI: ISBN
    @Size(max = 20)
    @Column(name = "isbn", length = 20)
    private String isbn;

    // ✅ THÊM MỚI: Số trang
    @Column(name = "page_count")
    private Integer pageCount;

    // ✅ THÊM MỚI: Ngôn ngữ
    @Size(max = 50)
    @Nationalized
    @Column(name = "language", length = 50)
    private String language;

    // ✅ THÊM MỚI: Cân nặng (gram)
    @Column(name = "weight")
    private Integer weight;

    // ✅ THÊM MỚI: Kích thước (dài x rộng x cao) cm
    @Size(max = 50)
    @Column(name = "dimensions", length = 50)
    private String dimensions;

    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Size(max = 255)
    @NotNull
    @Column(name = "book_code", nullable = false)
    private String bookCode;
    
    // ✅ THÊM MỚI: Relationship với AuthorBook
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AuthorBook> authorBooks = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}