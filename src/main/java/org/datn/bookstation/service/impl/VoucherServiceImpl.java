package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public PaginationResponse<VoucherResponse> getAllWithPagination(
            int page, int size, String code, String name, String voucherType, Byte status
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Voucher> spec = Specification.where(null);

        if (code != null && !code.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (voucherType != null && !voucherType.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("voucherType")), voucherType.toLowerCase()));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Voucher> voucherPage = voucherRepository.findAll(spec, pageable);

        List<VoucherResponse> responses = voucherPage.getContent().stream().map(voucher -> {
            VoucherResponse dto = new VoucherResponse();
            dto.setId(voucher.getId());
            dto.setCode(voucher.getCode());
            dto.setName(voucher.getName());
            dto.setDescription(voucher.getDescription());
            dto.setVoucherType(voucher.getVoucherType());
            dto.setDiscountPercentage(voucher.getDiscountPercentage());
            dto.setDiscountAmount(voucher.getDiscountAmount());
            dto.setStartTime(voucher.getStartTime());
            dto.setEndTime(voucher.getEndTime());
            dto.setMinOrderValue(voucher.getMinOrderValue());
            dto.setMaxDiscountValue(voucher.getMaxDiscountValue());
            dto.setUsageLimit(voucher.getUsageLimit());
            dto.setUsedCount(voucher.getUsedCount());
            dto.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
            dto.setStatus(voucher.getStatus());
            dto.setCreatedAt(voucher.getCreatedAt());
            dto.setUpdatedAt(voucher.getUpdatedAt());
            dto.setCreatedBy(voucher.getCreatedBy());
            dto.setUpdatedBy(voucher.getUpdatedBy());
            return dto;
        }).collect(Collectors.toList());

        return new PaginationResponse<>(
                responses,
                voucherPage.getNumber(),
                voucherPage.getSize(),
                voucherPage.getTotalElements(),
                voucherPage.getTotalPages()
        );
    }

    @Override
    public void addVoucher(VoucherRepuest request) {
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode()); // Mã voucher
        voucher.setName(request.getName()); // Tên voucher
        voucher.setDescription(request.getDescription()); // Mô tả voucher
        voucher.setVoucherType(request.getVoucherType()); // Loại voucher (PERCENTAGE, FIXED_AMOUNT, ...)
        voucher.setDiscountPercentage(request.getDiscountPercentage()); // Phần trăm giảm giá (nếu là loại phần trăm)
        voucher.setDiscountAmount(request.getDiscountAmount()); // Số tiền giảm giá cố định (nếu là loại cố định)
        voucher.setStartTime(request.getStartTime()); // Thời gian bắt đầu hiệu lực (epoch millis)
        voucher.setEndTime(request.getEndTime()); // Thời gian kết thúc hiệu lực (epoch millis)
        voucher.setMinOrderValue(request.getMinOrderValue()); // Giá trị đơn hàng tối thiểu để áp dụng voucher
       voucher.setMaxDiscountValue(
    request.getMaxDiscountValue() != null ? request.getMaxDiscountValue() : BigDecimal.ZERO
);// Giá trị giảm giá tối đa (nếu có)
        voucher.setUsageLimit(request.getUsageLimit()); // Số lần voucher có thể sử dụng tổng cộng
        voucher.setUsedCount(0); // Số lần voucher đã được sử dụng
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser()); // Số lần tối đa một user có thể sử dụng voucher này
        voucher.setStatus((byte )0); // Trạng thái voucher (0: không hoạt động, 1: hoạt động, ...)
        voucher.setCreatedBy(request.getCreatedBy()); // Người tạo voucher
        voucher.setUpdatedBy(request.getUpdatedBy()); // Người cập nhật cuối cùng
        // createdAt và updatedAt sẽ tự động set ở @PrePersist
        voucherRepository.save(voucher);
    }

    @Override
    public void editVoucher(VoucherRepuest request) {
        Voucher voucher = voucherRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setCode(request.getCode());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setVoucherType(request.getVoucherType());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setDiscountAmount(request.getDiscountAmount());
        voucher.setStartTime(request.getStartTime());
        voucher.setEndTime(request.getEndTime());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setUsedCount(request.getUsedCount());
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser());
        voucher.setStatus(request.getStatus());
        voucher.setUpdatedBy(request.getUpdatedBy());
        // updatedAt sẽ tự động set ở @PreUpdate
        voucherRepository.save(voucher);
    }

    @Override
    public void updateStatus(Integer id, byte status, String updatedBy) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setStatus(status);
        voucher.setUpdatedBy(updatedBy);
        voucherRepository.save(voucher);
    }

    @Override
    public void deleteVoucher(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucherRepository.delete(voucher);
    }
}
