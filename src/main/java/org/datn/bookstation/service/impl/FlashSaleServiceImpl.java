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
import org.datn.bookstation.specification.FlashSaleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
                .map(flashSaleMapper::toResponse)
                .collect(Collectors.toList());
        PaginationResponse<FlashSaleResponse> paginationResponse = new PaginationResponse<>(flashSaleResponses, page,
                size, flashSaleList.getTotalElements(), flashSaleList.getTotalPages());
        return new ApiResponse<>(200, "Lấy danh sách flash sale thành công", paginationResponse);
    }

    @Override
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllWithFilter(int page, int size, String name, Long from, Long to, Byte status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<FlashSale> specification = FlashSaleSpecification.filterBy(name, from, to, status);
        Page<FlashSale> flashSalePage = flashSaleRepository.findAll(specification, pageable);

        List<FlashSaleResponse> responses = flashSalePage.getContent()
            .stream()
            .map(flashSaleMapper::toResponse)
            .collect(Collectors.toList());

        PaginationResponse<FlashSaleResponse> pagination = PaginationResponse.<FlashSaleResponse>builder()
            .content(responses)
            .pageNumber(flashSalePage.getNumber())
            .pageSize(flashSalePage.getSize())
            .totalElements(flashSalePage.getTotalElements())
            .totalPages(flashSalePage.getTotalPages())
            .build();

        return new ApiResponse<>(200, "Lấy danh sách flash sale thành công", pagination);
    }

    @Override
    public ApiResponse<FlashSaleResponse> createFlashSale(FlashSaleRequest request) {
     
            FlashSale flashSale = flashSaleMapper.toFlashSale(request);
            flashSale.setCreatedAt(System.currentTimeMillis());
            flashSale.setUpdatedAt(System.currentTimeMillis());
            // flashSale.setCreatedBy(1L);
            // flashSale.setUpdatedBy(1L);
            flashSaleRepository.save(flashSale);
            return new ApiResponse<>(200, "Tạo flash sale thành công", flashSaleMapper.toResponse(flashSale));
        
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
        return new ApiResponse<>(200, "Cập nhật flash sale thành công", flashSaleMapper.toResponse(flashSale));
    }

    @Override
    public ApiResponse<FlashSaleResponse> toggleStatus(Integer id) {
        FlashSale flashSale = flashSaleRepository.findById(id).orElse(null);

        if (flashSale == null) {
            return new ApiResponse<>(404, "Flash sale không tồn tại", null);
        }
        
        flashSale.setStatus((byte) (flashSale.getStatus() == 1 ? 0 : 1));
        flashSaleRepository.save(flashSale);
        return new ApiResponse<>(200, "Cập nhật trạng thái flash sale thành công", flashSaleMapper.toResponse(flashSale));
    }
}
