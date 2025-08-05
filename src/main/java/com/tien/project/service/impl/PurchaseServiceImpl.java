package com.tien.project.service.impl;

import com.tien.project.dto.request.PurchaseRequest;
import com.tien.project.entity.Purchase;
import com.tien.project.repository.PurchaseRepository;
import com.tien.project.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Override
    public List<Purchase> getPurchasesByCustomerId(Integer customerId) {
        return purchaseRepository.findByCustomerId(customerId);
    }

    @Override
    public Purchase addPurchase(Integer customerId, PurchaseRequest request) {
        Purchase purchase = new Purchase();
        purchase.setCustomerId(customerId);
        purchase.setTotalAmount(request.getTotalAmount());
        purchase.setCurrency(request.getCurrency());
        purchase.setPaymentMethod(request.getPaymentMethod());
        purchase.setStatus(Purchase.PurchaseStatus.valueOf(request.getStatus()));
        purchase.setNotes(request.getNotes());
        purchase.setPurchaseDate(LocalDateTime.now());
        return purchaseRepository.save(purchase);
    }

    @Override
    public Purchase updatePurchase(Integer customerId, Integer purchaseId, PurchaseRequest request) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (purchase != null && purchase.getCustomerId().equals(customerId)) {
            purchase.setTotalAmount(request.getTotalAmount());
            purchase.setCurrency(request.getCurrency());
            purchase.setPaymentMethod(request.getPaymentMethod());
            purchase.setStatus(Purchase.PurchaseStatus.valueOf(request.getStatus()));
            purchase.setNotes(request.getNotes());
            return purchaseRepository.save(purchase);
        }
        return null;
    }

    @Override
    public boolean deletePurchase(Integer customerId, Integer purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (purchase != null && purchase.getCustomerId().equals(customerId)) {
            purchaseRepository.delete(purchase);
            return true;
        }
        return false;
    }
}