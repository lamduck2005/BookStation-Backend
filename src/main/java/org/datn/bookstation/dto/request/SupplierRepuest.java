package org.datn.bookstation.dto.request;



import lombok.Data;

import java.time.Instant;
@Data

public class SupplierRepuest {
        private Integer id;
        private String supplierName;
        private String contactName;
        private String phoneNumber;
        private String email;
        private String address;
        private Byte status;
        private Instant createdAt;
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;


}
