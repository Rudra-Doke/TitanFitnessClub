package com.titan.service;

import com.titan.repository.AttendanceRepository;
import com.titan.repository.MemberRepository;
import com.titan.repository.MembershipPlanRepository;
import com.titan.repository.PaymentRepository;
import com.titan.repository.TrainerRepository;
import com.titan.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final TrainerRepository trainerRepository;

    public DashboardService(
            UserRepository userRepository,
            MemberRepository memberRepository,
            MembershipPlanRepository membershipPlanRepository,
            AttendanceRepository attendanceRepository,
            PaymentRepository paymentRepository,
            TrainerRepository trainerRepository) {

        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.attendanceRepository = attendanceRepository;
        this.paymentRepository = paymentRepository;
        this.trainerRepository = trainerRepository;
    }

    public long getTotalMembers() {
        return memberRepository.count();
    }

    public long getTotalPlans() {
        return membershipPlanRepository.count();
    }

    public long getTotalAttendance() {
        return attendanceRepository.count();
    }

    public long getTotalTrainers() {
        return trainerRepository.count();
    }

    public long getTotalPayments() {
        return paymentRepository.count();
    }

    public Double getTotalRevenue() {
        return paymentRepository.getTotalRevenue();
    }
}