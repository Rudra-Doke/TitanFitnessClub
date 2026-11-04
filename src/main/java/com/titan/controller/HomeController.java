package com.titan.controller;
import com.titan.service.MembershipPlanService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class HomeController {

    private final MembershipPlanService membershipPlanService;

    public HomeController(MembershipPlanService membershipPlanService) {
        this.membershipPlanService = membershipPlanService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("plans", membershipPlanService.getAllPlans());
        return "home";
    }
}
