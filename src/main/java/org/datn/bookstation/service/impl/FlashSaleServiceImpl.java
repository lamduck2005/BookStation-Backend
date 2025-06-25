package org.datn.bookstation.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.datn.bookstation.entity.FlashSale;
import org.datn.bookstation.mapper.FlashSaleMapper;
import org.datn.bookstation.repository.FlashSaleRepository;
import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FlashSaleServiceImpl implements FlashSaleService {

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private FlashSaleMapper flashSaleMapper;

    @Override
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllFlashSaleWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FlashSale> flashSaleList = flashSaleRepository.findAll(pageable);
        List<FlashSaleResponse> flashSaleResponses = flashSaleList.getContent()
                .stream()
                .map(flashSaleMapper::toFlashSaleResponse)
                .collect(Collectors.toList());
        PaginationResponse<FlashSaleResponse> paginationResponse = new PaginationResponse<>(flashSaleResponses, page,
                size, flashSaleList.getTotalElements(), flashSaleList.getTotalPages());
        return new ApiResponse<>(200, "Lấy danh sách flash sale thành công", paginationResponse);
    }

    @Override
    public ApiResponse<FlashSaleResponse> createFlashSale(FlashSaleRequest request) {
     
            FlashSale flashSale = flashSaleMapper.toFlashSale(request);
            flashSale.setCreatedAt(System.currentTimeMillis());
            flashSale.setUpdatedAt(System.currentTimeMillis());
            // flashSale.setCreatedBy(1L);
            // flashSale.setUpdatedBy(1L);
            flashSaleRepository.save(flashSale);
            return new ApiResponse<>(200, "Tạo flash sale thành công", flashSaleMapper.toFlashSaleResponse(flashSale));
        
    }

    @Override
    public ApiResponse<FlashSaleResponse> updateFlashSale(FlashSaleRequest request, Integer id) {
        FlashSale flashSale = flashSaleRepository.findById(id).orElse(null);
        if (flashSale == null) {
            return new ApiResponse<>(404, "Flash sale không tồn tại", null);
        }
       
        flashSale.setName(request.getName());
        flashSale.setStartTime(request.getStartTime());
        flashSale.setEndTime(request.getEndTime());
        flashSale.setStatus(request.getStatus());
        flashSaleRepository.save(flashSale);
        return new ApiResponse<>(200, "Cập nhật flash sale thành công", flashSaleMapper.toFlashSaleResponse(flashSale));
    }
}
