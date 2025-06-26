package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;

public interface VoucherService {
    PaginationResponse<VoucherRepuest> getAllWithPagination(
            int page, int size, String code, Byte status
    );

    void addVoucher(VoucherRepuest request);

    void editVoucher(VoucherRepuest request);

    void updateStatus(Integer id, byte status, String updatedBy);

    void deleteVoucher(Integer id);
}
