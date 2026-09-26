package com.titan.service;

import com.titan.entity.Trainer;
import com.titan.repository.TrainerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {

    private final TrainerRepository repository;

    public TrainerService(TrainerRepository repository) {
        this.repository = repository;
    }

    public List<Trainer> getAllTrainers() {
        return repository.findAll();
    }

    public Trainer saveTrainer(Trainer trainer) {
        return repository.save(trainer);
    }

    public Trainer getTrainer(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteTrainer(Long id) {
        repository.deleteById(id);
    }
}