package com.titan.repository;

import com.titan.entity.Member;
import com.titan.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE LOWER(p.paymentStatus) = 'paid'")
    Double getTotalRevenue();

    List<Payment> findByMemberOrderByPaymentDateDesc(Member member);

    java.util.Optional<Payment> findFirstByMemberAndPaymentStatusIgnoreCaseOrderByPaymentDateDesc(Member member, String paymentStatus);

}
