package com.titan.service;

import com.titan.entity.Payment;
import com.titan.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    public Payment savePayment(Payment payment) {
        if (payment == null || payment.getMember() == null || payment.getMembershipPlan() == null
                || payment.getAmount() == null || !Double.isFinite(payment.getAmount())
                || payment.getAmount() <= 0 || payment.getPaymentDate() == null) {
            throw new IllegalArgumentException("A valid member, plan, positive amount, and payment date are required.");
        }
        String status = payment.getPaymentStatus() == null ? "" : payment.getPaymentStatus().trim().toLowerCase(Locale.ROOT);
        if (!Set.of("paid", "pending", "failed").contains(status)) {
            throw new IllegalArgumentException("Payment status must be Paid, Pending, or Failed.");
        }
        String method = payment.getPaymentMethod() == null ? "" : payment.getPaymentMethod().trim();
        if (method.isBlank()) throw new IllegalArgumentException("A payment method is required.");
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(status.substring(0, 1).toUpperCase(Locale.ROOT) + status.substring(1));
        return repository.save(payment);
    }

    public Payment getPayment(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deletePayment(Long id) {
        repository.deleteById(id);
    }

    public long getTotalPayments() {
        return repository.count();
    }
}
