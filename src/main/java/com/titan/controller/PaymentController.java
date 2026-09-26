package com.titan.controller;

import com.titan.entity.Payment;
import com.titan.service.MemberService;
import com.titan.service.MembershipPlanService;
import com.titan.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String savePayment(@ModelAttribute Payment payment) {

        paymentService.savePayment(payment);

        return "redirect:/payments";
    }

    @GetMapping("/edit/{id}")
    public String editPayment(@PathVariable Long id,
                              Model model) {

        model.addAttribute("payment",
                paymentService.getPayment(id));

        model.addAttribute("members",
                memberService.getAllMembers());

        model.addAttribute("plans",
                membershipPlanService.getAllPlans());

        return "payment-form";
    }

    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {

        paymentService.deletePayment(id);

        return "redirect:/payments";
    }

}