package com.titan.service;

import com.titan.entity.Payment;
import com.titan.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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