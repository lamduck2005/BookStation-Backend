package org.datn.bookstation.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.datn.bookstation.entity.EventGiftClaim;
import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.EventGift;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.GiftClaimStatus;
import org.datn.bookstation.entity.enums.GiftDeliveryMethod;
import org.springframework.data.jpa.domain.Specification;

public class EventGiftClaimSpecification {

    public static Specification<EventGiftClaim> hasEventId(Integer eventId) {
        return (root, query, criteriaBuilder) -> {
            if (eventId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<EventGiftClaim, EventParticipant> participantJoin = root.join("eventParticipant", JoinType.LEFT);
            Join<EventParticipant, Event> eventJoin = participantJoin.join("event", JoinType.LEFT);
            return criteriaBuilder.equal(eventJoin.get("id"), eventId);
        };
    }

    public static Specification<EventGiftClaim> hasEventParticipantId(Integer eventParticipantId) {
        return (root, query, criteriaBuilder) -> {
            if (eventParticipantId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("eventParticipant").get("id"), eventParticipantId);
        };
    }

    public static Specification<EventGiftClaim> hasEventGiftId(Integer eventGiftId) {
        return (root, query, criteriaBuilder) -> {
            if (eventGiftId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("eventGift").get("id"), eventGiftId);
        };
    }

    public static Specification<EventGiftClaim> hasUserId(Integer userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<EventGiftClaim, EventParticipant> participantJoin = root.join("eventParticipant", JoinType.LEFT);
            Join<EventParticipant, User> userJoin = participantJoin.join("user", JoinType.LEFT);
            return criteriaBuilder.equal(userJoin.get("id"), userId);
        };
    }

    public static Specification<EventGiftClaim> hasClaimStatus(String claimStatus) {
        return (root, query, criteriaBuilder) -> {
            if (claimStatus == null || claimStatus.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            try {
                GiftClaimStatus status = GiftClaimStatus.valueOf(claimStatus.toUpperCase());
                return criteriaBuilder.equal(root.get("claimStatus"), status);
            } catch (IllegalArgumentException e) {
                return criteriaBuilder.conjunction();
            }
        };
    }

    public static Specification<EventGiftClaim> hasDeliveryMethod(String deliveryMethod) {
        return (root, query, criteriaBuilder) -> {
            if (deliveryMethod == null || deliveryMethod.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            try {
                GiftDeliveryMethod method = GiftDeliveryMethod.valueOf(deliveryMethod.toUpperCase());
                return criteriaBuilder.equal(root.get("deliveryMethod"), method);
            } catch (IllegalArgumentException e) {
                return criteriaBuilder.conjunction();
            }
        };
    }

    public static Specification<EventGiftClaim> hasGiftType(String giftType) {
        return (root, query, criteriaBuilder) -> {
            if (giftType == null || giftType.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<EventGiftClaim, EventGift> giftJoin = root.join("eventGift", JoinType.LEFT);
            return criteriaBuilder.equal(giftJoin.get("giftType"), giftType);
        };
    }

    public static Specification<EventGiftClaim> hasAutoDelivered(Boolean autoDelivered) {
        return (root, query, criteriaBuilder) -> {
            if (autoDelivered == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("autoDelivered"), autoDelivered);
        };
    }

    public static Specification<EventGiftClaim> claimedBetween(Long startTime, Long endTime) {
        return (root, query, criteriaBuilder) -> {
            if (startTime == null && endTime == null) {
                return criteriaBuilder.conjunction();
            }
            if (startTime != null && endTime != null) {
                return criteriaBuilder.between(root.get("claimedAt"), startTime, endTime);
            } else if (startTime != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("claimedAt"), startTime);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("claimedAt"), endTime);
            }
        };
    }

    public static Specification<EventGiftClaim> completedBetween(Long startTime, Long endTime) {
        return (root, query, criteriaBuilder) -> {
            if (startTime == null && endTime == null) {
                return criteriaBuilder.conjunction();
            }
            if (startTime != null && endTime != null) {
                return criteriaBuilder.between(root.get("completedAt"), startTime, endTime);
            } else if (startTime != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("completedAt"), startTime);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("completedAt"), endTime);
            }
        };
    }

    public static Specification<EventGiftClaim> hasPickupStoreId(Integer pickupStoreId) {
        return (root, query, criteriaBuilder) -> {
            if (pickupStoreId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("pickupStoreId"), pickupStoreId);
        };
    }
}
