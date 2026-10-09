package com.titan.controller;

import com.titan.entity.DietPlan;
import com.titan.entity.Member;
import com.titan.repository.MemberRepository;
import com.titan.service.DietPlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/diet-plans")
public class DietPlanController {

    private final DietPlanService dietPlanService;
    private final MemberRepository memberRepository;

    public DietPlanController(DietPlanService dietPlanService,
                              MemberRepository memberRepository) {
        this.dietPlanService = dietPlanService;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public String dietPlans(Model model) {

        model.addAttribute("dietPlans",
                dietPlanService.getAllDietPlans());

        model.addAttribute("members",
                memberRepository.findAll());

        return "diet-plans";
    }

    @PostMapping("/save")
    public String saveDietPlan(
            @RequestParam Long memberId,
            @RequestParam String meal,
            @RequestParam String food,
            @RequestParam(required = false) String quantity,
            @RequestParam(required = false) String calories,
            @RequestParam(required = false) String protein,
            @RequestParam(required = false) String notes) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        DietPlan dietPlan = new DietPlan();

        dietPlan.setMember(member);
        dietPlan.setMeal(meal);
        dietPlan.setFood(food);
        dietPlan.setQuantity(quantity);
        dietPlan.setCalories(calories);
        dietPlan.setProtein(protein);
        dietPlan.setNotes(notes);

        dietPlanService.saveDietPlan(dietPlan);

        return "redirect:/diet-plans";
    }

    @PostMapping("/delete/{id}")
    public String deleteDietPlan(@PathVariable Long id) {

        dietPlanService.deleteDietPlan(id);

        return "redirect:/diet-plans";
    }
}
