package com.titan.controller;

import com.titan.entity.Member;
import com.titan.repository.MemberRepository;
import com.titan.repository.MembershipPlanRepository;
import com.titan.repository.PaymentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class MemberMembershipController {
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final PaymentRepository paymentRepository;

    public MemberMembershipController(MemberRepository memberRepository, MembershipPlanRepository planRepository,
                                      PaymentRepository paymentRepository) {
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/member/membership")
    public String membership(Authentication authentication, Model model) {
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        if (member == null) return "redirect:/login";
        model.addAttribute("member", member);
        String name = member.getMembershipPlan();
        model.addAttribute("planName", name == null || name.isBlank() ? "No plan assigned" : name);
        var plan = name == null ? java.util.Optional.<com.titan.entity.MembershipPlan>empty()
                : planRepository.findByPlanNameIgnoreCase(name);
        model.addAttribute("duration", plan.map(p -> p.getDurationMonths() == null ? "Not specified" : p.getDurationMonths() + " months").orElse("Not specified"));
        LocalDate start = member.getJoinDate();
        model.addAttribute("startDate", start == null ? "Not recorded" : start.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        Integer months = plan.map(com.titan.entity.MembershipPlan::getDurationMonths).orElse(null);
        LocalDate expiry = start == null || months == null ? null : start.plusMonths(months);
        model.addAttribute("expiryDate", expiry == null ? "Not available" : expiry.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        String status = member.getStatus() == null ? "Not recorded" : member.getStatus();
        if (expiry != null && expiry.isBefore(LocalDate.now())) status = "Expired";
        model.addAttribute("membershipStatus", status);
        model.addAttribute("latestPaymentStatus", paymentRepository.findByMemberOrderByPaymentDateDesc(member).stream()
                .findFirst().map(p -> p.getPaymentStatus()).orElse("No payments recorded"));
        return "member/membership";
    }
}
