package com.titan.controller;

import com.titan.entity.Attendance;
import com.titan.service.AttendanceService;
import com.titan.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberService memberService;

    public AttendanceController(AttendanceService attendanceService,
                                MemberService memberService) {

        this.attendanceService = attendanceService;
        this.memberService = memberService;
    }

    @GetMapping
    public String viewAttendance(Model model) {

        model.addAttribute("attendanceList",
                attendanceService.getAllAttendance());

        return "attendance";
    }

    @GetMapping("/add")
    public String addAttendance(Model model) {

        model.addAttribute("attendance",
                new Attendance());

        model.addAttribute("members",
                memberService.getAllMembers());

        return "attendance-form";
    }

    @PostMapping("/save")
    public String saveAttendance(@ModelAttribute Attendance attendance) {

        attendanceService.saveAttendance(attendance);

        return "redirect:/attendance";
    }

    @GetMapping("/delete/{id}")
    public String deleteAttendance(@PathVariable Long id) {

        attendanceService.deleteAttendance(id);

        return "redirect:/attendance";
    }

}