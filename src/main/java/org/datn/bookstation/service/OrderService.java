package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    Optional<Integer> findIdByCode(String code);
    
    PaginationResponse<OrderResponse> getAllWithPagination(int page, int size, String code, 
            Integer userId, OrderStatus orderStatus, String orderType, Long startDate, Long endDate);
    
    List<Order> getAll();
    
    Order getById(Integer id);
    
    OrderResponse getByIdWithDetails(Integer id);
    
    ApiResponse<OrderResponse> create(OrderRequest request);
    
    ApiResponse<OrderResponse> update(OrderRequest request, Integer id);
    
    ApiResponse<OrderResponse> updateStatus(Integer id, OrderStatus newStatus, Integer staffId);
    
    void delete(Integer id);
    
    List<OrderResponse> getOrdersByUser(Integer userId);
    
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    
    ApiResponse<OrderResponse> cancelOrder(Integer id, String reason, Integer userId);
}
