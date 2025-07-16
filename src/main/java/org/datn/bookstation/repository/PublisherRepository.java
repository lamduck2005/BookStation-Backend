package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PublisherRepository extends JpaRepository<Publisher, Integer>, JpaSpecificationExecutor<Publisher> {
    
    List<Publisher> findByStatus(Byte status);
    
    Optional<Publisher> findByPublisherNameIgnoreCase(String publisherName);
    
    boolean existsByPublisherNameIgnoreCaseAndIdNot(String publisherName, Integer id);
    
    boolean existsByPublisherNameIgnoreCase(String publisherName);

}
