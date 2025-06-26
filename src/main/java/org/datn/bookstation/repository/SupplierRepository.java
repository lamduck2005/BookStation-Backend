package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Page<Supplier> findAll(Specification<Supplier> spec, Pageable pageable);
}