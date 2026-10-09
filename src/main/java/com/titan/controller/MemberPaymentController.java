package com.titan.controller;

import com.titan.entity.Member;
import com.titan.entity.Payment;
import com.titan.repository.MemberRepository;
import com.titan.repository.PaymentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MemberPaymentController {

    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    public MemberPaymentController(
            PaymentRepository paymentRepository,
            MemberRepository memberRepository) {

        this.paymentRepository = paymentRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/member/payments")
    public String payments(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberRepository.findByUsername(username)
                .orElse(null);
        if (member == null) return "redirect:/login";
        List<Payment> payments = paymentRepository.findByMemberOrderByPaymentDateDesc(member);

        model.addAttribute("payments", payments);

        return "member/payments";
    }
}
