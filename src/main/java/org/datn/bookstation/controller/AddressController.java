package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.AddressRequest;
import org.datn.bookstation.dto.response.AddressResponse;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.DropdownOptionResponse;
import org.datn.bookstation.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
@AllArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // Lấy toàn bộ địa chỉ của 1 user
    @GetMapping
    public ApiResponse<List<AddressResponse>> getAddressesByUser(@RequestParam Integer userId) {
        return addressService.getAddressesByUser(userId);
    }

    // Lấy địa chỉ theo id
    @GetMapping("/{id}")
    public ApiResponse<AddressResponse> getById(@PathVariable Integer id) {
        return addressService.getById(id);
    }

    // Tạo địa chỉ mới
    @PostMapping
    public ApiResponse<AddressResponse> create(@RequestBody AddressRequest request) {
        return addressService.create(request);
    }

    // Sửa địa chỉ theo id
    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> update(@RequestBody AddressRequest request, @PathVariable Integer id) {
        return addressService.update(request, id);
    }

    // Xóa địa chỉ theo id
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        return addressService.delete(id);
    }

    /**
     * API lấy danh sách địa chỉ dropdown cho user (dành cho admin tạo đơn thủ công)
     */
    @GetMapping("/user/{userId}/dropdown")
    public ApiResponse<List<DropdownOptionResponse>> getAddressDropdownForUser(@PathVariable Integer userId) {
        try {
            ApiResponse<List<AddressResponse>> response = addressService.getAddressesByUser(userId);

            if (response.getStatus() == 200 && response.getData() != null) {
                List<DropdownOptionResponse> dropdown = response.getData().stream()
                        .map(address -> new DropdownOptionResponse(
                                address.getId(),
                                address.getRecipientName() + " - " +
                                        address.getAddressDetail() + ", " +
                                        address.getWardName() + ", " +
                                        address.getDistrictName() + ", " +
                                        address.getProvinceName() +
                                        (address.getIsDefault() ? " (Mặc định)" : "")
                        ))
                        .collect(Collectors.toList());

                return new ApiResponse<>(200, "Lấy danh sách địa chỉ thành công", dropdown);
            } else {
                return new ApiResponse<>(response.getStatus(), response.getMessage(), null);
            }
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi lấy danh sách địa chỉ: " + e.getMessage(), null);
        }
    }
}