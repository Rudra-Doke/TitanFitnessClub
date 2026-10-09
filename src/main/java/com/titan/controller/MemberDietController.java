package com.titan.controller;

import com.titan.entity.DietPlan;
import com.titan.entity.Member;
import com.titan.repository.DietPlanRepository;
import com.titan.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MemberDietController {

    private final MemberRepository memberRepository;
    private final DietPlanRepository dietPlanRepository;

    public MemberDietController(MemberRepository memberRepository,
                                DietPlanRepository dietPlanRepository) {
        this.memberRepository = memberRepository;
        this.dietPlanRepository = dietPlanRepository;
    }

    @GetMapping("/member/diet")
    public String diet(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberRepository.findByUsername(username)
                .orElse(null);
        if (member == null) return "redirect:/member/profile";

        List<DietPlan> dietPlans =
                dietPlanRepository.findByMemberId(member.getId());

        model.addAttribute("dietPlans", dietPlans);

        return "member/diet";
    }
}
