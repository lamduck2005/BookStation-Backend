package org.datn.bookstation.controller;

import java.util.List;

import org.datn.bookstation.dto.request.UserVoucherRequest;
import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.dto.response.voucherUserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserVoucher;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.UserVoucherRepository;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherService;
import org.datn.bookstation.service.VoucherCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VoucherCalculationService voucherCalculationService;
    @Autowired
    private UserVoucherRepository userVoucherRepository;
    @Autowired
    private VoucherRepository voucherRepository; // Thêm dòng này nếu chưa có

    @GetMapping
    public PaginationResponse<VoucherResponse> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) Byte status
    ) {
        return voucherService.getAllWithPagination(page, size, code, name, voucherType, status);
    }


    @GetMapping("/userVoucher/{userId}")
    public List<org.datn.bookstation.dto.response.voucherUserResponse> getVoucherById(@PathVariable Integer userId) {
        List<org.datn.bookstation.dto.response.voucherUserResponse> vouchers = userVoucherRepository.findVouchersByUserId(userId);
        if (vouchers == null || vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher cho người dùng với ID: " + userId);
        }
        return vouchers;
    }
    @GetMapping("/new")
    public voucherUserResponse getVoucherByuserId() {
        String code = "WELCOMETOSHOP";
        List<voucherUserResponse> vouchers = userVoucherRepository.findVouchersByVoucherId(code);
        if (vouchers == null || vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher với mã: " + code);
        }
        return vouchers.get(0); // Lấy voucher đầu tiên trong danh sách
    }
    @GetMapping("/new/{userId}")
    public List<voucherUserResponse> getVoucherByUserIdNew(@PathVariable Integer userId) {
        String code = "WELCOMETOSHOP";
        List<voucherUserResponse> vouchers = userVoucherRepository.findVouchersByVoucherUserId(code , userId);
        if (vouchers == null || vouchers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy voucher với mã: " + code);
        }
        return vouchers;
    }

    @PostMapping("/NewVoucher")
    public void addVoucherForUser(@RequestBody UserVoucherRequest request) {
        UserVoucher userVoucher = new UserVoucher();

        // Tạo đối tượng User và Voucher chỉ với id
        User user = new User();
        user.setId(request.getUserId());
        userVoucher.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setId(request.getVoucherId());
        userVoucher.setVoucher(voucher);

        userVoucher.setUsedCount(0); // hoặc để mặc định cũng được

        userVoucherRepository.save(userVoucher);
    }
    


    @PostMapping
    public void addVoucher(@RequestBody VoucherRepuest request) {
        voucherService.addVoucher(request);
    }

    @PutMapping
    public void editVoucher(@RequestBody VoucherRepuest request) {
        voucherService.editVoucher(request);
    }

    @PatchMapping("/status")
    public void updateStatus(
            @RequestParam Integer id,
            @RequestParam byte status,
            @RequestParam String updatedBy
    ) {
        voucherService.updateStatus(id, status, updatedBy);
    }

    @DeleteMapping("/{id}")
    public void deleteVoucher(@PathVariable Integer id) {
        voucherService.deleteVoucher(id);
    }

    /**
     * API lấy danh sách voucher có thể sử dụng cho user (dành cho admin tạo đơn thủ công)
     */
    @GetMapping("/user/{userId}/available")
    public ResponseEntity<ApiResponse<List<DropdownOptionResponse>>> getAvailableVouchersForUser(@PathVariable Integer userId) {
        try {
            // Lấy tất cả voucher đang hoạt động
            PaginationResponse<VoucherResponse> allVouchers = voucherService.getAllWithPagination(0, 1000, null, null, null, (byte) 1);

            long currentTime = System.currentTimeMillis();
            List<DropdownOptionResponse> availableVouchers = allVouchers.getContent().stream()
                    .filter(voucher -> {
                        // Kiểm tra thời gian hiệu lực
                        boolean isTimeValid = voucher.getStartTime() <= currentTime && currentTime <= voucher.getEndTime();
                        
                        // Kiểm tra user có thể sử dụng voucher này không
                        boolean canUserUse = voucherCalculationService.canUserUseVoucher(userId, voucher.getId());
                        
                        // Kiểm tra voucher còn lượt sử dụng không
                        boolean hasUsageLimit = voucher.getUsageLimit() == null || voucher.getUsedCount() < voucher.getUsageLimit();
                        
                        return isTimeValid && canUserUse && hasUsageLimit;
                    })
                    .map(voucher -> new DropdownOptionResponse(
                            voucher.getId(),
                            voucher.getCode() + " - " + voucher.getName() +
                                    (voucher.getDiscountPercentage() != null ?
                                            " (-" + voucher.getDiscountPercentage() + "%)" :
                                            " (-" + voucher.getDiscountAmount() + "đ)")
                    ))
                    .collect(Collectors.toList());

            ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách voucher có thể sử dụng thành công",
                    availableVouchers
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<DropdownOptionResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy danh sách voucher: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
