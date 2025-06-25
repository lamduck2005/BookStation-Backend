package org.datn.bookstation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/flash-sales")  
public class FlashSaleController {
    @Autowired
    private FlashSaleService flashSaleService;

    @GetMapping
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllFlashSaleWithPagination(@RequestParam int page, @RequestParam int size) {
        return flashSaleService.getAllFlashSaleWithPagination(page, size);
    }

    @PostMapping
    public ApiResponse<FlashSaleResponse> createFlashSale(@RequestBody FlashSaleRequest request) {
        return flashSaleService.createFlashSale(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<FlashSaleResponse> updateFlashSale(@RequestBody FlashSaleRequest request, @PathVariable Integer id) {
        return flashSaleService.updateFlashSale(request, id);
    }
    
}
