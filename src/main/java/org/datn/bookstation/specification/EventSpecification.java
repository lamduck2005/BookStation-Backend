package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.enums.EventStatus;
import org.datn.bookstation.entity.enums.EventType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EventSpecification {
    public static Specification<Event> filterBy(String name, Integer categoryId, EventStatus status, EventType eventType, Long startDate, Long endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (name != null && !name.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("eventName")),
                    "%" + name.toLowerCase() + "%"
                ));
            }
            
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("eventCategory").get("id"), categoryId));
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            if (eventType != null) {
                predicates.add(criteriaBuilder.equal(root.get("eventType"), eventType));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
