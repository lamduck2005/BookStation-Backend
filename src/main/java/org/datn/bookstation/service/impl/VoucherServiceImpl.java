package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.dto.response.VoucherStatsResponse;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private org.datn.bookstation.service.VoucherCalculationService voucherCalculationService;

    @Override
    public java.util.List<org.datn.bookstation.dto.response.AvailableVoucherResponse> getAvailableVouchersForUser(
            Integer userId) {
        PaginationResponse<VoucherResponse> allVouchers = getAllWithPagination(0, 1000, null, null, null, (byte) 1);
        long currentTime = System.currentTimeMillis();
        return allVouchers.getContent().stream()
                .filter(voucher -> {
                    boolean isTimeValid = voucher.getStartTime() <= currentTime && currentTime <= voucher.getEndTime();
                    boolean canUserUse = voucherCalculationService.canUserUseVoucher(userId, voucher.getId());
                    boolean hasUsageLimit = voucher.getUsageLimit() == null
                            || voucher.getUsedCount() < voucher.getUsageLimit();
                    return isTimeValid && canUserUse && hasUsageLimit;
                })
                .map(voucher -> {
                    org.datn.bookstation.dto.response.AvailableVoucherResponse dto = new org.datn.bookstation.dto.response.AvailableVoucherResponse();
                    dto.setId(voucher.getId());
                    dto.setCode(voucher.getCode());
                    dto.setName(voucher.getName());
                    dto.setDescription(voucher.getDescription());
                    dto.setCategoryVi(voucher.getVoucherCategory() == null ? ""
                            : (voucher.getVoucherCategory().name().equals("NORMAL") ? "Giảm giá sản phẩm"
                                    : "Giảm giá vận chuyển"));
                    dto.setDiscountTypeVi(voucher.getDiscountType() == null ? ""
                            : (voucher.getDiscountType().name().equals("PERCENTAGE") ? "Giảm theo phần trăm"
                                    : "Giảm số tiền cố định"));
                    dto.setDiscountValue(voucher.getDiscountPercentage() != null ? voucher.getDiscountPercentage()
                            : voucher.getDiscountAmount());
                    dto.setMinOrderValue(voucher.getMinOrderValue());
                    dto.setMaxDiscountValue(voucher.getMaxDiscountValue());
                    dto.setStartTime(voucher.getStartTime());
                    dto.setEndTime(voucher.getEndTime());
                    dto.setUsageLimit(voucher.getUsageLimit());
                    dto.setUsedCount(voucher.getUsedCount());
                    dto.setUsageLimitPerUser(voucher.getUsageLimitPerUser());
                    dto.setRemainingUses(voucher.getUsageLimit() == null ? null
                            : voucher.getUsageLimit() - (voucher.getUsedCount() == null ? 0 : voucher.getUsedCount()));
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm");
                    dto.setExpireDate(
                            voucher.getEndTime() != null
                                    ? java.time.LocalDateTime
                                            .ofInstant(java.time.Instant.ofEpochMilli(voucher.getEndTime()),
                                                    java.time.ZoneId.systemDefault())
                                            .format(formatter)
                                    : "");
                    dto.setDiscountInfo((voucher.getDiscountPercentage() != null
                            ? ("Giảm " + voucher.getDiscountPercentage().stripTrailingZeros().toPlainString() + "%")
                            : ("Giảm " + formatVnMoney(voucher.getDiscountAmount()) + "đ")) + " cho đơn từ "
                            + formatVnMoney(voucher.getMinOrderValue()) + "đ");
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private static String formatVnMoney(java.math.BigDecimal money) {
        if (money == null)
            return "0";
        java.text.DecimalFormat df = new java.text.DecimalFormat("###,###");
        return df.format(money.setScale(0, java.math.RoundingMode.DOWN));
    }

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public PaginationResponse<VoucherResponse> getAllWithPagination(
            int page, int size, String code, String name, String voucherType, Byte status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Voucher> spec = Specification.where(null);

        if (code != null && !code.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (voucherType != null && !voucherType.isEmpty()) {
            spec = spec
                    .and((root, query, cb) -> cb.equal(cb.lower(root.get("voucherType")), voucherType.toLowerCase()));
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
            dto.setVoucherCategory(voucher.getVoucherCategory());
            dto.setDiscountType(voucher.getDiscountType());
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
                voucherPage.getTotalPages());
    }

    @Override
    public void addVoucher(VoucherRepuest request) {
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode()); // Mã voucher
        voucher.setName(request.getName()); // Tên voucher
        voucher.setDescription(request.getDescription()); // Mô tả voucher
        voucher.setVoucherCategory(request.getVoucherCategory()); // Category voucher (NORMAL, SHIPPING)
        voucher.setDiscountType(request.getDiscountType()); // Discount type (PERCENTAGE, FIXED_AMOUNT)
        voucher.setDiscountPercentage(request.getDiscountPercentage()); // Phần trăm giảm giá (nếu là loại phần trăm)
        voucher.setDiscountAmount(request.getDiscountAmount()); // Số tiền giảm giá cố định (nếu là loại cố định)
        voucher.setStartTime(request.getStartTime()); // Thời gian bắt đầu hiệu lực (epoch millis)
        voucher.setEndTime(request.getEndTime()); // Thời gian kết thúc hiệu lực (epoch millis)
        voucher.setMinOrderValue(request.getMinOrderValue()); // Giá trị đơn hàng tối thiểu để áp dụng voucher
        voucher.setMaxDiscountValue(
                request.getMaxDiscountValue() != null ? request.getMaxDiscountValue() : BigDecimal.ZERO);// Giá trị giảm
                                                                                                         // giá tối đa
                                                                                                         // (nếu có)
        voucher.setUsageLimit(request.getUsageLimit()); // Số lần voucher có thể sử dụng tổng cộng
        voucher.setUsedCount(0); // Số lần voucher đã được sử dụng
        voucher.setUsageLimitPerUser(request.getUsageLimitPerUser()); // Số lần tối đa một user có thể sử dụng voucher
                                                                      // này
        voucher.setStatus((byte) 1); // Trạng thái voucher (0: không hoạt động, 1: hoạt động, ...)
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
        voucher.setVoucherCategory(request.getVoucherCategory());
        voucher.setDiscountType(request.getDiscountType());
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

    @Override
    public List<VoucherResponse> searchVouchersForCounterSales(String query, int limit) {
        Specification<Voucher> spec = (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Active vouchers only
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), (byte) 1));

        // ✅ NEW: Exclude shipping vouchers for counter sales
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("voucherCategory"),
                org.datn.bookstation.entity.enums.VoucherCategory.SHIPPING));

        // Search by code or name
        if (query != null && !query.trim().isEmpty()) {
            String searchQuery = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchQuery),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchQuery)));
        }

        // Current time validation
        long currentTime = System.currentTimeMillis();
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), currentTime),
                criteriaBuilder.greaterThanOrEqualTo(root.get("endTime"), currentTime)));

        // Has usage limit
        spec = spec.and((root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.isNull(root.get("usageLimit")),
                criteriaBuilder.lessThan(root.get("usedCount"), root.get("usageLimit"))));

        Pageable pageable = PageRequest.of(0, limit);
        Page<Voucher> vouchers = voucherRepository.findAll(spec, pageable);

        return vouchers.getContent().stream()
                .map(this::toVoucherResponse)
                .collect(Collectors.toList());
    }

    private VoucherResponse toVoucherResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setName(voucher.getName());
        response.setDescription(voucher.getDescription());
        response.setVoucherCategory(voucher.getVoucherCategory());
        response.setDiscountType(voucher.getDiscountType());
        response.setDiscountPercentage(voucher.getDiscountPercentage());
        response.setDiscountAmount(voucher.getDiscountAmount());
        response.setMinOrderValue(voucher.getMinOrderValue());
        response.setMaxDiscountValue(voucher.getMaxDiscountValue());
        response.setStartTime(voucher.getStartTime());
        response.setEndTime(voucher.getEndTime());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsedCount(voucher.getUsedCount());
        response.setStatus(voucher.getStatus());
        return response;
    }

    @Override
    public VoucherStatsResponse getVoucherStats() {
        Long currentTime = System.currentTimeMillis();

        // Tổng số voucher
        Long totalVouchers = voucherRepository.countTotalVouchers();

        // ✅ SỬA: Voucher chưa được sử dụng thay vì voucher đang hoạt động
        Long unusedVouchers = voucherRepository.countUnusedVouchers();

        // Tổng lượt sử dụng (giữ nguyên)
        Long totalUsageCount = voucherRepository.sumTotalUsageCount();

        // Voucher phổ biến nhất
        List<String> mostPopularCodes = voucherRepository.findMostPopularVoucherCode();
        String mostPopularVoucher = mostPopularCodes.isEmpty() ? "N/A" : mostPopularCodes.get(0);

        return new VoucherStatsResponse(
                totalVouchers != null ? totalVouchers : 0L,
                unusedVouchers != null ? unusedVouchers : 0L,  // ← SỬA: dùng unusedVouchers
                totalUsageCount != null ? totalUsageCount : 0L,
                mostPopularVoucher);
    }
}
