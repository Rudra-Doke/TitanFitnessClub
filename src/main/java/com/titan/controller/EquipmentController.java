package com.titan.controller;

import com.titan.entity.Equipment;
import com.titan.service.EquipmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService service;

    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    @GetMapping
    public String viewEquipment(Model model) {

        model.addAttribute("equipmentList",
                service.getAllEquipment());

        return "equipment";
    }

    @GetMapping("/add")
    public String addEquipmentForm(Model model) {

        model.addAttribute("equipment",
                new Equipment());

        return "equipment-form";
    }

    @PostMapping("/save")
    public String saveEquipment(@ModelAttribute Equipment equipment) {

        service.saveEquipment(equipment);

        return "redirect:/equipment";
    }

    @GetMapping("/edit/{id}")
    public String editEquipment(@PathVariable Long id,
                                Model model) {

        model.addAttribute("equipment",
                service.getEquipment(id));

        return "equipment-form";
    }

    @PostMapping("/delete/{id}")
    public String deleteEquipment(@PathVariable Long id) {

        service.deleteEquipment(id);

        return "redirect:/equipment";
    }

}
