package com.titan.controller;

import com.titan.entity.Member;
import com.titan.service.MemberService;
import com.titan.service.MembershipPlanService;
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
    private final MembershipPlanService membershipPlanService;

    public MemberController(MemberService memberService, MemberRepository memberRepository,
                            AttendanceRepository attendanceRepository, PaymentRepository paymentRepository,
                            WorkoutRepository workoutRepository, MembershipPlanService membershipPlanService) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.workoutRepository = workoutRepository;
        this.membershipPlanService = membershipPlanService;
    }

    // ===== MEMBER DASHBOARD =====
    @GetMapping("/member/dashboard")
    public String memberDashboard(Authentication authentication, Model model) {
        Member member = memberRepository.findByUsername(authentication.getName()).orElse(null);
        if (member == null) return "redirect:/login";
        model.addAttribute("member", member);
        String planName = paymentRepository.findFirstByMemberAndPaymentStatusIgnoreCaseOrderByPaymentDateDesc(member, "Paid")
                .map(payment -> payment.getMembershipPlan().getPlanName()).orElse(member.getMembershipPlan());
        model.addAttribute("planName", planName == null || planName.isBlank() ? "No plan assigned" : planName);
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
    public String members(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("members", memberService.searchMembers(q));
        model.addAttribute("searchQuery", q == null ? "" : q);
        return "members";
    }

    @GetMapping("/members/add")
    public String addMemberForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("plans", membershipPlanService.getAllPlans());
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
        if (member == null) return "redirect:/members";
        model.addAttribute("member", member);
        model.addAttribute("plans", membershipPlanService.getAllPlans());
        return "member-form";
    }

    @PostMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return "redirect:/members";
    }
}
