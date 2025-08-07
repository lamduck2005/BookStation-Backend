package org.datn.bookstation.controller;

import org.datn.bookstation.dto.request.PriceValidationRequest;
import org.datn.bookstation.dto.request.OrderDetailRefundRequest;
import org.datn.bookstation.dto.request.RefundRequestDto;
import org.datn.bookstation.dto.request.AdminRefundDecisionDto;
import org.datn.bookstation.dto.request.OrderStatusTransitionRequest;
import org.datn.bookstation.dto.response.OrderStatusTransitionResponse;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.OrderRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.RevenueStatsResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.EnumOptionResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.service.OrderService;
import org.datn.bookstation.service.OrderStatusTransitionService;
import org.datn.bookstation.dto.request.OrderCalculationRequest;
import org.datn.bookstation.dto.response.OrderCalculationResponse;
import org.datn.bookstation.service.OrderCalculationService;
import org.datn.bookstation.service.PriceValidationService;
import org.datn.bookstation.utils.OrderStatusUtil;
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
        private final OrderCalculationService orderCalculationService;
        private final PriceValidationService priceValidationService;
        private final OrderStatusTransitionService orderStatusTransitionService;

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
                PaginationResponse<OrderResponse> orders = orderService.getAllWithPagination(page, size, code, userId,
                                orderStatus, orderType, startDate, endDate);
                ApiResponse<PaginationResponse<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(),
                                "Thành công",
                                orders);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Integer id) {
                OrderResponse order = orderService.getByIdWithDetails(id);
                if (order == null) {
                        ApiResponse<OrderResponse> response = new ApiResponse<>(HttpStatus.NOT_FOUND.value(),
                                        "Không tìm thấy đơn hàng", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                ApiResponse<OrderResponse> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công", order);
                return ResponseEntity.ok(response);
        }

        @PostMapping
        public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest orderRequest) {
                // Tạo đơn hàng trực tiếp, không validate giá ở đây nữa vì đã có API riêng
                ApiResponse<OrderResponse> response = orderService.create(orderRequest);
                HttpStatus status = response.getStatus() == 201 ? HttpStatus.CREATED
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<OrderResponse>> update(@PathVariable Integer id,
                        @Valid @RequestBody OrderRequest orderRequest) {
                ApiResponse<OrderResponse> response = orderService.update(orderRequest, id);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        @PatchMapping("/{id}/status")
        public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
                        @PathVariable Integer id,
                        @RequestParam OrderStatus newStatus,
                        @RequestParam(required = false) Integer staffId) {
                ApiResponse<OrderResponse> response = orderService.updateStatus(id, newStatus, staffId);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        /**
         * Endpoint chuyển trạng thái đơn hàng theo tài liệu nghiệp vụ
         * POST /api/orders/{orderId}/status-transition
         */
        @PostMapping("/{orderId}/status-transition")
        public ResponseEntity<ApiResponse<OrderStatusTransitionResponse>> statusTransition(
                        @PathVariable Integer orderId,
                        @Valid @RequestBody OrderStatusTransitionRequest request) {
                // Set orderId từ path parameter vào request
                request.setOrderId(orderId);

                ApiResponse<OrderStatusTransitionResponse> response = orderStatusTransitionService
                                .transitionOrderStatus(request);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        @PatchMapping("/{id}/cancel")
        public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
                        @PathVariable Integer id,
                        @RequestParam(required = false) String reason,
                        @RequestParam Integer userId) {
                ApiResponse<OrderResponse> response = orderService.cancelOrder(id, reason, userId);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        /**
         * ✅ THÊM MỚI: API đánh dấu giao hàng thất bại
         * PATCH /api/orders/{id}/delivery-failed
         */
        @PatchMapping("/{id}/delivery-failed")
        public ResponseEntity<ApiResponse<OrderResponse>> markDeliveryFailed(
                        @PathVariable Integer id,
                        @RequestParam(required = false) String reason,
                        @RequestParam Integer staffId) {
                ApiResponse<OrderResponse> response = orderService.updateStatus(id, OrderStatus.DELIVERY_FAILED,
                                staffId);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        /**
         * ✅ THÊM MỚI: API hoàn trả đơn hàng một phần
         */
        @PostMapping("/{id}/partial-refund")
        public ResponseEntity<ApiResponse<OrderResponse>> partialRefund(
                        @PathVariable Integer id,
                        @RequestParam Integer userId,
                        @RequestParam(required = false) String reason,
                        @RequestBody List<OrderDetailRefundRequest> refundDetails) {
                ApiResponse<OrderResponse> response = orderService.partialRefund(id, userId, reason, refundDetails);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        /**
         * ✅ THÊM MỚI: API hoàn trả đơn hàng toàn bộ
         */
        @PostMapping("/{id}/full-refund")
        public ResponseEntity<ApiResponse<OrderResponse>> fullRefund(
                        @PathVariable Integer id,
                        @RequestParam Integer userId,
                        @RequestParam(required = false) String reason) {
                ApiResponse<OrderResponse> response = orderService.fullRefund(id, userId, reason);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable Integer id) {
                orderService.delete(id);
                return ResponseEntity.noContent().build();
        }

        // ✅ THÊM MỚI: API lấy đơn hàng của user có phân trang
        @GetMapping("/user/{userId}/pagination")
        public ResponseEntity<ApiResponse<PaginationResponse<OrderResponse>>> getOrdersByUserWithPagination(
                        @PathVariable Integer userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                PaginationResponse<OrderResponse> orders = orderService.getOrdersByUserWithPagination(userId, page,
                                size);
                ApiResponse<PaginationResponse<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(),
                                "Thành công",
                                orders);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUser(@PathVariable Integer userId) {
                List<OrderResponse> orders = orderService.getOrdersByUser(userId);
                ApiResponse<List<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                                orders);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/status/{status}")
        public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
                List<OrderResponse> orders = orderService.getOrdersByStatus(status);
                ApiResponse<List<OrderResponse>> response = new ApiResponse<>(HttpStatus.OK.value(), "Thành công",
                                orders);
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
                                orderStatuses);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/order-types")
        public ResponseEntity<ApiResponse<List<EnumOptionResponse>>> getOrderTypes() {
                List<EnumOptionResponse> orderTypes = List.of(
                                new EnumOptionResponse("ONLINE", "Đơn hàng online"),
                                new EnumOptionResponse("COUNTER", "Đơn hàng tại quầy"));
                ApiResponse<List<EnumOptionResponse>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Lấy danh sách loại đơn hàng thành công",
                                orderTypes);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/dropdown")
        public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getDropdownOrders() {
                List<Order> orders = orderService.getAll();
                List<DropdownOptionResponse> dropdown = orders.stream()
                                .map(order -> new DropdownOptionResponse(order.getId(), order.getCode()))
                                .collect(Collectors.toList());
                ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(HttpStatus.OK.value(),
                                "Lấy danh sách đơn hàng thành công", dropdown);
                return ResponseEntity.ok(response);
        }

        /**
         * API tính toán tạm tiền đơn hàng cho admin
         * Trước khi tạo đơn thực tế, admin có thể xem trước số tiền cuối cùng
         */
        @PostMapping("/calculate")
        public ResponseEntity<ApiResponse<OrderCalculationResponse>> calculateOrderTotal(
                        @Valid @RequestBody OrderCalculationRequest request) {
                ApiResponse<OrderCalculationResponse> response = orderCalculationService.calculateOrderTotal(request);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        /**
         * API validate điều kiện tạo đơn
         */
        @PostMapping("/validate")
        public ResponseEntity<ApiResponse<String>> validateOrderConditions(
                        @Valid @RequestBody OrderCalculationRequest request) {
                ApiResponse<String> response = orderCalculationService.validateOrderConditions(request);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 404 ? HttpStatus.NOT_FOUND
                                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        /**
         * ✅ ENHANCED: API validate giá và số lượng sản phẩm (ưu tiên validate số lượng
         * flash sale)
         */
        @PostMapping("/validate-prices")
        public ResponseEntity<ApiResponse<String>> validateProductPricesAndQuantities(
                        @Valid @RequestBody List<PriceValidationRequest> priceValidationRequests,
                        @RequestParam Integer userId) {

                // ✅ SỬ DỤNG METHOD MỚI ĐỂ VALIDATE CẢ SỐ LƯỢNG VÀ GIÁ
                ApiResponse<String> response = priceValidationService
                                .validateProductPricesAndQuantities(priceValidationRequests, userId);
                HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK
                                : response.getStatus() == 400 ? HttpStatus.BAD_REQUEST
                                                : HttpStatus.INTERNAL_SERVER_ERROR;
                return ResponseEntity.status(status).body(response);
        }

        private String getOrderStatusDisplayName(OrderStatus orderStatus) {
                return OrderStatusUtil.getStatusDisplayName(orderStatus);
        }

        /**
         * ✅ THÊM MỚI: API cho khách hàng gửi yêu cầu hoàn trả
         */
        @PostMapping("/{orderId}/request-refund")
        public ResponseEntity<ApiResponse<OrderResponse>> requestRefund(
                        @PathVariable Integer orderId,
                        @Valid @RequestBody RefundRequestDto refundRequest) {
                ApiResponse<OrderResponse> response = orderService.requestRefund(orderId, refundRequest);
                return ResponseEntity.ok(response);
        }

        /**
         * ✅ THÊM MỚI: API cho admin chấp nhận yêu cầu hoàn trả
         */
        @PostMapping("/admin/approve-refund")
        public ResponseEntity<ApiResponse<OrderResponse>> approveRefundRequest(
                        @Valid @RequestBody AdminRefundDecisionDto decision) {
                ApiResponse<OrderResponse> response = orderService.approveRefundRequest(decision);
                return ResponseEntity.ok(response);
        }

        /**
         * ✅ THÊM MỚI: API cho admin từ chối yêu cầu hoàn trả
         */
        @PostMapping("/admin/reject-refund")
        public ResponseEntity<ApiResponse<OrderResponse>> rejectRefundRequest(
                        @Valid @RequestBody AdminRefundDecisionDto decision) {
                ApiResponse<OrderResponse> response = orderService.rejectRefundRequest(decision);
                return ResponseEntity.ok(response);
        }

        /**
         * ✅ API lấy chi tiết đơn hàng theo id (phục vụ hoàn hàng)
         */
        @GetMapping("/{id}/detail")
        public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable Integer id) {
                OrderResponse orderResponse = orderService.getOrderDetailById(id);
                if (orderResponse == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new ApiResponse<>(404, "Không tìm thấy đơn hàng với id: " + id, null));
                }
                return ResponseEntity.ok(new ApiResponse<>(200, "Thành công", orderResponse));
        }

}
