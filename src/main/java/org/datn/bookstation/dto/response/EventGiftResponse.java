package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventGiftResponse {
    private Integer id;
    private Integer eventId;
    private String eventName;
    private String giftName;
    private String description;
    private BigDecimal giftValue;
    private Integer quantity;
    private Integer remainingQuantity; 
    private String imageUrl;
    private String giftType;
    private Integer bookId;
    private String bookTitle;
    private Integer voucherId;
    private String voucherCode;
    private Integer pointValue;
    private Boolean isActive;
    private Long createdAt;
}
