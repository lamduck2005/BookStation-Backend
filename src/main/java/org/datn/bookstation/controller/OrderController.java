package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.EnumOptionResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<OrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        PaginationResponse<OrderResponse> orders = orderService.getAllWithPagination(page, size, code, userId, orderStatus, orderType, startDate, endDate);
        ApiResponse<PaginationResponse<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", orders);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Integer id) {
        OrderResponse order = orderService.getByIdWithDetails(id);
        if (order == null) {
            ApiResponse<OrderResponse> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy đơn hàng", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        ApiResponse<OrderResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", order);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest orderRequest) {
        ApiResponse<OrderResponse> response = orderService.create(orderRequest);
        HttpStatus status = response.getStatus() == 201 ? HttpStatus.CREATED : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(@PathVariable Integer id, @Valid @RequestBody OrderRequest orderRequest) {
        ApiResponse<OrderResponse> response = orderService.update(orderRequest, id);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Integer id,
            @RequestParam OrderStatus newStatus,
            @RequestParam(required = false) Integer staffId) {
        ApiResponse<OrderResponse> response = orderService.updateStatus(id, newStatus, staffId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason,
            @RequestParam Integer userId) {
        ApiResponse<OrderResponse> response = orderService.cancelOrder(id, reason, userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(@PathVariable Integer userId) {
        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        ApiResponse<List<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", orders);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderResponse> orders = orderService.getOrdersByStatus(status);
        ApiResponse<List<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", orders);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id")
    public ResponseEntity<Integer> getOrderIdByCode(@RequestParam String orderCode) {
        return orderService.findIdByCode(orderCode)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/order-statuses")
    public ResponseEntity<ApiResponse<List<EnumOptionResponse>>> getOrderStatuses() {
        List<EnumOptionResponse> orderStatuses = Arrays.stream(OrderStatus.values())
            .map(status -> new EnumOptionResponse(status.name(), getOrderStatusDisplayName(status)))
            .collect(Collectors.toList());
        ApiResponse<List<EnumOptionResponse>> response = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Lấy danh sách trạng thái đơn hàng thành công", 
            orderStatuses
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order-types")
    public ResponseEntity<ApiResponse<List<EnumOptionResponse>>> getOrderTypes() {
        List<EnumOptionResponse> orderTypes = List.of(
            new EnumOptionResponse("NORMAL", "Đơn hàng thường"),
            new EnumOptionResponse("EVENT_GIFT", "Đơn hàng giao quà sự kiện"),
            new EnumOptionResponse("PROMOTIONAL", "Đơn hàng khuyến mãi đặc biệt"),
            new EnumOptionResponse("SAMPLE", "Đơn hàng gửi mẫu")
        );
        ApiResponse<List<EnumOptionResponse>> response = new ApiResponse<>(
            HttpStatus.OK.value(), 
            "Lấy danh sách loại đơn hàng thành công", 
            orderTypes
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dropdown")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownOrders() {
        List<Order> orders = orderService.getAll();
        List<DropdownOptionResponse> dropdown = orders.stream()
            .map(order -> new DropdownOptionResponse(order.getId(), order.getCode()))
            .collect(Collectors.toList());
        ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Lấy danh sách đơn hàng thành công", dropdown);
        return ResponseEntity.ok(response);
    }

    private String getOrderStatusDisplayName(OrderStatus orderStatus) {
        switch (orderStatus) {
            case PENDING: return "Chờ xử lý";
            case CONFIRMED: return "Đã xác nhận";
            case SHIPPED: return "Đang giao hàng";
            case DELIVERED: return "Đã giao hàng";
            case CANCELED: return "Đã hủy";
            case REFUNDING: return "Đang hoàn tiền";
            case REFUNDED: return "Đã hoàn tiền";
            case RETURNED: return "Đã trả hàng";
            case PARTIALLY_REFUNDED: return "Hoàn tiền một phần";
            default: return orderStatus.name();
        }
    }
}
