package com.titan.repository;

import com.titan.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {

    java.util.Optional<MembershipPlan> findByPlanNameIgnoreCase(String planName);

}
