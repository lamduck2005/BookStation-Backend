package org.datn.bookstation.controller;

import java.util.List;

import org.datn.bookstation.dto.request.UserVoucherRequest;
import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.dto.response.voucherUserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserVoucher;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.UserVoucherRepository;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {
    @Autowired
    private VoucherService voucherService;
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


}
