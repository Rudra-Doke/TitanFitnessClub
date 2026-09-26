package com.titan.controller;

import com.titan.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("totalMembers",
                dashboardService.getTotalMembers());

        model.addAttribute("totalPlans",
                dashboardService.getTotalPlans());

        model.addAttribute("totalAttendance",
                dashboardService.getTotalAttendance());

        model.addAttribute("totalTrainers",
                dashboardService.getTotalTrainers());

        model.addAttribute("totalPayments",
                dashboardService.getTotalPayments());

        model.addAttribute("totalRevenue",
                dashboardService.getTotalRevenue());

        return "dashboard";
    }
}