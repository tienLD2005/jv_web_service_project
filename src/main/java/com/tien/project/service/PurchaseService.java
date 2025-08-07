package com.tien.project.service;

import com.tien.project.dto.request.PurchaseRequest;
import com.tien.project.entity.Purchase;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PurchaseService {
    List<Purchase> getPurchasesByCustomerId(Integer customerId);
    Purchase addPurchase(Integer customerId, PurchaseRequest request);
    Purchase updatePurchase(Integer customerId, Integer purchaseId, PurchaseRequest request);
    boolean deletePurchase(Integer customerId, Integer purchaseId);
    List<Map<String, Object>> getRevenueReportBetween(LocalDate start, LocalDate end);
    List<Map<String, Object>> getTopCustomers(int limit);
}