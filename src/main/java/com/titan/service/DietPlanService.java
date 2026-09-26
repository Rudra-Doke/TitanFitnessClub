package com.titan.service;

import com.titan.entity.DietPlan;
import com.titan.repository.DietPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DietPlanService {

    private final DietPlanRepository dietPlanRepository;

    public DietPlanService(DietPlanRepository dietPlanRepository) {
        this.dietPlanRepository = dietPlanRepository;
    }

    public List<DietPlan> getAllDietPlans() {
        return dietPlanRepository.findAll();
    }

    public DietPlan saveDietPlan(DietPlan dietPlan) {
        return dietPlanRepository.save(dietPlan);
    }

    public DietPlan getDietPlan(Long id) {
        return dietPlanRepository.findById(id).orElse(null);
    }

    public void deleteDietPlan(Long id) {
        dietPlanRepository.deleteById(id);
    }

    public List<DietPlan> getDietPlansByMember(Long memberId) {
        return dietPlanRepository.findByMemberId(memberId);
    }
}