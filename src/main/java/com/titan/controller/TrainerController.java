package com.titan.controller;

import com.titan.entity.Trainer;
import com.titan.service.TrainerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/trainers")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @GetMapping
    public String viewTrainers(Model model) {

        model.addAttribute("trainers", service.getAllTrainers());

        return "trainers";
    }

    @GetMapping("/add")
    public String addTrainerForm(Model model) {

        model.addAttribute("trainer", new Trainer());

        return "trainer-form";
    }

    @PostMapping("/save")
    public String saveTrainer(@ModelAttribute Trainer trainer) {

        service.saveTrainer(trainer);

        return "redirect:/trainers";
    }

    @GetMapping("/edit/{id}")
    public String editTrainer(@PathVariable Long id,
                              Model model) {

        model.addAttribute("trainer",
                service.getTrainer(id));

        return "trainer-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteTrainer(@PathVariable Long id) {

        service.deleteTrainer(id);

        return "redirect:/trainers";
    }
}