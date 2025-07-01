package org.datn.bookstation.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EventGiftRequest {
    private Integer eventId;
    private String giftName;
    private String description;
    private BigDecimal giftValue;
    private Integer quantity;
    private String imageUrl;
    private String giftType; // BOOK, VOUCHER, POINT, PHYSICAL_ITEM
    private Integer bookId;
    private Integer voucherId;
    private Integer pointValue;
    private Boolean isActive;
}
