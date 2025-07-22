package org.datn.bookstation.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.CheckoutSessionRequest;
import org.datn.bookstation.dto.response.CheckoutSessionResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.repository.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutSessionResponseMapper {

    private final CheckoutSessionMapper checkoutSessionMapper;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final VoucherRepository voucherRepository;

    public CheckoutSessionResponse toResponse(CheckoutSession session) {
        CheckoutSessionResponse response = new CheckoutSessionResponse();
        
        response.setId(session.getId());
        response.setUserId(session.getUser().getId());
        
        // User info
        if (session.getUser() != null) {
            response.setUserFullName(session.getUser().getFullName());
            response.setUserEmail(session.getUser().getEmail());
        }

        // Address info
        if (session.getAddress() != null) {
            response.setAddressId(session.getAddress().getId());
            response.setAddressFullText(buildFullAddress(session.getAddress()));
            response.setRecipientName(session.getAddress().getRecipientName());
            response.setRecipientPhone(session.getAddress().getPhoneNumber());
        }

        // Shipping & payment
        response.setShippingMethod(session.getShippingMethod());
        response.setShippingFee(session.getShippingFee());
        response.setEstimatedDeliveryFrom(session.getEstimatedDeliveryFrom());
        response.setEstimatedDeliveryTo(session.getEstimatedDeliveryTo());
        response.setEstimatedDeliveryText(buildDeliveryTimeText(session.getEstimatedDeliveryFrom(), session.getEstimatedDeliveryTo()));
        response.setPaymentMethod(session.getPaymentMethod());

        // Vouchers
        List<Integer> voucherIds = checkoutSessionMapper.parseVoucherIds(session.getSelectedVoucherIds());
        response.setSelectedVoucherIds(voucherIds);
        response.setSelectedVouchers(buildVoucherSummaries(voucherIds));

        // Checkout items - Force cast to resolve compilation issue
        @SuppressWarnings("unchecked")
        List<CheckoutSessionRequest.BookQuantity> items = checkoutSessionMapper.parseCheckoutItems(session.getCheckoutItems());
        response.setCheckoutItems(buildCheckoutItemResponses(items));

        // Financial info
        response.setSubtotal(session.getSubtotal());
        response.setTotalDiscount(session.getTotalDiscount());
        response.setTotalAmount(session.getTotalAmount());

        // Session status
        response.setStatus(session.getStatus());
        response.setExpiresAt(session.getExpiresAt());
        response.setIsExpired(session.isExpired());

        // Audit info
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        response.setNotes(session.getNotes());

        return response;
    }

    private String buildFullAddress(Address address) {
        if (address == null) return null;
        
        StringBuilder sb = new StringBuilder();
        if (address.getAddressDetail() != null) sb.append(address.getAddressDetail());
        
        return sb.toString();
    }

    private String buildDeliveryTimeText(Long from, Long to) {
        if (from == null && to == null) return null;
        
        if (from != null && to != null) {
            return String.format("Từ %s đến %s", 
                java.time.Instant.ofEpochMilli(from).toString(),
                java.time.Instant.ofEpochMilli(to).toString());
        } else if (from != null) {
            return "Từ " + java.time.Instant.ofEpochMilli(from).toString();
        } else if (to != null) {
            return "Trước " + java.time.Instant.ofEpochMilli(to).toString();
        }
        return null;
    }

    private List<CheckoutSessionResponse.VoucherSummary> buildVoucherSummaries(List<Integer> voucherIds) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<CheckoutSessionResponse.VoucherSummary> summaries = new ArrayList<>();
        
        for (Integer voucherId : voucherIds) {
            Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
            if (voucherOpt.isPresent()) {
                Voucher voucher = voucherOpt.get();
                CheckoutSessionResponse.VoucherSummary summary = new CheckoutSessionResponse.VoucherSummary();
                
                summary.setId(voucher.getId());
                summary.setCode(voucher.getCode());
                summary.setName(voucher.getName());
                // ✅ FIX: Sử dụng VoucherCategory mới thay vì VoucherType cũ
                summary.setVoucherType(voucher.getVoucherCategory().toString());
                
                // Calculate discount value
                BigDecimal discountValue = BigDecimal.ZERO;
                if (voucher.getDiscountPercentage() != null) {
                    discountValue = voucher.getDiscountPercentage();
                } else if (voucher.getDiscountAmount() != null) {
                    discountValue = voucher.getDiscountAmount();
                }
                summary.setDiscountValue(discountValue);
                
                // Check validity
                long currentTime = System.currentTimeMillis();
                boolean isValid = voucher.getStatus() == 1 && 
                                currentTime >= voucher.getStartTime() && 
                                currentTime <= voucher.getEndTime();
                summary.setIsValid(isValid);
                
                if (!isValid) {
                    if (voucher.getStatus() != 1) {
                        summary.setInvalidReason("Voucher đã bị vô hiệu hóa");
                    } else if (currentTime < voucher.getStartTime()) {
                        summary.setInvalidReason("Voucher chưa có hiệu lực");
                    } else if (currentTime > voucher.getEndTime()) {
                        summary.setInvalidReason("Voucher đã hết hạn");
                    }
                }
                
                summaries.add(summary);
            }
        }
        
        return summaries;
    }

    private List<CheckoutSessionResponse.CheckoutItemResponse> buildCheckoutItemResponses(List<CheckoutSessionRequest.BookQuantity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        List<CheckoutSessionResponse.CheckoutItemResponse> responses = new ArrayList<>();
        for (CheckoutSessionRequest.BookQuantity item : items) {
            CheckoutSessionResponse.CheckoutItemResponse response = new CheckoutSessionResponse.CheckoutItemResponse();
            response.setBookId(item.getBookId());
            response.setQuantity(item.getQuantity());
            Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                response.setBookTitle(book.getBookName());
                response.setBookImage(book.getCoverImageUrl());
            }
            responses.add(response);
        }
        return responses;
    }
}
