package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import java.util.List;

public interface VoucherService {
    java.util.List<org.datn.bookstation.dto.response.AvailableVoucherResponse> getAvailableVouchersForUser(Integer userId);
    PaginationResponse<VoucherResponse> getAllWithPagination(
            int page, int size, String code, String name, String voucherType, Byte status
    );

    void addVoucher(VoucherRepuest request);

    void editVoucher(VoucherRepuest request);

    void updateStatus(Integer id, byte status, String updatedBy);

    void deleteVoucher(Integer id);

    /**
     * ✅ Search voucher cho counter sales
     * Tìm theo mã voucher hoặc tên voucher
     */
    List<VoucherResponse> searchVouchersForCounterSales(String query, int limit);
}
