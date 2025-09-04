package org.datn.bookstation.controller;

import org.datn.bookstation.entity.enums.OrderStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @GetMapping("/order-status-count")
    public int getOrderStatusCount() {
        return OrderStatus.values().length;
    }
    
    @GetMapping("/order-status-all")
    public List<String> getAllOrderStatuses() {
        return Arrays.stream(OrderStatus.values())
                .map(Enum::name)
                .toList();
    }
}
