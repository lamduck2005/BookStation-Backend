package org.datn.bookstation.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BookResponse {
    private Integer id;
    private String book_code;
    private String book_name;
    private List<String> author_name;
    private BigDecimal price;
    private Integer stock_quantity;
    private String category_name;
    private String supplier_name;
    private String flash_sale_name;
    private String publication_date;
    private String created_at;
    private String updated_at;
}
