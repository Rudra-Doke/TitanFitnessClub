package com.titan.service;

import com.titan.entity.Member;
import com.titan.repository.MemberRepository;
import com.titan.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    public MemberService(MemberRepository memberRepository, UserRepository userRepository) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(Long id) {
        memberRepository.findById(id).ifPresent(member -> {
            member.setStatus("Inactive");
            memberRepository.save(member);
            if (member.getUsername() != null) {
                userRepository.findByUsername(member.getUsername()).ifPresent(user -> {
                    user.setEnabled(false);
                    userRepository.save(user);
                });
            }
        });
    }

    public Member getMember(Long id) {
        return memberRepository.findById(id).orElse(null);
    }

    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username).orElse(null);
    }
}
