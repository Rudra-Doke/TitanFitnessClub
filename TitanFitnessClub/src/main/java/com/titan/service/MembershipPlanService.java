package com.titan.service;

import com.titan.entity.MembershipPlan;
import com.titan.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipPlanService {

    private final MembershipPlanRepository repository;

    public MembershipPlanService(MembershipPlanRepository repository) {
        this.repository = repository;
    }

    public List<MembershipPlan> getAllPlans() {
        return repository.findAll();
    }

    public MembershipPlan savePlan(MembershipPlan plan) {
        return repository.save(plan);
    }

    public MembershipPlan getPlan(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deletePlan(Long id) {
        repository.deleteById(id);
    }
}