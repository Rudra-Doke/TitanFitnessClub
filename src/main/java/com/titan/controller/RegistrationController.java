package com.titan.controller;

import com.titan.entity.Member;
import com.titan.entity.MembershipPlan;
import com.titan.entity.Payment;
import com.titan.entity.Role;
import com.titan.entity.User;
import com.titan.repository.UserRepository;
import com.titan.service.MemberService;
import com.titan.service.MembershipPlanService;
import com.titan.service.PaymentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class RegistrationController {

    private final MembershipPlanService membershipPlanService;
    private final MemberService memberService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(
            MembershipPlanService membershipPlanService,
            MemberService memberService,
            PaymentService paymentService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.membershipPlanService = membershipPlanService;
        this.memberService = memberService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute("plans",
                membershipPlanService.getAllPlans());

        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam Long membershipPlan) {

        if (!password.equals(confirmPassword)) {
            return "redirect:/register";
        }

        if (userRepository.existsByUsername(username)) {
            return "redirect:/register";
        }

        MembershipPlan plan =
                membershipPlanService.getPlan(membershipPlan);

        if (plan == null) {
            return "redirect:/register";
        }

        // Create login account
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.MEMBER);
        user.setEnabled(true);

        userRepository.save(user);

        // Create member
        Member member = new Member();

        member.setMemberId("MEM" + System.currentTimeMillis());
        member.setFullName(fullName);
        member.setUsername(username);
        member.setEmail(email);
        member.setPhone(phone);
        member.setAge(age);
        member.setGender(gender);
        member.setJoinDate(LocalDate.now());
        member.setMembershipPlan(plan.getPlanName());
        member.setStatus("Active");

        memberService.saveMember(member);

        // Create payment automatically
        Payment payment = new Payment();
        payment.setMember(member);
        payment.setMembershipPlan(plan);
        payment.setAmount(plan.getPrice());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("Registration");
        payment.setPaymentStatus("Paid");

        paymentService.savePayment(payment);

        return "redirect:/login";
    }
}