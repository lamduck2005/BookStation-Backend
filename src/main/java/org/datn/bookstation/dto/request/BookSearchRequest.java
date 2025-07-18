package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookSearchRequest {
    private Integer bookId;
    private String bookName;
    private BigDecimal price;
    private String coverImageUrl;

}
