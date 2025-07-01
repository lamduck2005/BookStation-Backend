package org.datn.bookstation.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.datn.bookstation.entity.EventHistory;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class EventHistorySpecification {

    public static Specification<EventHistory> hasEventId(Integer eventId) {
        return (root, query, criteriaBuilder) -> {
            if (eventId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("event").get("id"), eventId);
        };
    }

    public static Specification<EventHistory> hasActionType(String actionType) {
        return (root, query, criteriaBuilder) -> {
            if (actionType == null || actionType.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("actionType"), actionType);
        };
    }

    public static Specification<EventHistory> hasPerformedBy(Integer performedBy) {
        return (root, query, criteriaBuilder) -> {
            if (performedBy == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("performedBy").get("id"), performedBy);
        };
    }

    public static Specification<EventHistory> createdBetween(Long startTime, Long endTime) {
        return (root, query, criteriaBuilder) -> {
            if (startTime == null && endTime == null) {
                return criteriaBuilder.conjunction();
            }
            if (startTime != null && endTime != null) {
                return criteriaBuilder.between(root.get("createdAt"), startTime, endTime);
            } else if (startTime != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startTime);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endTime);
            }
        };
    }

    public static Specification<EventHistory> descriptionContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), 
                "%" + keyword.toLowerCase() + "%"
            );
        };
    }

    public static Specification<EventHistory> hasEventName(String eventName) {
        return (root, query, criteriaBuilder) -> {
            if (eventName == null || eventName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<EventHistory, Event> eventJoin = root.join("event", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(eventJoin.get("eventName")), 
                "%" + eventName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<EventHistory> hasPerformedByName(String performedByName) {
        return (root, query, criteriaBuilder) -> {
            if (performedByName == null || performedByName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<EventHistory, User> userJoin = root.join("performedBy", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(userJoin.get("fullName")), 
                "%" + performedByName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<EventHistory> actionTypeIn(String[] actionTypes) {
        return (root, query, criteriaBuilder) -> {
            if (actionTypes == null || actionTypes.length == 0) {
                return criteriaBuilder.conjunction();
            }
            return root.get("actionType").in((Object[]) actionTypes);
        };
    }
}
