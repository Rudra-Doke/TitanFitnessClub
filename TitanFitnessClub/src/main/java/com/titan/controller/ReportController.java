package com.titan.controller;

import com.titan.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports(Model model) {

        model.addAttribute("totalMembers", reportService.totalMembers());
        model.addAttribute("totalTrainers", reportService.totalTrainers());
        model.addAttribute("totalPlans", reportService.totalPlans());
        model.addAttribute("totalAttendance", reportService.totalAttendance());
        model.addAttribute("totalEquipment", reportService.totalEquipment());
        model.addAttribute("totalPayments", reportService.totalPayments());

        model.addAttribute("activePage", "reports");
        model.addAttribute("totalRevenue", reportService.totalRevenue());

        return "reports";
    }
}