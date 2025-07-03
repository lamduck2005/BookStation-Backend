package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;

public interface VoucherService {
    PaginationResponse<VoucherResponse> getAllWithPagination(
            int page, int size, String code, String name, String voucherType, Byte status
    );

    void addVoucher(VoucherRepuest request);

    void editVoucher(VoucherRepuest request);

    void updateStatus(Integer id, byte status, String updatedBy);

    void deleteVoucher(Integer id);
}
