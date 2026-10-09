package com.titan.controller;

import com.titan.entity.Member;
import com.titan.repository.MemberRepository;
import com.titan.repository.MembershipPlanRepository;
import com.titan.repository.PaymentRepository;
import com.titan.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    private final PaymentService paymentService;

    public MemberMembershipController(MemberRepository memberRepository, MembershipPlanRepository planRepository,
                                      PaymentRepository paymentRepository, PaymentService paymentService) {
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @GetMapping("/member/membership")
    public String membership(Authentication authentication, Model model) {
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        if (member == null) return "redirect:/login";
        model.addAttribute("member", member);
        var latestPaid = paymentRepository.findFirstByMemberAndPaymentStatusIgnoreCaseOrderByPaymentDateDesc(member, "Paid");
        String name = latestPaid.map(p -> p.getMembershipPlan().getPlanName()).orElse(member.getMembershipPlan());
        model.addAttribute("planName", name == null || name.isBlank() ? "No plan assigned" : name);
        var plan = latestPaid.map(com.titan.entity.Payment::getMembershipPlan)
                .or(() -> name == null ? java.util.Optional.empty() : planRepository.findByPlanNameIgnoreCase(name));
        model.addAttribute("duration", plan.map(p -> p.getDurationMonths() == null ? "Not specified" : p.getDurationMonths() + " months").orElse("Not specified"));
        LocalDate start = latestPaid.map(com.titan.entity.Payment::getPaymentDate).orElse(null);
        model.addAttribute("startDate", start == null ? "Not recorded" : start.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        Integer months = plan.map(com.titan.entity.MembershipPlan::getDurationMonths).orElse(null);
        LocalDate expiry = start == null || months == null ? null : start.plusMonths(months);
        model.addAttribute("expiryDate", expiry == null ? "Not available" : expiry.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        String status = latestPaid.isEmpty() ? "Awaiting payment" : (expiry != null && expiry.isBefore(LocalDate.now()) ? "Expired" : "Active");
        model.addAttribute("membershipStatus", status);
        model.addAttribute("latestPaymentStatus", paymentRepository.findByMemberOrderByPaymentDateDesc(member).stream()
                .findFirst().map(p -> p.getPaymentStatus()).orElse("No payments recorded"));
        model.addAttribute("plans", planRepository.findAll().stream()
                .filter(p -> p.getStatus() == null || !"inactive".equalsIgnoreCase(p.getStatus())).toList());
        model.addAttribute("renewalPending", paymentRepository.findByMemberOrderByPaymentDateDesc(member).stream()
                .anyMatch(p -> "Pending".equalsIgnoreCase(p.getPaymentStatus()) && "Renewal Request".equalsIgnoreCase(p.getPaymentMethod())));
        return "member/membership";
    }

    @PostMapping("/member/membership/request")
    public String requestMembership(@RequestParam Long planId, Authentication authentication, RedirectAttributes redirectAttributes) {
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        if (member == null) return "redirect:/login";
        var selectedPlan = planRepository.findById(planId)
                .filter(p -> p.getStatus() == null || !"inactive".equalsIgnoreCase(p.getStatus()));
        if (selectedPlan.isEmpty() || selectedPlan.get().getPrice() == null || selectedPlan.get().getPrice() <= 0) {
            redirectAttributes.addFlashAttribute("membershipError", "That membership plan is not available.");
            return "redirect:/member/membership";
        }
        boolean pending = paymentRepository.findByMemberOrderByPaymentDateDesc(member).stream()
                .anyMatch(p -> "Pending".equalsIgnoreCase(p.getPaymentStatus()) && "Renewal Request".equalsIgnoreCase(p.getPaymentMethod()));
        if (pending) {
            redirectAttributes.addFlashAttribute("membershipError", "You already have a membership request waiting for staff review.");
            return "redirect:/member/membership";
        }
        var plan = selectedPlan.get();
        com.titan.entity.Payment payment = new com.titan.entity.Payment();
        payment.setMember(member);
        payment.setMembershipPlan(plan);
        payment.setAmount(plan.getPrice());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("Renewal Request");
        payment.setPaymentStatus("Pending");
        paymentService.savePayment(payment);
        redirectAttributes.addFlashAttribute("membershipSuccess", "Your request was sent to the gym. Staff will confirm it after payment.");
        return "redirect:/member/membership";
    }
}
