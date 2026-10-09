package com.titan.controller;

import com.titan.entity.Member;
import com.titan.entity.MembershipPlan;
import com.titan.entity.Payment;
import com.titan.entity.Role;
import com.titan.entity.User;
import com.titan.repository.UserRepository;
import com.titan.repository.MemberRepository;
import com.titan.service.MemberService;
import com.titan.service.MembershipPlanService;
import com.titan.service.PaymentService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class RegistrationController {

    private final MembershipPlanService membershipPlanService;
    private final MemberService memberService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(
            MembershipPlanService membershipPlanService,
            MemberService memberService,
            PaymentService paymentService,
            UserRepository userRepository,
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {

        this.membershipPlanService = membershipPlanService;
        this.memberService = memberService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute("plans",
                membershipPlanService.getAllPlans());

        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String processRegistration(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam String fitnessGoal,
            @RequestParam Long membershipPlan,
            RedirectAttributes redirectAttributes) {

        fullName = fullName.trim();
        username = username.trim();
        email = email.trim();
        phone = phone.trim();
        boolean validEmail = email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        boolean validPhone = phone.matches("^[+0-9(). -]{7,25}$");
        boolean validPassword = password != null && password.length() >= 10
                && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72;
        if (fullName.isBlank() || fullName.length() > 120
                || !username.matches("^[A-Za-z0-9_.-]{3,50}$") || !validEmail
                || !validPhone || !validPassword || age < 13 || age > 120
                || !java.util.Set.of("Male", "Female", "Other").contains(gender)
                || !java.util.Set.of("Weight Loss", "Muscle Gain", "General Fitness", "Bodybuilding").contains(fitnessGoal)) {
            redirectAttributes.addFlashAttribute("registrationError", "Please check your details and use a password with at least 10 characters.");
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("registrationError", "Passwords do not match.");
            return "redirect:/register";
        }

        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("registrationError", "That username is already in use.");
            return "redirect:/register";
        }
        if (memberRepository.existsByEmailIgnoreCase(email)) {
            redirectAttributes.addFlashAttribute("registrationError", "An account already exists for that email address.");
            return "redirect:/register";
        }

        MembershipPlan plan =
                membershipPlanService.getPlan(membershipPlan);

        if (plan == null || plan.getPrice() == null || plan.getPrice() <= 0
                || "inactive".equalsIgnoreCase(plan.getStatus())) {
            redirectAttributes.addFlashAttribute("registrationError", "Please choose a valid membership plan.");
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

        member.setMemberId("MEM" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        member.setFullName(fullName);
        member.setUsername(username);
        member.setEmail(email);
        member.setPhone(phone);
        member.setAge(age);
        member.setGender(gender);
        member.setFitnessGoal(fitnessGoal);
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
        payment.setPaymentStatus("Pending");

        paymentService.savePayment(payment);

        redirectAttributes.addFlashAttribute("registrationSuccess", "Your account was created. Sign in to complete your first membership payment from the Payments or Membership page.");
        return "redirect:/login";
    }
}
