package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.EventHistoryRequest;
import org.datn.bookstation.dto.response.EventHistoryResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.EventHistory;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.mapper.EventHistoryMapper;
import org.datn.bookstation.mapper.EventHistoryResponseMapper;
import org.datn.bookstation.repository.EventHistoryRepository;
import org.datn.bookstation.repository.EventRepository;
import org.datn.bookstation.service.EventHistoryService;
import org.datn.bookstation.specification.EventHistorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventHistoryServiceImpl implements EventHistoryService {

    private final EventHistoryRepository eventHistoryRepository;
    private final EventRepository eventRepository;
    private final EventHistoryMapper eventHistoryMapper;
    private final EventHistoryResponseMapper eventHistoryResponseMapper;

    @Override
    @Transactional
    public EventHistoryResponse create(EventHistoryRequest request) {
        log.info("Creating new event history for event: {}", request.getEventId());

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getEventId()));

        EventHistory history = eventHistoryMapper.toEntity(request, event);
        history = eventHistoryRepository.save(history);

        log.info("Successfully created event history with id: {}", history.getId());
        return eventHistoryResponseMapper.toResponse(history);
    }

    @Override
    public EventHistoryResponse findById(Integer id) {
        log.info("Finding event history by id: {}", id);
        
        EventHistory history = eventHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event history not found with id: " + id));
        
        return eventHistoryResponseMapper.toResponse(history);
    }

    @Override
    @Transactional
    public EventHistoryResponse update(Integer id, EventHistoryRequest request) {
        log.info("Updating event history with id: {}", id);

        EventHistory existingHistory = eventHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event history not found with id: " + id));

        eventHistoryMapper.updateEntity(existingHistory, request);
        existingHistory = eventHistoryRepository.save(existingHistory);

        log.info("Successfully updated event history with id: {}", id);
        return eventHistoryResponseMapper.toResponse(existingHistory);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        log.info("Deleting event history with id: {}", id);

        if (!eventHistoryRepository.existsById(id)) {
            throw new RuntimeException("Event history not found with id: " + id);
        }

        eventHistoryRepository.deleteById(id);
        log.info("Successfully deleted event history with id: {}", id);
    }

    @Override
    public PaginationResponse<EventHistoryResponse> findAll(
            Integer eventId, String actionType, Integer performedBy, 
            Long createdStartTime, Long createdEndTime, String descriptionKeyword,
            String eventName, String performedByName, String[] actionTypes, Pageable pageable) {

        log.info("Finding all event histories with filters");

        Specification<EventHistory> spec = null;

        if (eventId != null) {
            spec = EventHistorySpecification.hasEventId(eventId);
        }
        if (actionType != null) {
            spec = (spec == null) ? EventHistorySpecification.hasActionType(actionType) : 
                   spec.and(EventHistorySpecification.hasActionType(actionType));
        }
        if (performedBy != null) {
            spec = (spec == null) ? EventHistorySpecification.hasPerformedBy(performedBy) : 
                   spec.and(EventHistorySpecification.hasPerformedBy(performedBy));
        }
        if (createdStartTime != null || createdEndTime != null) {
            spec = (spec == null) ? EventHistorySpecification.createdBetween(createdStartTime, createdEndTime) : 
                   spec.and(EventHistorySpecification.createdBetween(createdStartTime, createdEndTime));
        }
        if (descriptionKeyword != null) {
            spec = (spec == null) ? EventHistorySpecification.descriptionContains(descriptionKeyword) : 
                   spec.and(EventHistorySpecification.descriptionContains(descriptionKeyword));
        }
        if (eventName != null) {
            spec = (spec == null) ? EventHistorySpecification.hasEventName(eventName) : 
                   spec.and(EventHistorySpecification.hasEventName(eventName));
        }
        if (performedByName != null) {
            spec = (spec == null) ? EventHistorySpecification.hasPerformedByName(performedByName) : 
                   spec.and(EventHistorySpecification.hasPerformedByName(performedByName));
        }
        if (actionTypes != null && actionTypes.length > 0) {
            spec = (spec == null) ? EventHistorySpecification.actionTypeIn(actionTypes) : 
                   spec.and(EventHistorySpecification.actionTypeIn(actionTypes));
        }

        Page<EventHistory> historiesPage = eventHistoryRepository.findAll(spec, pageable);
        
        return PaginationResponse.<EventHistoryResponse>builder()
                .content(historiesPage.getContent().stream()
                        .map(eventHistoryResponseMapper::toResponse)
                        .toList())
                .pageNumber(historiesPage.getNumber())
                .pageSize(historiesPage.getSize())
                .totalElements(historiesPage.getTotalElements())
                .totalPages(historiesPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public EventHistoryResponse logAction(Integer eventId, String actionType, String description, Integer performedBy) {
        log.info("Logging action for event: {}, action: {}", eventId, actionType);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventHistory history = new EventHistory();
        history.setEvent(event);
        history.setActionType(actionType);
        history.setDescription(description);
        
        if (performedBy != null) {
            User user = new User();
            user.setId(performedBy);
            history.setPerformedBy(user);
        }

        history = eventHistoryRepository.save(history);

        log.info("Successfully logged action with history id: {}", history.getId());
        return eventHistoryResponseMapper.toResponse(history);
    }

    @Override
    @Transactional
    public EventHistoryResponse logActionWithValues(Integer eventId, String actionType, String description, 
                                                   Integer performedBy, String oldValues, String newValues) {
        log.info("Logging action with values for event: {}, action: {}", eventId, actionType);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        EventHistory history = new EventHistory();
        history.setEvent(event);
        history.setActionType(actionType);
        history.setDescription(description);
        history.setOldValues(oldValues);
        history.setNewValues(newValues);
        
        if (performedBy != null) {
            User user = new User();
            user.setId(performedBy);
            history.setPerformedBy(user);
        }

        history = eventHistoryRepository.save(history);

        log.info("Successfully logged action with values, history id: {}", history.getId());
        return eventHistoryResponseMapper.toResponse(history);
    }

    @Override
    public PaginationResponse<EventHistoryResponse> findHistoryByEvent(Integer eventId, Pageable pageable) {
        log.info("Finding history by event: {}", eventId);
        return findAll(eventId, null, null, null, null, null, null, null, null, pageable);
    }

    @Override
    public PaginationResponse<EventHistoryResponse> findHistoryByUser(Integer userId, Pageable pageable) {
        log.info("Finding history by user: {}", userId);
        return findAll(null, null, userId, null, null, null, null, null, null, pageable);
    }

    @Override
    public PaginationResponse<EventHistoryResponse> findRecentHistory(Integer limit, Pageable pageable) {
        log.info("Finding recent history, limit: {}", limit);
        return findAll(null, null, null, null, null, null, null, null, null, pageable);
    }
}
