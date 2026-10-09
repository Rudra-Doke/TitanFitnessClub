package com.titan.controller;

import com.titan.entity.Member;
import com.titan.service.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberProfileController {

    private final MemberService memberService;

    public MemberProfileController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member/profile")
    public String profile(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberService.getMemberByUsername(username);

        if (member == null) {
            return "redirect:/member/dashboard";
        }

        model.addAttribute("member", member);

        return "member/profile";
    }
}
