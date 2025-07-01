package org.datn.bookstation.repository;

import org.datn.bookstation.entity.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EventCategoryRepository extends JpaRepository<EventCategory, Integer>, JpaSpecificationExecutor<EventCategory> {
    boolean existsByCategoryName(String categoryName);
    
    List<EventCategory> findByIsActiveTrue();
}
