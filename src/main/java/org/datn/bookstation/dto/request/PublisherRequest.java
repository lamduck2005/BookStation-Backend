package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublisherRequest {
    
    private Integer id; // For update operations
    
    @NotNull(message = "Tên nhà xuất bản không được để trống")
    @Size(max = 255, message = "Tên nhà xuất bản không được vượt quá 255 ký tự")
    private String publisherName;
    
    @Size(max = 2000, message = "Địa chỉ không được vượt quá 2000 ký tự")
    private String address;
    
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phoneNumber;
    
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;
    
    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;
    
    private Integer establishedYear;
    
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;
    
    private Byte status;
    
    private Integer createdBy;
    
    private Integer updatedBy;
}
