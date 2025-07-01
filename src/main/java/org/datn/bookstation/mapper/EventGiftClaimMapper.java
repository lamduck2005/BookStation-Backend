package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.EventGiftClaimRequest;
import org.datn.bookstation.entity.EventGiftClaim;
import org.datn.bookstation.entity.EventGift;
import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.GiftDeliveryMethod;
import org.springframework.stereotype.Component;

@Component
public class EventGiftClaimMapper {

    public EventGiftClaim toEntity(EventGiftClaimRequest request, 
                                 EventParticipant eventParticipant, 
                                 EventGift eventGift) {
        EventGiftClaim claim = new EventGiftClaim();
        claim.setEventParticipant(eventParticipant);
        claim.setEventGift(eventGift);
        claim.setNotes(request.getNotes());
        
        // Set delivery method
        if (request.getDeliveryMethod() != null) {
            try {
                claim.setDeliveryMethod(GiftDeliveryMethod.valueOf(request.getDeliveryMethod()));
            } catch (IllegalArgumentException e) {
                claim.setDeliveryMethod(GiftDeliveryMethod.ONLINE_SHIPPING);
            }
        }
        
        // Set delivery order if provided
        if (request.getDeliveryOrderId() != null) {
            Order order = new Order();
            order.setId(request.getDeliveryOrderId());
            claim.setDeliveryOrder(order);
        }
        
        claim.setPickupStoreId(request.getPickupStoreId());
        
        return claim;
    }

    public void updateEntity(EventGiftClaim existingClaim, EventGiftClaimRequest request) {
        existingClaim.setNotes(request.getNotes());
        
        // Update delivery method
        if (request.getDeliveryMethod() != null) {
            try {
                existingClaim.setDeliveryMethod(GiftDeliveryMethod.valueOf(request.getDeliveryMethod()));
            } catch (IllegalArgumentException e) {
                existingClaim.setDeliveryMethod(GiftDeliveryMethod.ONLINE_SHIPPING);
            }
        }
        
        // Update delivery order if provided
        if (request.getDeliveryOrderId() != null) {
            Order order = new Order();
            order.setId(request.getDeliveryOrderId());
            existingClaim.setDeliveryOrder(order);
        } else {
            existingClaim.setDeliveryOrder(null);
        }
        
        existingClaim.setPickupStoreId(request.getPickupStoreId());
    }
}
