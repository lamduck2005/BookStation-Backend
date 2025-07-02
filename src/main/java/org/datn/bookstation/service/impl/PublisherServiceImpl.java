package org.datn.bookstation.service.impl;

import org.datn.bookstation.dto.request.PublisherRequest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Publisher;
import org.datn.bookstation.repository.PublisherRepository;
import org.datn.bookstation.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublisherServiceImpl implements PublisherService {
    @Autowired
    private PublisherRepository publisherRepository;

    @Override
    public PaginationResponse<PublisherRequest> getAllWithPagination(int page, int size, String publisherName, String email, String status) {
        Pageable pageable = PageRequest.of(page, size);

        Specification<Publisher> spec = null;

        if (publisherName != null && !publisherName.isEmpty()) {
            Specification<Publisher> nameSpec = (root, query, cb) -> cb.like(cb.lower(root.get("publisherName")), "%" + publisherName.toLowerCase() + "%");
            spec = spec == null ? nameSpec : spec.and(nameSpec);
        }
        if (email != null && !email.isEmpty()) {
            Specification<Publisher> emailSpec = (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
            spec = spec == null ? emailSpec : spec.and(emailSpec);
        }
        if (status != null && !status.isEmpty()) {
            Specification<Publisher> statusSpec = (root, query, cb) -> cb.equal(root.get("status"), status);
            spec = spec == null ? statusSpec : spec.and(statusSpec);
        }

        Page<Publisher> publisherPage = publisherRepository.findAll(spec, pageable);

        List<PublisherRequest> responses = publisherPage.getContent().stream().map(publisher -> {
            PublisherRequest dto = new PublisherRequest();
            dto.setId(publisher.getId());
            dto.setPublisherName(publisher.getPublisherName());
            dto.setPhoneNumber(publisher.getPhoneNumber());
            dto.setEmail(publisher.getEmail());
            dto.setAddress(publisher.getAddress());
            dto.setWebsite(publisher.getWebsite());
            dto.setEstablishedYear(publisher.getEstablishedYear());
            dto.setDescription(publisher.getDescription());
            dto.setStatus(publisher.getStatus());
            dto.setCreatedBy(publisher.getCreatedBy());
            dto.setUpdatedBy(publisher.getUpdatedBy());
            return dto;
        }).collect(Collectors.toList());

        return new PaginationResponse<>(
                responses,
                publisherPage.getNumber(),
                publisherPage.getSize(),
                publisherPage.getTotalElements(),
                publisherPage.getTotalPages()
        );
    }

    @Override
    public void addPublisher(PublisherRequest request) {
        Publisher publisher = new Publisher();
        publisher.setPublisherName(request.getPublisherName());
        publisher.setPhoneNumber(request.getPhoneNumber());
        publisher.setEmail(request.getEmail());
        publisher.setAddress(request.getAddress());
        publisher.setWebsite(request.getWebsite());
        publisher.setEstablishedYear(request.getEstablishedYear());
        publisher.setDescription(request.getDescription());
        publisher.setStatus((byte) 1);
        publisher.setCreatedBy(request.getCreatedBy());
        publisher.setUpdatedBy(request.getCreatedBy());
        publisherRepository.save(publisher);
    }

    @Override
    public void editPublisher(PublisherRequest request) {
        Publisher publisher = publisherRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        publisher.setPublisherName(request.getPublisherName());
        publisher.setPhoneNumber(request.getPhoneNumber());
        publisher.setEmail(request.getEmail());
        publisher.setAddress(request.getAddress());
        publisher.setWebsite(request.getWebsite());
        publisher.setEstablishedYear(request.getEstablishedYear());
        publisher.setDescription(request.getDescription());
        
        // Nếu FE không gửi status thì giữ nguyên status cũ
        if (request.getStatus() != null) {
            publisher.setStatus(request.getStatus());
        }

        publisher.setUpdatedBy(request.getUpdatedBy());
        publisherRepository.save(publisher);
    }

    @Override
    public void deletePublisher(Integer id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found"));
        publisherRepository.delete(publisher);
    }

    @Override
    public void upStatus(Integer id, byte status, String updatedBy) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found"));
        publisher.setStatus(status);
        // Convert String to Integer if needed
        try {
            publisher.setUpdatedBy(Integer.parseInt(updatedBy));
        } catch (NumberFormatException e) {
            publisher.setUpdatedBy(1); // Default system user
        }
        publisherRepository.save(publisher);
    }

    @Override
    public List<Publisher> getActivePublishers() {
        return publisherRepository.findByStatus((byte) 1);
    }
}
