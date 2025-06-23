package org.datn.bookstation.service;

import org.datn.bookstation.entity.Order;
import java.util.Optional;

public interface OrderService {
    Optional<Order> findByCode(String code);
}
