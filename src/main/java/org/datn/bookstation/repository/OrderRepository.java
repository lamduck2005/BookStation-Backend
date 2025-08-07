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

  @Query(value = """
              SELECT YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as year,
                     MONTH(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as month,
                     COALESCE(SUM(total_amount), 0) as revenue
              FROM [Order]
              WHERE order_status = 'DELIVERED'
                AND order_date BETWEEN :start AND :end
              GROUP BY YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')), MONTH(DATEADD(SECOND, order_date / 1000, '1970-01-01'))
              ORDER BY year, month
          """, nativeQuery = true)
  List<Object[]> getMonthlyRevenue(@Param("start") Long start, @Param("end") Long end);

  @Query(value = """
          SELECT YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as year,
                 DATEPART(WEEK, DATEADD(SECOND, order_date / 1000, '1970-01-01')) as week,
                 COALESCE(SUM(total_amount), 0) as revenue
          FROM [Order]
          WHERE order_status = 'DELIVERED'
            AND order_date BETWEEN :start AND :end
          GROUP BY YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')), DATEPART(WEEK, DATEADD(SECOND, order_date / 1000, '1970-01-01'))
          ORDER BY year, week
      """, nativeQuery = true)
  List<Object[]> getWeeklyRevenue(@Param("start") Long start, @Param("end") Long end);

  @Query(value = """
          SELECT YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as year,
                 COALESCE(SUM(total_amount), 0) as revenue
          FROM [Order]
          WHERE order_status = 'DELIVERED'
            AND order_date BETWEEN :start AND :end
          GROUP BY YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01'))
          ORDER BY year
      """, nativeQuery = true)
  List<Object[]> getYearlyRevenue(@Param("start") Long start, @Param("end") Long end);

  @Query(value = """
          SELECT
              YEAR(DATEADD(SECOND, o.order_date / 1000, '1970-01-01')) as year,
              MONTH(DATEADD(SECOND, o.order_date / 1000, '1970-01-01')) as month,
              COALESCE(SUM(od.quantity), 0) as total_sold
          FROM [Order] o
          JOIN order_detail od ON o.id = od.order_id
          WHERE o.order_status = 'DELIVERED'
            AND o.order_date BETWEEN :start AND :end
          GROUP BY YEAR(DATEADD(SECOND, o.order_date / 1000, '1970-01-01')), MONTH(DATEADD(SECOND, o.order_date / 1000, '1970-01-01'))
          ORDER BY year, month
      """, nativeQuery = true)
  List<Object[]> getMonthlySoldQuantity(@Param("start") Long start, @Param("end") Long end);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
  Long countDeliveredOrders();
}
