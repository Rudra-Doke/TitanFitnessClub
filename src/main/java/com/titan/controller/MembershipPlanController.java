package com.titan.controller;

import com.titan.entity.MembershipPlan;
import com.titan.service.MembershipPlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/plans")
public class MembershipPlanController {

    private final MembershipPlanService service;

    public MembershipPlanController(MembershipPlanService service) {
        this.service = service;
    }

    @GetMapping
    public String viewPlans(Model model) {
        model.addAttribute("plans", service.getAllPlans());
        return "plans";
    }

    @GetMapping("/add")
    public String addPlanForm(Model model) {
        model.addAttribute("plan", new MembershipPlan());
        return "plan-form";
    }

    @PostMapping("/save")
    public String savePlan(@ModelAttribute MembershipPlan plan) {
        service.savePlan(plan);
        return "redirect:/plans";
    }

    @GetMapping("/edit/{id}")
    public String editPlan(@PathVariable Long id, Model model) {
        model.addAttribute("plan", service.getPlan(id));
        return "plan-form";
    }

    @PostMapping("/delete/{id}")
    public String deletePlan(@PathVariable Long id) {
        service.deletePlan(id);
        return "redirect:/plans";
    }
}
