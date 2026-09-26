package com.titan.controller;

import com.titan.entity.Attendance;
import com.titan.entity.Member;
import com.titan.repository.AttendanceRepository;
import com.titan.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MemberAttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    public MemberAttendanceController(
            AttendanceRepository attendanceRepository,
            MemberRepository memberRepository) {

        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/member/attendance")
    public String attendance(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Attendance> attendanceList =
                attendanceRepository.findByMemberOrderByAttendanceDateDesc(member);

        long presentDays = attendanceList.stream()
                .filter(a -> "Present".equalsIgnoreCase(a.getStatus()))
                .count();

        long absentDays = attendanceList.stream()
                .filter(a -> "Absent".equalsIgnoreCase(a.getStatus()))
                .count();

        long totalDays = presentDays + absentDays;

        long attendancePercentage = totalDays == 0
                ? 0
                : Math.round((presentDays * 100.0) / totalDays);

        model.addAttribute("attendanceList", attendanceList);
        model.addAttribute("presentDays", presentDays);
        model.addAttribute("absentDays", absentDays);
        model.addAttribute("attendancePercentage", attendancePercentage);

        return "member/attendance";
    }
}