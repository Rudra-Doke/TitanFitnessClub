package com.titan.service;

import com.titan.entity.Equipment;
import com.titan.repository.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {

    private final EquipmentRepository repository;

    public EquipmentService(EquipmentRepository repository) {
        this.repository = repository;
    }

    public List<Equipment> getAllEquipment() {
        return repository.findAll();
    }

    public Equipment saveEquipment(Equipment equipment) {
        return repository.save(equipment);
    }

    public Equipment getEquipment(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteEquipment(Long id) {
        repository.deleteById(id);
    }

    public long getTotalEquipment() {
        return repository.count();
    }
}