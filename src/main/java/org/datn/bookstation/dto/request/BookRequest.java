package org.datn.bookstation.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BookRequest {
    private String book_code;
    private String book_name;
    private List<String> author_name;
    private BigDecimal price;
    private Integer stock_quantity;
    private String category_name;
    private String supplier_name;
    private String flash_sale_name;
    private String publication_date;
}
