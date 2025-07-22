package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.PublisherRequest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Publisher;

import java.util.List;

public interface PublisherService {
    PaginationResponse<PublisherRequest> getAllWithPagination(int page, int size, String publisherName, String email, String status);

    void addPublisher(PublisherRequest request);

    void editPublisher(PublisherRequest request);

    void deletePublisher(Integer id);

    void upStatus(Integer id, byte status, String updatedBy);
    
    List<Publisher> getActivePublishers(); // For dropdown

    List<Publisher> getAllPublisher();

}
