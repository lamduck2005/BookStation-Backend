package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

  // ============ STATISTICS QUERIES ============

  // Đếm số đơn hàng theo khoảng thời gian và trạng thái
  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses")
  Long countByDateRangeAndStatuses(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
      @Param("statuses") List<OrderStatus> statuses);

  // Đếm TỔNG số đơn hàng theo khoảng thời gian (tất cả trạng thái)
  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime")
  Long countAllOrdersByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // ✅ SỬA: Tính tổng doanh thu theo khoảng thời gian và trạng thái (chỉ tính
  // subtotal, không tính phí ship)
  @Query("SELECT COALESCE(SUM(o.subtotal), 0) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses")
  BigDecimal sumRevenueByDateRangeAndStatuses(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
      @Param("statuses") List<OrderStatus> statuses);

  // ✅ SỬA: Chỉ tính tiền đã hoàn trả THỰC SỰ (COMPLETED) - không tính APPROVED
  @Query("SELECT COALESCE(SUM(rr.totalRefundAmount), 0) FROM RefundRequest rr " +
      "WHERE rr.order.orderDate >= :startTime AND rr.order.orderDate <= :endTime " +
      "AND rr.status = 'COMPLETED'")
  BigDecimal sumRefundedAmountByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // Tính tổng phí vận chuyển theo khoảng thời gian và trạng thái
  @Query("SELECT COALESCE(SUM(o.shippingFee), 0) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses")
  BigDecimal sumShippingFeeByDateRangeAndStatuses(@Param("startTime") Long startTime,
      @Param("endTime") Long endTime, @Param("statuses") List<OrderStatus> statuses);

  // ✅ SỬA: Đếm số đơn COD theo khoảng thời gian (sử dụng paymentMethod)
  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.paymentMethod = 'COD' AND o.orderStatus IN :statuses")
  Long countCodOrdersByDateRangeAndStatuses(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
      @Param("statuses") List<OrderStatus> statuses);

  // ✅ SỬA: Đếm số đơn COD thất bại (DELIVERY_FAILED, CANCELED) theo khoảng thời
  // gian
  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.paymentMethod = 'COD' AND o.orderStatus IN :statuses")
  Long countFailedCodOrdersByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
      @Param("statuses") List<OrderStatus> statuses);

  // ✅ FIXED: Tính refund bằng subquery để tránh duplicate từ LEFT JOIN
  @Query(value = "SELECT " +
      "CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE) as date, " +
      "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
      "(SELECT SUM(rr.total_refund_amount) " +
      " FROM refund_request rr " +
      " WHERE rr.status = 'COMPLETED' " +
      " AND rr.order_id IN (SELECT o2.id FROM [order] o2 WHERE CAST(DATEADD(SECOND, o2.order_date/1000, '1970-01-01') AS DATE) = CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE)))"
      +
      ", 0) as revenue, " +
      "COUNT(o.id) as orderCount " +
      "FROM [order] o " +
      "WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
      "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
      +
      "GROUP BY CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE) ORDER BY date", nativeQuery = true)
  List<Object[]> findDailyRevenueByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // Top sản phẩm bán chạy theo khoảng thời gian - SQL Server native query
  @Query(value = "SELECT TOP (10) " +
      "b.id as bookId, b.book_name as bookTitle, b.cover_image_url as bookImage, " +
      "'Multiple Authors' as authorName, SUM(od.quantity) as totalQuantity, " +
      "SUM(od.quantity * od.unit_price) as totalRevenue " +
      "FROM order_detail od " +
      "JOIN book b ON od.book_id = b.id " +
      "JOIN [order] o ON od.order_id = o.id " +
      "WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
      "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
      "GROUP BY b.id, b.book_name, b.cover_image_url " +
      "ORDER BY SUM(od.quantity) DESC", nativeQuery = true)
  List<Object[]> findTopProductsByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // ✅ FIXED: Payment method stats với refund logic đúng
  @Query(value = "SELECT o.order_type, COUNT(o.id), " +
      "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
      "(SELECT SUM(rr.total_refund_amount) " +
      " FROM refund_request rr " +
      " WHERE rr.status = 'COMPLETED' " +
      " AND rr.order_id IN (SELECT o2.id FROM [order] o2 WHERE o2.order_type = o.order_type AND o2.order_date >= :startTime AND o2.order_date <= :endTime))"
      +
      ", 0) " +
      "FROM [order] o WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
      "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
      +
      "GROUP BY o.order_type", nativeQuery = true)
  List<Object[]> findPaymentMethodStatsByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // ✅ FIXED: Location stats với refund logic đúng
  @Query(value = "SELECT a.province_name, a.province_id, COUNT(o.id), " +
      "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
      "(SELECT SUM(rr.total_refund_amount) " +
      " FROM refund_request rr " +
      " JOIN [order] o3 ON rr.order_id = o3.id " +
      " JOIN address a3 ON o3.address_id = a3.id " +
      " WHERE rr.status = 'COMPLETED' " +
      " AND a3.province_id = a.province_id " +
      " AND o3.order_date >= :startTime AND o3.order_date <= :endTime)" +
      ", 0) " +
      "FROM [order] o JOIN address a ON o.address_id = a.id " +
      "WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
      "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
      +
      "GROUP BY a.province_name, a.province_id ORDER BY COUNT(o.id) DESC", nativeQuery = true)
  List<Object[]> findLocationStatsByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // Thống kê khách hàng mới vs cũ
  @Query("SELECT u.id, u.fullName, u.email, u.phoneNumber, COUNT(o), COALESCE(SUM(o.totalAmount), 0), " +
      "MIN(o.orderDate) as firstOrder, MAX(o.orderDate) as lastOrder " +
      "FROM Order o JOIN o.user u " +
      "WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses " +
      "GROUP BY u.id, u.fullName, u.email, u.phoneNumber " +
      "ORDER BY SUM(o.totalAmount) DESC")
  List<Object[]> findCustomerStatsByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
      @Param("statuses") List<OrderStatus> statuses);

  // Khách hàng rủi ro cao (có nhiều đơn bị hủy/trả hàng) - Tạm thời comment để
  // test
  // @Query("SELECT u.id, u.fullName, u.email, u.phoneNumber, COUNT(o), " +
  // "SUM(CASE WHEN o.orderStatus IN ('DELIVERY_FAILED', 'CANCELED', 'REFUNDED')
  // THEN 1 ELSE 0 END) " +
  // "FROM Order o JOIN o.user u " +
  // "WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime " +
  // "GROUP BY u.id, u.fullName, u.email, u.phoneNumber " +
  // "HAVING SUM(CASE WHEN o.orderStatus IN ('DELIVERY_FAILED', 'CANCELED',
  // 'REFUNDED') THEN 1 ELSE 0 END) > 0 " +
  // "AND (SUM(CASE WHEN o.orderStatus IN ('DELIVERY_FAILED', 'CANCELED',
  // 'REFUNDED') THEN 1 ELSE 0 END) * 1.0 / COUNT(o)) > 0.3 " +
  // "ORDER BY SUM(CASE WHEN o.orderStatus IN ('DELIVERY_FAILED', 'CANCELED',
  // 'REFUNDED') THEN 1 ELSE 0 END) DESC")
  // List<Object[]> findRiskyCustomersByDateRange(@Param("startTime") Long
  // startTime, @Param("endTime") Long endTime);

  // Đếm khách hàng mới (đặt hàng lần đầu trong khoảng thời gian)
  @Query("SELECT COUNT(DISTINCT u.id) FROM Order o JOIN o.user u " +
      "WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime " +
      "AND o.orderDate = (SELECT MIN(o2.orderDate) FROM Order o2 WHERE o2.user.id = u.id)")
  Long countNewCustomersByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // Đếm khách hàng quay lại
  @Query("SELECT COUNT(DISTINCT u.id) FROM Order o JOIN o.user u " +
      "WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime " +
      "AND EXISTS(SELECT 1 FROM Order o2 WHERE o2.user.id = u.id AND o2.orderDate < :startTime)")
  Long countReturningCustomersByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // phong
