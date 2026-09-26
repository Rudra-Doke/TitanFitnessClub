package com.titan.controller;

import com.titan.entity.Member;
import com.titan.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ===== MEMBER DASHBOARD =====
    @GetMapping("/member/dashboard")
    public String memberDashboard() {
        return "member/dashboard";
    }

    // ===== ADMIN MEMBER MANAGEMENT =====
    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "members";
    }

    @GetMapping("/members/add")
    public String addMemberForm(Model model) {
        model.addAttribute("member", new Member());
        return "member-form";
    }

    @PostMapping("/members/save")
    public String saveMember(@ModelAttribute Member member) {
        memberService.saveMember(member);
        return "redirect:/members";
    }

    @GetMapping("/members/edit/{id}")
    public String editMember(@PathVariable Long id, Model model) {
        Member member = memberService.getMember(id);
        model.addAttribute("member", member);
        return "member-form";
    }

    @GetMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return "redirect:/members";
    }
}