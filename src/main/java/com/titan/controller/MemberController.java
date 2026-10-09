package com.titan.controller;

import com.titan.entity.Member;
import com.titan.service.MemberService;
import com.titan.repository.MemberRepository;
import com.titan.repository.AttendanceRepository;
import com.titan.repository.PaymentRepository;
import com.titan.repository.WorkoutRepository;
import com.titan.entity.Payment;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final WorkoutRepository workoutRepository;

    public MemberController(MemberService memberService, MemberRepository memberRepository,
                            AttendanceRepository attendanceRepository, PaymentRepository paymentRepository,
                            WorkoutRepository workoutRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.workoutRepository = workoutRepository;
    }

    // ===== MEMBER DASHBOARD =====
    @GetMapping("/member/dashboard")
    public String memberDashboard(Authentication authentication, Model model) {
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        if (member == null) return "redirect:/login";
        model.addAttribute("member", member);
        model.addAttribute("planName", member.getMembershipPlan() == null || member.getMembershipPlan().isBlank() ? "No plan assigned" : member.getMembershipPlan());
        model.addAttribute("attendanceToday", attendanceRepository.findByMemberOrderByAttendanceDateDesc(member).stream()
                .filter(a -> LocalDate.now().equals(a.getAttendanceDate())).findFirst()
                .map(a -> a.getStatus()).orElse("No attendance recorded today"));
        model.addAttribute("latestPaymentStatus", paymentRepository.findByMemberOrderByPaymentDateDesc(member).stream()
                .findFirst().map(Payment::getPaymentStatus).orElse("No payments recorded"));
        model.addAttribute("workoutCount", workoutRepository.findByMemberId(member.getId()).size());
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

    @PostMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return "redirect:/members";
    }
}
