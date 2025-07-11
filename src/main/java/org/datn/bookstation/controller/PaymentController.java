package org.datn.bookstation.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.service.VnPayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;
    private final OrderRepository orderRepository;

    /**
     * Tạo URL thanh toán VNPAY cho đơn hàng.
     * Frontend truyền orderId, backend trả về URL để redirect.
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(@RequestParam Integer orderId, HttpServletRequest request) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Không tìm thấy đơn hàng", null));
        }
        Order order = optionalOrder.get();
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, "Chỉ tạo thanh toán cho đơn hàng PENDING", null));
        }
        String clientIp = request.getRemoteAddr();
        String payUrl = vnPayService.generatePaymentUrl(order, clientIp);
        return ResponseEntity.ok(new ApiResponse<>(200, "Tạo URL thành công", payUrl));
    }

    /**
     * Tạo URL thanh toán thủ công (không cần Order).
     * Ví dụ:
     * POST /api/payment/vnpay/manual?amount=100000&orderCode=TEST123&orderInfo=Test
     */
    @PostMapping("/vnpay/manual")
    public ResponseEntity<ApiResponse<String>> createManualPayment(
            @RequestParam BigDecimal amount,
            @RequestParam String orderCode,
            @RequestParam(required = false) String orderInfo,
            HttpServletRequest request) {
        String payUrl = vnPayService.generatePaymentUrl(amount, orderCode, orderInfo, request.getRemoteAddr());
        return ResponseEntity.ok(new ApiResponse<>(200, "Tạo URL thành công", payUrl));
    }

    /**
     * Endpoint VNPAY redirect người dùng về (GET)
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> allParams) {
        String receivedHash = allParams.get("vnp_SecureHash");
        boolean valid = vnPayService.validateChecksum(allParams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), receivedHash);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Checksum không hợp lệ");
        }

        return ResponseEntity.ok("Thanh toán thành công, thông tin thanh toán: " + allParams.toString());

        // // Lấy thông tin cần thiết
        // String responseCode = allParams.get("vnp_ResponseCode");
        // String orderCode = allParams.get("vnp_TxnRef");
        // String amountStr = allParams.get("vnp_Amount");

        // if (orderCode == null) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Thiếu mã đơn hàng");
        // }

        // // Tìm đơn hàng
        // Optional<Integer> orderIdOpt = orderRepository.findIdByCode(orderCode);
        // if (orderIdOpt.isEmpty()) {
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
        // }
        // Order order = orderRepository.findById(orderIdOpt.get()).orElse(null);
        // if (order == null) {
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn hàng");
        // }

        // // Xác thực số tiền
        // long paidAmount = Long.parseLong(amountStr) / 100; // chia 100 về VND thực
        // BigDecimal paidBig = BigDecimal.valueOf(paidAmount);
        // if (order.getTotalAmount().compareTo(paidBig) != 0) {
        //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Số tiền không khớp");
        // }

        // if ("00".equals(responseCode)) {
        //     order.setOrderStatus(OrderStatus.CONFIRMED);
        //     orderRepository.save(order);
        //     return ResponseEntity.ok("Thanh toán thành công cho đơn hàng " + order.getCode());
        // } else {
        //     return ResponseEntity.ok("Thanh toán thất bại. Mã lỗi: " + responseCode);
        // }
    }

    /**
     * VNPAY gọi server-to-server để thông báo kết quả (IPN)
     */
    @PostMapping("/vnpay-ipn")
    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> allParams) {
        String receivedHash = allParams.get("vnp_SecureHash");
        boolean valid = vnPayService.validateChecksum(allParams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), receivedHash);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Checksum không hợp lệ");
        }

        String responseCode = allParams.get("vnp_ResponseCode");
        String orderCode = allParams.get("vnp_TxnRef");

        Optional<Integer> orderIdOpt = orderRepository.findIdByCode(orderCode);
        if (orderIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }
        Order order = orderRepository.findById(orderIdOpt.get()).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        }

        if ("00".equals(responseCode)) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        } else if ("24".equals(responseCode)) {
            order.setOrderStatus(OrderStatus.CANCELED);
        } else {
            // Các mã khác đánh dấu thất bại, có thể custom
            order.setOrderStatus(OrderStatus.CANCELED);
        }
        orderRepository.save(order);
        return ResponseEntity.ok("OK");
    }
} 