package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public Optional<Order> findByCode(String code) {
        return orderRepository.findByCode(code);
    }
}
