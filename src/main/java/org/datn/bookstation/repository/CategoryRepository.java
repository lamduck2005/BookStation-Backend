package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
//    @Query(
//            """
//           select NEW org.datn.bookstation.dto.response.ParentCategoryResponse(c.id,c.categoryName,c.description,c.parentCategory.id,c.parentCategory.categoryName,c.parentCategory.description) from Category c
//"""
//    )
//    public List<ParentCategoryResponse> getAllParentCategoryRequests();


    @Query("SELECT c1.id , c1.categoryName , c1.description , " +
            "c1.status , c2.id , c2.categoryName , " +
            "c2.description AS childDesc, c2.status " +
            "FROM Category c1 LEFT JOIN Category c2 ON c1.id = c2.parentCategory.id")
    List<Category> findCategoryHierarchy();

    @Query("""
            select c from Category c where c.parentCategory is null and c.id = :id
            """)
    Category getByParentCategoryIsNull(@Param("id") Integer id);

    List<Category> findByStatus(Byte status);
}
