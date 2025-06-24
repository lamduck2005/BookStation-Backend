package org.datn.bookstation.service;

import java.util.Optional;

public interface OrderService {
    Optional<Integer> findIdByCode(String code);
}
