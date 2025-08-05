package com.tien.project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PurchaseRequest {

    @NotNull
    private BigDecimal totalAmount;

    @NotNull
    private String currency;

    @NotNull
    private String paymentMethod;

    @NotNull
    private String status;

    private String notes;


}