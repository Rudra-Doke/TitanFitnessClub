package com.titan.repository;

import com.titan.entity.Attendance;
import com.titan.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByMemberOrderByAttendanceDateDesc(Member member);

}