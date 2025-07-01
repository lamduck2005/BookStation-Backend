package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.EventParticipantRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.EventParticipantResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Event;
import org.datn.bookstation.entity.EventParticipant;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.ParticipantStatus;
import org.datn.bookstation.mapper.EventParticipantMapper;
import org.datn.bookstation.mapper.EventParticipantResponseMapper;
import org.datn.bookstation.repository.EventParticipantRepository;
import org.datn.bookstation.repository.EventRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.EventParticipantService;
import org.datn.bookstation.specification.EventParticipantSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventParticipantServiceImpl implements EventParticipantService {
    private final EventParticipantRepository eventParticipantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventParticipantMapper eventParticipantMapper;
    private final EventParticipantResponseMapper eventParticipantResponseMapper;

    @Override
    public PaginationResponse<EventParticipantResponse> getAllWithPagination(int page, int size, Integer eventId, 
            Integer userId, ParticipantStatus status, Boolean isWinner, String userEmail) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());
        Specification<EventParticipant> specification = EventParticipantSpecification.filterBy(eventId, userId, status, isWinner, userEmail);
        Page<EventParticipant> participantPage = eventParticipantRepository.findAll(specification, pageable);
        
        List<EventParticipantResponse> participantResponses = participantPage.getContent().stream()
                .map(eventParticipantResponseMapper::toResponse)
                .collect(Collectors.toList());
                
        return PaginationResponse.<EventParticipantResponse>builder()
                .content(participantResponses)
                .pageNumber(participantPage.getNumber())
                .pageSize(participantPage.getSize())
                .totalElements(participantPage.getTotalElements())
                .totalPages(participantPage.getTotalPages())
                .build();
    }

    @Override
    public List<EventParticipant> getAll() {
        return eventParticipantRepository.findAll();
    }

    @Override
    public List<EventParticipant> getByEventId(Integer eventId) {
        return eventParticipantRepository.findByEventId(eventId);
    }

    @Override
    public List<EventParticipant> getByUserId(Integer userId) {
        return eventParticipantRepository.findByUserId(userId);
    }

    @Override
    public EventParticipant getById(Integer id) {
        return eventParticipantRepository.findById(id).orElse(null);
    }

    @Override
    public EventParticipant findByEventIdAndUserId(Integer eventId, Integer userId) {
        return eventParticipantRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
    }

    @Override
    public ApiResponse<EventParticipant> add(EventParticipantRequest request) {
        // Validate event exists
        Event event = eventRepository.findById(request.getEventId()).orElse(null);
        if (event == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        // Validate user exists
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "Không tìm thấy người dùng", null);
        }
        
        // Check if user already joined this event
        EventParticipant existing = findByEventIdAndUserId(request.getEventId(), request.getUserId());
        if (existing != null) {
            return new ApiResponse<>(409, "Người dùng đã tham gia sự kiện này", null);
        }
        
        EventParticipant participant = eventParticipantMapper.toEventParticipant(request);
        participant.setEvent(event);
        participant.setUser(user);
        
        participant.setJoinedAt(Instant.now().toEpochMilli());
        EventParticipant saved = eventParticipantRepository.save(participant);
        return new ApiResponse<>(201, "Tạo mới thành công", saved);
    }

    @Override
    public ApiResponse<EventParticipant> update(EventParticipantRequest request, Integer id) {
        EventParticipant existing = eventParticipantRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Không tìm thấy người tham gia", null);
        }
        
        // Validate event exists
        Event event = eventRepository.findById(request.getEventId()).orElse(null);
        if (event == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        // Validate user exists
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "Không tìm thấy người dùng", null);
        }
        
        existing.setEvent(event);
        existing.setUser(user);
        existing.setIsWinner(request.getIsWinner());
        existing.setCompletionStatus(request.getCompletionStatus());
        existing.setNotes(request.getNotes());
        
        EventParticipant saved = eventParticipantRepository.save(existing);
        return new ApiResponse<>(200, "Cập nhật thành công", saved);
    }

    @Override
    public void delete(Integer id) {
        eventParticipantRepository.deleteById(id);
    }

    @Override
    public ApiResponse<EventParticipant> joinEvent(Integer eventId, Integer userId) {
        // Validate event exists
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return new ApiResponse<>(404, "Không tìm thấy sự kiện", null);
        }
        
        // Validate user exists
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "Không tìm thấy người dùng", null);
        }
        
        // Check if user already joined this event
        EventParticipant existing = findByEventIdAndUserId(eventId, userId);
        if (existing != null) {
            return new ApiResponse<>(409, "Người dùng đã tham gia sự kiện này", null);
        }
        
        // Check if event has reached max participants
        if (event.getMaxParticipants() != null) {
            Integer currentCount = eventParticipantRepository.countRegisteredParticipants(eventId);
            if (currentCount >= event.getMaxParticipants()) {
                return new ApiResponse<>(400, "Sự kiện đã đạt số lượng tham gia tối đa", null);
            }
        }
        
        EventParticipant participant = new EventParticipant();
        participant.setEvent(event);
        participant.setUser(user);
        participant.setCompletionStatus(ParticipantStatus.JOINED);
        participant.setJoinedAt(Instant.now().toEpochMilli());
        
        EventParticipant saved = eventParticipantRepository.save(participant);
        
        // Update event's current participants count
        event.setCurrentParticipants((event.getCurrentParticipants() != null ? event.getCurrentParticipants() : 0) + 1);
        eventRepository.save(event);
        
        return new ApiResponse<>(201, "Tham gia sự kiện thành công", saved);
    }

    @Override
    public ApiResponse<EventParticipant> completeTask(Integer participantId) {
        EventParticipant participant = eventParticipantRepository.findById(participantId).orElse(null);
        if (participant == null) {
            return new ApiResponse<>(404, "Không tìm thấy người tham gia", null);
        }
        
        if (participant.getCompletionStatus() == ParticipantStatus.COMPLETED) {
            return new ApiResponse<>(400, "Người tham gia đã hoàn thành nhiệm vụ", null);
        }
        
        participant.setCompletionStatus(ParticipantStatus.COMPLETED);
        EventParticipant saved = eventParticipantRepository.save(participant);
        
        return new ApiResponse<>(200, "Hoàn thành nhiệm vụ thành công", saved);
    }
}
