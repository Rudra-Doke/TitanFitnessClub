package com.titan.controller;

import com.titan.entity.Payment;
import com.titan.entity.Member;
import com.titan.entity.MembershipPlan;
import com.titan.service.MemberService;
import com.titan.service.MembershipPlanService;
import com.titan.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final MembershipPlanService membershipPlanService;

    public PaymentController(PaymentService paymentService,
                             MemberService memberService,
                             MembershipPlanService membershipPlanService) {

        this.paymentService = paymentService;
        this.memberService = memberService;
        this.membershipPlanService = membershipPlanService;
    }

    @GetMapping
    public String viewPayments(Model model) {

        model.addAttribute("payments",
                paymentService.getAllPayments());

        return "payments";
    }

    @GetMapping("/add")
    public String addPaymentForm(Model model) {

        model.addAttribute("payment",
                new Payment());

        model.addAttribute("members",
                memberService.getAllMembers());

        model.addAttribute("plans",
                membershipPlanService.getAllPlans());

        return "payment-form";
    }

    @PostMapping("/save")
    @Transactional
    public String savePayment(@ModelAttribute Payment payment, RedirectAttributes redirectAttributes) {
        try {
            if (payment.getMemberId() == null || payment.getMembershipPlanId() == null) {
                throw new IllegalArgumentException("Choose a member and a membership plan.");
            }
            Member member = memberService.getMember(payment.getMemberId());
            MembershipPlan plan = membershipPlanService.getPlan(payment.getMembershipPlanId());
            if (member == null || plan == null) {
                throw new IllegalArgumentException("Choose a valid member and membership plan.");
            }
            payment.setMember(member);
            payment.setMembershipPlan(plan);
            paymentService.savePayment(payment);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("paymentError", exception.getMessage());
            return "redirect:/payments/add";
        }

        return "redirect:/payments";
    }

    @GetMapping("/edit/{id}")
    public String editPayment(@PathVariable Long id,
                              Model model) {

        Payment payment = paymentService.getPayment(id);
        if (payment == null) return "redirect:/payments";
        payment.setMemberId(payment.getMember().getId());
        payment.setMembershipPlanId(payment.getMembershipPlan().getId());
        model.addAttribute("payment", payment);

        model.addAttribute("members",
                memberService.getAllMembers());

        model.addAttribute("plans",
                membershipPlanService.getAllPlans());

        return "payment-form";
    }

    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {

        paymentService.deletePayment(id);

        return "redirect:/payments";
    }

}
