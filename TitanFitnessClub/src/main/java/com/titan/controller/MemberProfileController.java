package com.titan.controller;

import com.titan.entity.Member;
import com.titan.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberProfileController {

    private final MemberRepository memberRepository;

    public MemberProfileController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/member/profile")
    public String profile(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberRepository
                .findByUsername(username)
                .orElse(null);
        model.addAttribute("member", member);

        return "member/profile";
    }
}