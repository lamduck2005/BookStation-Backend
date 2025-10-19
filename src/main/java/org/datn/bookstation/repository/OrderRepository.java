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

    // SỬA: Tính tổng doanh thu theo khoảng thời gian và trạng thái (chỉ tính
    // subtotal, không tính phí ship)
    @Query("SELECT COALESCE(SUM(o.subtotal), 0) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses")
    BigDecimal sumRevenueByDateRangeAndStatuses(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("statuses") List<OrderStatus> statuses);

    // SỬA: Chỉ tính tiền đã hoàn trả THỰC SỰ (COMPLETED) - không tính APPROVED
    @Query("SELECT COALESCE(SUM(rr.totalRefundAmount), 0) FROM RefundRequest rr " +
            "WHERE rr.order.orderDate >= :startTime AND rr.order.orderDate <= :endTime " +
            "AND rr.status = 'COMPLETED'")
    BigDecimal sumRefundedAmountByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    // THÊM MỚI: Query riêng cho PARTIALLY_REFUNDED orders
    @Query("SELECT COALESCE(SUM(rr.totalRefundAmount), 0) FROM RefundRequest rr " +
            "WHERE rr.order.orderDate >= :startTime AND rr.order.orderDate <= :endTime " +
            "AND rr.order.orderStatus = 'PARTIALLY_REFUNDED' " +
            "AND rr.status = 'COMPLETED'")
    BigDecimal sumRefundedAmountFromPartialOrdersByDateRange(@Param("startTime") Long startTime,
            @Param("endTime") Long endTime);

    // Tính tổng giảm giá (discountAmount + discountShipping)
    @Query("SELECT COALESCE(SUM(o.discountAmount + o.discountShipping), 0) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses")
    BigDecimal sumTotalDiscountsByDateRangeAndStatuses(@Param("startTime") Long startTime,
            @Param("endTime") Long endTime,
            @Param("statuses") List<OrderStatus> statuses);

    // Tính tổng phí vận chuyển theo khoảng thời gian và trạng thái
    @Query("SELECT COALESCE(SUM(o.shippingFee), 0) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderStatus IN :statuses")
    BigDecimal sumShippingFeeByDateRangeAndStatuses(@Param("startTime") Long startTime,
            @Param("endTime") Long endTime, @Param("statuses") List<OrderStatus> statuses);

    // SỬA: Đếm số đơn COD theo khoảng thời gian (sử dụng paymentMethod)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.paymentMethod = 'COD' AND o.orderStatus IN :statuses")
    Long countCodOrdersByDateRangeAndStatuses(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("statuses") List<OrderStatus> statuses);

    // SỬA: Đếm số đơn COD thất bại (DELIVERY_FAILED, CANCELED) theo khoảng thời
    // gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.paymentMethod = 'COD' AND o.orderStatus IN :statuses")
    Long countFailedCodOrdersByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime,
            @Param("statuses") List<OrderStatus> statuses);

    // FIXED: Tính refund bằng subquery để tránh duplicate từ LEFT JOIN
    @Query(value = "SELECT " +
            "CAST(to_timestamp(o.order_date/1000) AS DATE) as date, " +
            "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
            "(SELECT SUM(rr.total_refund_amount) " +
            " FROM refund_request rr " +
            " WHERE rr.status = 'COMPLETED' " +
            " AND rr.order_id IN (SELECT o2.id FROM \"order\" o2 WHERE CAST(to_timestamp(o2.order_date/1000) AS DATE) = CAST(to_timestamp(o.order_date/1000) AS DATE)))"
            +
            ", 0) as revenue, " +
            "COUNT(o.id) as orderCount " +
            "FROM \"order\" o " +
            "WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
            "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
            +
            "GROUP BY CAST(to_timestamp(o.order_date/1000) AS DATE) ORDER BY date", nativeQuery = true)
    List<Object[]> findDailyRevenueByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    // Top sản phẩm bán chạy theo khoảng thời gian - PostgreSQL native query
    @Query(value = "SELECT " +
            "b.id as bookId, b.book_name as bookTitle, b.cover_image_url as bookImage, " +
            "'Multiple Authors' as authorName, SUM(od.quantity) as totalQuantity, " +
            "SUM(od.quantity * od.unit_price) as totalRevenue " +
            "FROM order_detail od " +
            "JOIN book b ON od.book_id = b.id " +
            "JOIN \"order\" o ON od.order_id = o.id " +
            "WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
            "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
            "GROUP BY b.id, b.book_name, b.cover_image_url " +
            "ORDER BY SUM(od.quantity) DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Object[]> findTopProductsByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    // FIXED: Payment method stats với refund logic đúng
    @Query(value = "SELECT o.order_type, COUNT(o.id), " +
            "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
            "(SELECT SUM(rr.total_refund_amount) " +
            " FROM refund_request rr " +
            " WHERE rr.status = 'COMPLETED' " +
            " AND rr.order_id IN (SELECT o2.id FROM \"order\" o2 WHERE o2.order_type = o.order_type AND o2.order_date >= :startTime AND o2.order_date <= :endTime))"
            +
            ", 0) " +
            "FROM \"order\" o WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
            "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
            +
            "GROUP BY o.order_type", nativeQuery = true)
    List<Object[]> findPaymentMethodStatsByDateRange(@Param("startTime") Long startTime,
            @Param("endTime") Long endTime);

    // FIXED: Location stats với refund logic đúng
    @Query(value = "SELECT a.province_name, a.province_id, COUNT(o.id), " +
            "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
            "(SELECT SUM(rr.total_refund_amount) " +
            " FROM refund_request rr " +
            " JOIN \"order\" o3 ON rr.order_id = o3.id " +
            " JOIN address a3 ON o3.address_id = a3.id " +
            " WHERE rr.status = 'COMPLETED' " +
            " AND a3.province_id = a.province_id " +
            " AND o3.order_date >= :startTime AND o3.order_date <= :endTime)" +
            ", 0) " +
            "FROM \"order\" o JOIN address a ON o.address_id = a.id " +
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
    // @Query(value = """
    // SELECT YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as year,
    // MONTH(DATEADD(SECOND, order_date / 1000, '1970-01-01')) as month,
    // COALESCE(SUM(total_amount), 0) as revenue
    // FROM [Order]
    // WHERE order_status = 'DELIVERED'
    // AND order_date BETWEEN :start AND :end
    // GROUP BY YEAR(DATEADD(SECOND, order_date / 1000, '1970-01-01')),
    // MONTH(DATEADD(SECOND, order_date / 1000, '1970-01-01'))
    // ORDER BY year, month
    // """, nativeQuery = true)
    // List<Object[]> getMonthlyRevenue(@Param("start") Long start, @Param("end")
    // Long end);

    // FIXED: Weekly revenue với refund logic đúng
    @Query(value = "SELECT " +
            "CONCAT(EXTRACT(YEAR FROM to_timestamp(o.order_date/1000)), '-W', " +
            "LPAD(EXTRACT(WEEK FROM to_timestamp(o.order_date/1000))::TEXT, 2, '0')) as week_period, " +
            "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
            "(SELECT SUM(rr.total_refund_amount) " +
            " FROM refund_request rr " +
            " WHERE rr.status = 'COMPLETED' " +
            " AND rr.order_id IN (SELECT o2.id FROM \"order\" o2 WHERE " +
            "   EXTRACT(YEAR FROM to_timestamp(o2.order_date/1000)) = EXTRACT(YEAR FROM to_timestamp(o.order_date/1000)) AND "
            +
            "   EXTRACT(WEEK FROM to_timestamp(o2.order_date/1000)) = EXTRACT(WEEK FROM to_timestamp(o.order_date/1000))))"
            +
            ", 0) as revenue, " +
            "COUNT(o.id) as orderCount, " +
            "MIN(CAST(to_timestamp(o.order_date/1000) AS DATE)) as week_start, " +
            "MAX(CAST(to_timestamp(o.order_date/1000) AS DATE)) as week_end " +
            "FROM \"order\" o WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
            "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
            +
            "GROUP BY EXTRACT(YEAR FROM to_timestamp(o.order_date/1000)), " +
            "EXTRACT(WEEK FROM to_timestamp(o.order_date/1000)) " +
            "ORDER BY week_period", nativeQuery = true)
    List<Object[]> findWeeklyRevenueByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    // FIXED: Monthly revenue với refund logic đúng
    @Query(value = "SELECT " +
            "TO_CHAR(to_timestamp(o.order_date/1000), 'YYYY-MM') as month_period, " +
            "COALESCE(SUM(o.subtotal), 0) - COALESCE(" +
            "(SELECT SUM(rr.total_refund_amount) " +
            " FROM refund_request rr " +
            " WHERE rr.status = 'COMPLETED' " +
            " AND rr.order_id IN (SELECT o2.id FROM \"order\" o2 WHERE " +
            "   TO_CHAR(to_timestamp(o2.order_date/1000), 'YYYY-MM') = TO_CHAR(to_timestamp(o.order_date/1000), 'YYYY-MM')))"
            +
            ", 0) as revenue, " +
            "COUNT(o.id) as orderCount, " +
            "MIN(CAST(to_timestamp(o.order_date/1000) AS DATE)) as month_start, " +
            "MAX(CAST(to_timestamp(o.order_date/1000) AS DATE)) as month_end " +
            "FROM \"order\" o WHERE o.order_date >= :startTime AND o.order_date <= :endTime " +
            "AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'REFUNDING') "
            +
            "GROUP BY TO_CHAR(to_timestamp(o.order_date/1000), 'YYYY-MM') " +
            "ORDER BY month_period", nativeQuery = true)
    List<Object[]> findMonthlyRevenueByDateRange(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    @Query(value = """
                SELECT EXTRACT(YEAR FROM to_timestamp(order_date / 1000)) as year,
                       EXTRACT(WEEK FROM to_timestamp(order_date / 1000)) as week,
                       COALESCE(SUM(total_amount), 0) as revenue
                FROM "order"
                WHERE order_status = 'DELIVERED'
                  AND order_date BETWEEN :start AND :end
                GROUP BY EXTRACT(YEAR FROM to_timestamp(order_date / 1000)), EXTRACT(WEEK FROM to_timestamp(order_date / 1000))
                ORDER BY year, week
            """, nativeQuery = true)
    List<Object[]> getWeeklyRevenue(@Param("start") Long start, @Param("end") Long end);

    @Query(value = """
                SELECT EXTRACT(YEAR FROM to_timestamp(order_date / 1000)) as year,
                       COALESCE(SUM(total_amount), 0) as revenue
                FROM "order"
                WHERE order_status = 'DELIVERED'
                  AND order_date BETWEEN :start AND :end
                GROUP BY EXTRACT(YEAR FROM to_timestamp(order_date / 1000))
                ORDER BY year
            """, nativeQuery = true)
    List<Object[]> getYearlyRevenue(@Param("start") Long start, @Param("end") Long end);

    @Query(value = """
                SELECT
                    EXTRACT(YEAR FROM to_timestamp(o.order_date / 1000)) as year,
                    EXTRACT(MONTH FROM to_timestamp(o.order_date / 1000)) as month,
                    COALESCE(SUM(od.quantity), 0) as total_sold
                FROM "order" o
                JOIN order_detail od ON o.id = od.order_id
                WHERE o.order_status = 'DELIVERED'
                  AND o.order_date BETWEEN :start AND :end
                GROUP BY EXTRACT(YEAR FROM to_timestamp(o.order_date / 1000)), EXTRACT(MONTH FROM to_timestamp(o.order_date / 1000))
                ORDER BY year, month
            """, nativeQuery = true)
    List<Object[]> getMonthlySoldQuantity(@Param("start") Long start, @Param("end") Long end);

   @Query(value = """
    SELECT
        (SELECT COALESCE(SUM(od.quantity), 0)
         FROM "order" o
         JOIN order_detail od ON o.id = od.order_id
         WHERE o.order_status IN ('DELIVERED', 'REFUND_REQUESTED', 'PARTIALLY_REFUNDED')
        ) -
        (SELECT COALESCE(SUM(ri.refund_quantity), 0)
         FROM refund_request rr
         JOIN refund_item ri ON rr.id = ri.refund_request_id
         WHERE rr.status = 'COMPLETED'
        ) as net_sold_quantity
    """, nativeQuery = true)
Long countDeliveredOrders();

    // FIXED findAllWeeklyRevenueByDateRange - Match Book API net revenue formula
    @Query(value = """
            WITH t AS (
              SELECT to_timestamp(o.order_date / 1000) AS dt,
                     CASE WHEN o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED')
                          THEN (o.total_amount - COALESCE(o.shipping_fee, 0))
                          ELSE 0
                     END AS net_revenue
              FROM "order" o
              WHERE o.order_date BETWEEN :start AND :end
                AND o.order_status IN ('DELIVERED','PARTIALLY_REFUNDED')
            )
            SELECT
              CONCAT(
                EXTRACT(YEAR FROM dt)::TEXT,
                '-W',
                LPAD(EXTRACT(WEEK FROM dt)::TEXT, 2, '0')
              ) AS week_key,
              COALESCE(SUM(net_revenue), 0) AS revenue,
              COUNT(*) AS order_count
            FROM t
            GROUP BY CONCAT(
              EXTRACT(YEAR FROM dt)::TEXT,
              '-W',
              LPAD(EXTRACT(WEEK FROM dt)::TEXT, 2, '0')
            )
            ORDER BY week_key
            """, nativeQuery = true)
    List<Object[]> findAllWeeklyRevenueByDateRange(@Param("start") Long start, @Param("end") Long end);

    // FIXED findAllMonthlyRevenueByDateRange - Match Book API net revenue formula
    @Query(value = """
            WITH t AS (
              SELECT to_timestamp(o.order_date / 1000) AS dt,
                     CASE WHEN o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED')
                          THEN (o.total_amount - COALESCE(o.shipping_fee, 0))
                          ELSE 0
                     END AS net_revenue
              FROM "order" o
              WHERE o.order_date BETWEEN :start AND :end
                AND o.order_status IN ('DELIVERED','PARTIALLY_REFUNDED')
            )
            SELECT
              CONCAT(
                EXTRACT(YEAR FROM dt)::TEXT,
                '-',
                LPAD(EXTRACT(MONTH FROM dt)::TEXT, 2, '0')
              ) AS month_key,
              COALESCE(SUM(net_revenue), 0) AS revenue
            FROM t
            GROUP BY CONCAT(
              EXTRACT(YEAR FROM dt)::TEXT,
              '-',
              LPAD(EXTRACT(MONTH FROM dt)::TEXT, 2, '0')
            )
            ORDER BY month_key
            """, nativeQuery = true)
    List<Object[]> findAllMonthlyRevenueByDateRange(@Param("start") Long start, @Param("end") Long end);

    // findYearlyRevenueByDateRange - FIXED TO MATCH BOOK API
    @Query(value = """
            SELECT
              EXTRACT(YEAR FROM to_timestamp(o.order_date / 1000))::TEXT AS year_key,
              COALESCE(SUM(
                CASE WHEN o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED')
                     THEN (o.total_amount - COALESCE(o.shipping_fee, 0))
                     ELSE 0
                END
              ), 0) AS revenue
            FROM "order" o
            WHERE o.order_date BETWEEN :start AND :end
              AND o.order_status IN ('DELIVERED','PARTIALLY_REFUNDED')
            GROUP BY EXTRACT(YEAR FROM to_timestamp(o.order_date / 1000))
            ORDER BY year_key
            """, nativeQuery = true)
    List<Object[]> findYearlyRevenueByDateRange(@Param("start") Long start, @Param("end") Long end);

    // ================================================================
    // ORDER STATISTICS APIs - 2-TIER ARCHITECTURE QUERIES
    // ================================================================

    /**
     * 📊 ORDER STATISTICS SUMMARY - Query dữ liệu tổng quan theo ngày (MATCHING
     * BOOK API LOGIC)
     * Sử dụng CÙNG logic tính netRevenue như Book API để đảm bảo consistency:
     * - Proportional revenue allocation từ order_detail
     * - Handles refunds correctly
     * - Matches Book API exactly
     * 
     * Trả về: date, totalOrders, completedOrders, canceledOrders, refundedOrders,
     * netRevenue
     */
    @Query(value = "WITH order_stats AS (" +
            "  SELECT " +
            "    CAST(to_timestamp(o.order_date / 1000) + INTERVAL '7 hours' AS DATE) as saleDate, " +
            "    COUNT(DISTINCT o.id) as totalOrders, " +
            "    SUM(CASE WHEN o.order_status = 'DELIVERED' THEN 1 ELSE 0 END) as completedOrders, " +
            "    SUM(CASE WHEN o.order_status = 'CANCELED' THEN 1 ELSE 0 END) as canceledOrders, " +
            "    SUM(CASE WHEN o.order_status IN ('PARTIALLY_REFUNDED', 'REFUNDED') THEN 1 ELSE 0 END) as refundedOrders "
            +
            "  FROM \"order\" o " +
            "  WHERE o.order_date >= :startDate AND o.order_date <= :endDate " +
            "  GROUP BY CAST(to_timestamp(o.order_date / 1000) + INTERVAL '7 hours' AS DATE) " +
            "), " +
            "revenue_calc AS (" +
            "  SELECT " +
            "    CAST(to_timestamp(o.order_date / 1000) + INTERVAL '7 hours' AS DATE) as saleDate, " +
            "    COALESCE(SUM((o.total_amount - COALESCE(o.shipping_fee, 0)) * ((od.unit_price * od.quantity) / o.subtotal)), 0) - "
            +
            "    COALESCE(SUM((o.total_amount - COALESCE(o.shipping_fee, 0)) * ((refunds.refund_quantity * od.unit_price) / o.subtotal)), 0) as netRevenue "
            +
            "  FROM order_detail od " +
            "  JOIN \"order\" o ON od.order_id = o.id " +
            "  LEFT JOIN ( " +
            "    SELECT rr.order_id, ri.book_id, SUM(ri.refund_quantity) as refund_quantity " +
            "    FROM refund_item ri " +
            "    JOIN refund_request rr ON ri.refund_request_id = rr.id " +
            "    WHERE rr.status = 'COMPLETED' " +
            "    GROUP BY rr.order_id, ri.book_id " +
            "  ) refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id " +
            "  WHERE o.order_date >= :startDate AND o.order_date <= :endDate " +
            "    AND o.order_status IN ('DELIVERED', 'PARTIALLY_REFUNDED') " +
            "  GROUP BY CAST(to_timestamp(o.order_date / 1000) + INTERVAL '7 hours' AS DATE) " +
            ") " +
            "SELECT os.saleDate, os.totalOrders, os.completedOrders, os.canceledOrders, os.refundedOrders, " +
            "       COALESCE(rc.netRevenue, 0) as netRevenue " +
            "FROM order_stats os " +
            "LEFT JOIN revenue_calc rc ON os.saleDate = rc.saleDate " +
            "ORDER BY os.saleDate", nativeQuery = true)
    List<Object[]> findOrderStatisticsSummaryByDateRange(@Param("startDate") Long startDate,
            @Param("endDate") Long endDate);

    /**
     * 📊 ORDER STATISTICS DETAILS - Query chi tiết đơn hàng trong khoảng thời gian
     * Tương tự BookRepository.findTopBooksByDateRange() nhưng cho Order
     * 
     * Trả về: order_code, customer_name, customer_email, total_amount,
     * order_status, created_at, product_info
     */
    @Query(value = "SELECT " +
            "o.code as orderCode, " +
            "u.full_name as customerName, " +
            "u.email as customerEmail, " +
            "o.total_amount as totalAmount, " +
            "o.order_status as orderStatus, " +
            "o.order_date as createdAt, " +
            "( " +
            "  SELECT STRING_AGG( " +
            "    CONCAT(b.book_name, ' (x', od.quantity, ')'), " +
            "    ', ' " +
            "  ) " +
            "  FROM order_detail od " +
            "  JOIN book b ON od.book_id = b.id " +
            "  WHERE od.order_id = o.id " +
            ") as productInfo, " +
            "CASE " +
            "  WHEN o.order_status = 'REFUNDED' THEN 0 " +
            "  WHEN o.order_status = 'PARTIALLY_REFUNDED' THEN " +
            "    CAST(o.subtotal - COALESCE(o.discount_amount + o.discount_shipping, 0) - " +
            "    COALESCE((SELECT SUM(rr.total_refund_amount) FROM refund_request rr WHERE rr.order_id = o.id AND rr.status = 'COMPLETED'), 0) AS DECIMAL(10,2)) "
            +
            "  ELSE CAST(o.subtotal - COALESCE(o.discount_amount + o.discount_shipping, 0) AS DECIMAL(10,2)) " +
            "END as netRevenue " +
            "FROM \"order\" o " +
            "JOIN \"user\" u ON o.user_id = u.id " +
            "WHERE o.order_date >= :startDate AND o.order_date <= :endDate " +
            "ORDER BY o.order_date DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findOrderDetailsByDateRange(@Param("startDate") Long startDate, @Param("endDate") Long endDate,
            @Param("limit") Integer limit);
}
