package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Book;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class BookSpecification {

    public static Specification<Book> filterBy(String bookName, Integer categoryId, Integer supplierId,
            Integer publisherId,
            BigDecimal minPrice, BigDecimal maxPrice, Byte status,
            String bookCode) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (bookName != null && !bookName.isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")),
                                "%" + bookName.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (supplierId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("supplier").get("id"), supplierId));
            }

            if (publisherId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("publisher").get("id"), publisherId));
            }

            if (minPrice != null && maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.between(root.get("price"), minPrice, maxPrice));
            } else if (minPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            } else if (maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("status"), status));
            }

            if (bookCode != null && !bookCode.isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookCode")),
                                "%" + bookCode.toLowerCase() + "%"));
            }

            return predicates;
        };
    }

    public static Specification<Book> filterBy(String bookName, Integer categoryId, Integer parentCategoryId,
            Integer publisherId,
            BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (bookName != null && !bookName.isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")),
                                "%" + bookName.toLowerCase() + "%"));
            }
            if (parentCategoryId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("parentCategory").get("id"), parentCategoryId));
            }
            if (categoryId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (publisherId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("publisher").get("id"), publisherId));
            }

            if (minPrice != null && maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.between(root.get("price"), minPrice, maxPrice));
            } else if (minPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            } else if (maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates;
        };
    }

    public static Specification<Book> filterBy(Integer categoryId, String text) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            System.out.println(categoryId);
            if (categoryId != null&&categoryId!=0) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (text != null && !text.isEmpty()) {
                String likeText = "%" + text.toLowerCase() + "%";
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")), likeText));
            }

            return predicates;
        };
    }
    public static Specification<Book> filterBy( String text) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            if (text != null && !text.isEmpty()) {
                String likeText = "%" + text.toLowerCase() + "%";
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")), likeText));
            }

            return predicates;
        };
    }
}
