package com.titan.controller;

import com.titan.entity.Member;
import com.titan.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class MemberViewModelAdvice {
    private final MemberRepository memberRepository;

    public MemberViewModelAdvice(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @ModelAttribute("currentMember")
    public Member currentMember(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_MEMBER".equals(a.getAuthority()))) {
            return null;
        }
        return memberRepository.findByUsername(authentication.getName()).orElse(null);
    }
}
