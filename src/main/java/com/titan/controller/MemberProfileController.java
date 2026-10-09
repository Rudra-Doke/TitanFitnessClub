package com.titan.controller;

import com.titan.entity.Member;
import com.titan.service.MemberService;
import com.titan.repository.MemberRepository;
import com.titan.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberProfileController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberProfileController(MemberService memberService, MemberRepository memberRepository,
                                   UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/member/profile")
    public String profile(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberService.getMemberByUsername(username);

        if (member == null) {
            return "redirect:/login";
        }

        model.addAttribute("member", member);

        return "member/profile";
    }

    @PostMapping("/member/profile/update")
    public String updateProfile(Authentication authentication,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam(required = false, defaultValue = "") String address,
                                @RequestParam(required = false, defaultValue = "") String fitnessGoal,
                                RedirectAttributes redirectAttributes) {
        Member member = memberService.getMemberByUsername(authentication.getName());
        if (member == null) return "redirect:/login";
        fullName = fullName.trim();
        email = email.trim();
        phone = phone.trim();
        address = address.trim();
        fitnessGoal = fitnessGoal.trim();
        if (fullName.isBlank() || fullName.length() > 120
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || email.length() > 254
                || !phone.matches("^[+0-9(). -]{7,25}$") || address.length() > 500 || fitnessGoal.length() > 120) {
            redirectAttributes.addFlashAttribute("profileError", "Check your name, email, phone, and other details.");
            return "redirect:/member/profile";
        }
        if (memberRepository.existsByEmailIgnoreCaseAndIdNot(email, member.getId())) {
            redirectAttributes.addFlashAttribute("profileError", "That email address is already used by another account.");
            return "redirect:/member/profile";
        }
        member.setFullName(fullName);
        member.setEmail(email);
        member.setPhone(phone);
        member.setAddress(address);
        member.setFitnessGoal(fitnessGoal);
        memberRepository.save(member);
        redirectAttributes.addFlashAttribute("profileSuccess", "Your profile was updated.");
        return "redirect:/member/profile";
    }

    @PostMapping("/member/profile/password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        var account = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (account == null) return "redirect:/login";
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "Current password is incorrect.");
            return "redirect:/member/profile";
        }
        if (newPassword.length() < 10 || newPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            redirectAttributes.addFlashAttribute("passwordError", "Use a new password with at least 10 characters and no more than 72 bytes.");
            return "redirect:/member/profile";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "The new passwords do not match.");
            return "redirect:/member/profile";
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(account);
        redirectAttributes.addFlashAttribute("passwordSuccess", "Your password was changed.");
        return "redirect:/member/profile";
    }
}
