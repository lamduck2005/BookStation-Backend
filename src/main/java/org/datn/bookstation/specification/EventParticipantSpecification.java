package org.datn.bookstation.specification;

import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.enums.ParticipantStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class EventParticipantSpecification {
    public static Specification<EventParticipant> filterBy(Integer eventId, Integer userId, 
            ParticipantStatus status, Boolean isWinner, String userEmail) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (eventId != null) {
                predicates.add(criteriaBuilder.equal(root.get("event").get("id"), eventId));
            }
            
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("completionStatus"), status));
            }
            
            if (isWinner != null) {
                predicates.add(criteriaBuilder.equal(root.get("isWinner"), isWinner));
            }
            
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("user").get("email")),
                    "%" + userEmail.toLowerCase() + "%"
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