//  @Query(value = """
//          SELECT YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as year,
//                 MONTH(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as month,
//                 COALESCE(SUM(total_amount), 0) as revenue
//          FROM [Order]
//          WHERE order_status = 'DELIVERED'
//            AND order_date BETWEEN :start AND :end
//          GROUP BY YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')), MONTH(DATEADD(SECOND, order_date / 1000, '1970-01-01'))
//          ORDER BY year, month
//      """, nativeQuery = true)
//  List<Object[]> getMonthlyRevenue(@Param("start") Long start, @Param("end") Long end);

  // ✅ FIXED: Weekly revenue với refund logic đúng
  @Query(value = "SELECT " +
      "CONCAT(YEAR(DATEADD(SECOND, o.order_date/1000, '1970-01-01')), '-W', " +
      "FORMAT(DATEPART(WEEK, DATEADD(SECOND, o.order_date/1000, '1970-01-01')), '00')) as week_period, " +
      "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
      "(SELECT SUM(rr.total_refund_amount) " +
      " FROM refund_request rr " +
      " WHERE rr.status = 'COMPLETED' " +
      " AND rr.order_id IN (SELECT o2.id FROM [order] o2 WHERE " +
      "   YEAR(DATEADD(SECOND, o2.order_date/1000, '1970-01-01')) = YEAR(DATEADD(SECOND, o.order_date/1000, '1970-01-01')) AND "
      +
      "   DATEPART(WEEK, DATEADD(SECOND, o2.order_date/1000, '1970-01-01')) = DATEPART(WEEK, DATEADD(SECOND, o.order_date/1000, '1970-01-01'))))"
      +
      ", 0) as revenue, " +
      "COUNT(o.id) as orderCount, " +
      "MIN(CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE)) as week_start, " +
      "MAX(CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE)) as week_end " +
      "FROM [order] o WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
      "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
      +
      "GROUP BY YEAR(DATEADD(SECOND, o.order_date/1000, '1970-01-01')), " +
      "DATEPART(WEEK, DATEADD(SECOND, o.order_date/1000, '1970-01-01')) " +
      "ORDER BY week_period", nativeQuery = true)
  List<Object[]> findWeeklyRevenueByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

  // ✅ FIXED: Monthly revenue với refund logic đúng
  @Query(value = "SELECT " +
      "FORMAT(DATEADD(SECOND, o.order_date/1000, '1970-01-01'), 'yyyy-MM') as month_period, " +
      "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
      "(SELECT SUM(rr.total_refund_amount) " +
      " FROM refund_request rr " +
      " WHERE rr.status = 'COMPLETED' " +
      " AND rr.order_id IN (SELECT o2.id FROM [order] o2 WHERE " +
      "   FORMAT(DATEADD(SECOND, o2.order_date/1000, '1970-01-01'), 'yyyy-MM') = FORMAT(DATEADD(SECOND, o.order_date/1000, '1970-01-01'), 'yyyy-MM')))"
      +
      ", 0) as revenue, " +
      "COUNT(o.id) as orderCount, " +
      "MIN(CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE)) as month_start, " +
      "MAX(CAST(DATEADD(SECOND, o.order_date/1000, '1970-01-01') AS DATE)) as month_end " +
      "FROM [order] o WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
      "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
      +
      "GROUP BY FORMAT(DATEADD(SECOND, o.order_date/1000, '1970-01-01'), 'yyyy-MM') " +
      "ORDER BY month_period", nativeQuery = true)
  List<Object[]> findMonthlyRevenueByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

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

  @Query("""

          SELECT COALESCE(SUM(od.quantity), 0)
      FROM Order o
      JOIN OrderDetail od ON od.order.id = o.id
      WHERE o.orderStatus = 'DELIVERED'
      """)
  Long countDeliveredOrders();

  // Sửa findAllWeeklyRevenueByDateRange - Dùng net revenue (subtotal - voucher)
  @Query(value = """
      WITH t AS (
        SELECT DATEADD(SECOND, o.order_date / 1000, '1970-01-01') AS dt,
               CASE WHEN (o.subtotal - o.discount_amount) < 0
                    THEN 0
                    ELSE (o.subtotal - o.discount_amount)
               END AS net_revenue
        FROM [Order] o
        WHERE o.order_date BETWEEN :start AND :end
          AND o.order_status IN ('DELIVERED','PARTIALLY_REFUNDED')
      )
      SELECT
        CONCAT(
          CAST(DATEPART(YEAR, dt) AS VARCHAR(4)),
          '-W',
          RIGHT('00' + CAST(DATEPART(ISO_WEEK, dt) AS VARCHAR(2)), 2)
        ) AS week_key,
        COALESCE(SUM(net_revenue), 0) AS revenue,
        COUNT(*) AS order_count
      FROM t
      GROUP BY CONCAT(
        CAST(DATEPART(YEAR, dt) AS VARCHAR(4)),
        '-W',
        RIGHT('00' + CAST(DATEPART(ISO_WEEK, dt) AS VARCHAR(2)), 2)
      )
      ORDER BY week_key
      """, nativeQuery = true)
  List<Object[]> findAllWeeklyRevenueByDateRange(@Param("start") Long start, @Param("end") Long end);

  // Sửa findAllMonthlyRevenueByDateRange - Chỉ trừ voucher, KHÔNG trừ shipping
  // fee
  @Query(value = """
      WITH t AS (
        SELECT DATEADD(SECOND, o.order_date / 1000, '1970-01-01') AS dt,
               CASE WHEN (o.subtotal - o.discount_amount) < 0
                    THEN 0
                    ELSE (o.subtotal - o.discount_amount)
               END AS net_revenue
        FROM [Order] o
        WHERE o.order_date BETWEEN :start AND :end
          AND o.order_status IN ('DELIVERED','PARTIALLY_REFUNDED')
      )
      SELECT
        CONCAT(
          CAST(DATEPART(YEAR, dt) AS VARCHAR(4)),
          '-',
          RIGHT('00' + CAST(DATEPART(MONTH, dt) AS VARCHAR(2)), 2)
        ) AS month_key,
        COALESCE(SUM(net_revenue), 0) AS revenue
      FROM t
      GROUP BY CONCAT(
        CAST(DATEPART(YEAR, dt) AS VARCHAR(4)),
        '-',
        RIGHT('00' + CAST(DATEPART(MONTH, dt) AS VARCHAR(2)), 2)
      )
      ORDER BY month_key
      """, nativeQuery = true)
  List<Object[]> findAllMonthlyRevenueByDateRange(@Param("start") Long start, @Param("end") Long end);

  // findYearlyRevenueByDateRange ĐÃ ĐÚNG - giữ nguyên
  @Query(value = """
      SELECT
        CAST(DATEPART(YEAR, DATEADD(SECOND, o.order_date / 1000, '1970-01-01')) AS VARCHAR(4)) AS year_key,
        COALESCE(SUM(
          CASE WHEN (o.subtotal - o.discount_amount  ) < 0
               THEN 0
               ELSE (o.subtotal - o.discount_amount )
          END
        ), 0) AS revenue
      FROM [Order] o
      WHERE o.order_date BETWEEN :start AND :end
        AND o.order_status IN ('DELIVERED','PARTIALLY_REFUNDED')
      GROUP BY CAST(DATEPART(YEAR, DATEADD(SECOND, o.order_date / 1000, '1970-01-01')) AS VARCHAR(4))
      ORDER BY year_key
      """, nativeQuery = true)
  List<Object[]> findYearlyRevenueByDateRange(@Param("start") Long start, @Param("end") Long end);

  // ================================================================
  // ORDER STATISTICS APIs - 2-TIER ARCHITECTURE QUERIES
  // ================================================================

  /**
   * 📊 ORDER STATISTICS SUMMARY - Query dữ liệu tổng quan theo ngày (SIMPLIFIED)
   * Tương tự BookRepository.findBookSalesSummaryByDateRange() nhưng cho Order
   * 
   * Trả về: date, totalOrders, completedOrders, canceledOrders, refundedOrders, netRevenue
   */
  @Query(value = "SELECT " +
         "CAST(DATEADD(HOUR, 7, DATEADD(SECOND, o.order_date / 1000, '1970-01-01')) AS DATE) as saleDate, " +
         "COUNT(o.id) as totalOrders, " +
         "SUM(CASE WHEN o.order_status = 'DELIVERED' THEN 1 ELSE 0 END) as completedOrders, " +
         "SUM(CASE WHEN o.order_status = 'CANCELED' THEN 1 ELSE 0 END) as canceledOrders, " +
         "SUM(CASE WHEN o.order_status IN ('PARTIALLY_REFUNDED', 'REFUNDED') THEN 1 ELSE 0 END) as refundedOrders, " +
         "COALESCE(SUM(o.total_amount - COALESCE(o.shipping_fee, 0)), 0) as netRevenue " +
         "FROM [order] o " +
         "WHERE o.order_date >= :startDate AND o.order_date <= :endDate " +
         "GROUP BY CAST(DATEADD(HOUR, 7, DATEADD(SECOND, o.order_date / 1000, '1970-01-01')) AS DATE) " +
         "ORDER BY saleDate", nativeQuery = true)
  List<Object[]> findOrderStatisticsSummaryByDateRange(@Param("startDate") Long startDate, @Param("endDate") Long endDate);

  /**
   * 📊 ORDER STATISTICS DETAILS - Query chi tiết đơn hàng trong khoảng thời gian
   * Tương tự BookRepository.findTopBooksByDateRange() nhưng cho Order
   * 
   * Trả về: order_code, customer_name, customer_email, total_amount, order_status, created_at, product_info
   */
  @Query(value = "SELECT TOP (:limit) " +
         "o.code as orderCode, " +
         "u.full_name as customerName, " +
         "u.email as customerEmail, " +
         "o.total_amount as totalAmount, " +
         "o.order_status as orderStatus, " +
         "o.order_date as createdAt, " +
         "( " +
         "  SELECT STRING_AGG( " +
         "    CONCAT(b.book_name, ' (ISBN:', b.isbn, ', ID:', b.id, ')'), " +
         "    ', ' " +
         "  ) " +
         "  FROM order_detail od " +
         "  JOIN book b ON od.book_id = b.id " +
         "  WHERE od.order_id = o.id " +
         ") as productInfo " +
         "FROM [order] o " +
         "JOIN [user] u ON o.user_id = u.id " +
         "WHERE o.order_date >= :startDate AND o.order_date <= :endDate " +
         "ORDER BY o.order_date DESC", nativeQuery = true)
  List<Object[]> findOrderDetailsByDateRange(@Param("startDate") Long startDate, @Param("endDate") Long endDate, @Param("limit") Integer limit);
}
