package com.titan.repository;

import com.titan.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    java.util.List<Member> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrMemberIdContainingIgnoreCase(
            String fullName, String email, String memberId);

}
