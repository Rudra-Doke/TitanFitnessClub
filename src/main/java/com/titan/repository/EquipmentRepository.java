package com.titan.repository;

import com.titan.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository
        extends JpaRepository<Equipment, Long> {
}