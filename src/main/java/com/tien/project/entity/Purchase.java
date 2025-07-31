package com.tien.project.entity;
import com.tien.project.entity.enums.EPurchase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Purchases")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Integer purchaseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "purchase_date", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime purchaseDate;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 10, columnDefinition = "VARCHAR(10) DEFAULT 'VND'")
    private String currency;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "status", nullable = false, columnDefinition = "ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'COMPLETED'")
    private EPurchase status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

}
