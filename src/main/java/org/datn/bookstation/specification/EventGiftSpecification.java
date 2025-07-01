package org.datn.bookstation.specification;

import org.datn.bookstation.entity.EventGift;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EventGiftSpecification {
    public static Specification<EventGift> filterBy(String giftName, Integer eventId, String giftType, Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (giftName != null && !giftName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("giftName")),
                    "%" + giftName.toLowerCase() + "%"
                ));
            }
            
            if (eventId != null) {
                predicates.add(criteriaBuilder.equal(root.get("event").get("id"), eventId));
            }
            
            if (giftType != null && !giftType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("giftType"), giftType));
            }
            
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
