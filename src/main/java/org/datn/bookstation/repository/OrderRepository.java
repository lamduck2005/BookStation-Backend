package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    Optional<Integer> findIdByCode(String code);
}
