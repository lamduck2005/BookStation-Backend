package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Page<Voucher> findAll(Specification<Voucher> spec, Pageable pageable);
}