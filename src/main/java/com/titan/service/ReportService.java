package com.titan.service;

import com.titan.repository.AttendanceRepository;
import com.titan.repository.EquipmentRepository;
import com.titan.repository.MemberRepository;
import com.titan.repository.MembershipPlanRepository;
import com.titan.repository.PaymentRepository;
import com.titan.repository.TrainerRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final AttendanceRepository attendanceRepository;
    private final EquipmentRepository equipmentRepository;
    private final PaymentRepository paymentRepository;

    public ReportService(MemberRepository memberRepository,
                         TrainerRepository trainerRepository,
                         MembershipPlanRepository membershipPlanRepository,
                         AttendanceRepository attendanceRepository,
                         EquipmentRepository equipmentRepository,
                         PaymentRepository paymentRepository) {

        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.attendanceRepository = attendanceRepository;
        this.equipmentRepository = equipmentRepository;
        this.paymentRepository = paymentRepository;
    }

    public long totalMembers() {
        return memberRepository.count();
    }

    public long totalTrainers() {
        return trainerRepository.count();
    }

    public long totalPlans() {
        return membershipPlanRepository.count();
    }

    public long totalAttendance() {
        return attendanceRepository.count();
    }

    public long totalEquipment() {
        return equipmentRepository.count();
    }

    public long totalPayments() {
        return paymentRepository.count();
    }

    public Double totalRevenue() {
        return paymentRepository.getTotalRevenue();
    }
}