package com.titan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberMembershipController {

    @GetMapping("/member/membership")
    public String membership() {
        return "member/membership";
    }
}