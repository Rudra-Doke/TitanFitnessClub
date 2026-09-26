package com.titan.service;

import com.titan.entity.Attendance;
import com.titan.repository.AttendanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    public List<Attendance> getAllAttendance() {
        return repository.findAll();
    }

    public Attendance saveAttendance(Attendance attendance) {
        return repository.save(attendance);
    }

    public Attendance getAttendance(Long id) {
        return repository.findById(id).orElse(null);
    }

    public void deleteAttendance(Long id) {
        repository.deleteById(id);
    }

    public long getTotalAttendance() {
        return repository.count();
    }

}