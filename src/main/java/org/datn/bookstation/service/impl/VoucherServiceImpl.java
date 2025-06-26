package org.datn.bookstation.service.impl;


import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Voucher;
import org.datn.bookstation.repository.VoucherRepository;
import org.datn.bookstation.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private VoucherRepository voucherRepository;


    @Override
    public PaginationResponse<VoucherRepuest> getAllWithPagination(
            int page, int size, String code, Byte status
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Voucher> spec = Specification.where(null);

        if (code != null && !code.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Voucher> voucherPage = voucherRepository.findAll(spec, pageable);

        List<VoucherRepuest> responses = voucherPage.getContent().stream().map(voucher -> {
            VoucherRepuest dto = new VoucherRepuest();
            dto.setId(voucher.getId());
            dto.setCode(voucher.getCode());
            dto.setDiscountPercentage(voucher.getDiscountPercentage());
            dto.setStartTime(voucher.getStartTime());
            dto.setEndTime(voucher.getEndTime());
            dto.setMinOrderValue(voucher.getMinOrderValue());
            dto.setMaxDiscountValue(voucher.getMaxDiscountValue());
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
        voucher.setCode(request.getCode());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setStartTime(request.getStartTime());
        voucher.setEndTime(request.getEndTime());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        voucher.setStatus(request.getStatus());
        voucher.setCreatedAt(Instant.now());
        voucher.setUpdatedAt(Instant.now());
        voucher.setCreatedBy(request.getCreatedBy());
        voucher.setUpdatedBy(request.getUpdatedBy());
        voucherRepository.save(voucher);
    }

    @Override
    public void editVoucher(VoucherRepuest request) {
        Voucher voucher = voucherRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setCode(request.getCode());
        voucher.setDiscountPercentage(request.getDiscountPercentage());
        voucher.setStartTime(request.getStartTime());
        voucher.setEndTime(request.getEndTime());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        voucher.setStatus(request.getStatus());
        voucher.setUpdatedAt(Instant.now());
        voucher.setUpdatedBy(request.getUpdatedBy());
        voucherRepository.save(voucher);
    }

    @Override
    public void updateStatus(Integer id, byte status, String updatedBy) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));
        voucher.setStatus(status);
        voucher.setUpdatedAt(Instant.now());
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
