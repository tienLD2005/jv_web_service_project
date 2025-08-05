package com.tien.project.repository;

import com.tien.project.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PurchaseRepository extends JpaRepository<Purchase, Integer> {
    List<Purchase> findByCustomerId(Integer customerId);

    @Query(value = """
    SELECT DATE(p.purchase_date) AS date, SUM(p.total_amount) AS totalRevenue
    FROM purchases p
    WHERE p.status = 'COMPLETED'
      AND p.purchase_date >= :start
      AND p.purchase_date < :endPlusOne
    GROUP BY DATE(p.purchase_date)
    ORDER BY date
    """, nativeQuery = true)
    List<Map<String, Object>> getRevenueReportBetween(
            @Param("start") LocalDate start,
            @Param("endPlusOne") LocalDateTime endPlusOne
    );

    @Query(value = """
    SELECT p.customer_id AS customerId, SUM(p.total_amount) AS totalSpent
    FROM purchases p
    WHERE p.status = 'COMPLETED'
    GROUP BY p.customer_id
    ORDER BY totalSpent DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> getTopCustomers(@Param("limit") int limit);
}