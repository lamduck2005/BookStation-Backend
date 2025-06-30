package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    Optional<Integer> findIdByCode(String code);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    List<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.orderStatus = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndOrderStatus(@Param("userId") Integer userId, @Param("status") OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.staff.id = :staffId ORDER BY o.createdAt DESC")
    List<Order> findByStaffId(@Param("staffId") Integer staffId);
    
    boolean existsByCode(String code);
}
