package com.titan.controller;

import com.titan.entity.Member;
import com.titan.entity.Workout;
import com.titan.repository.MemberRepository;
import com.titan.repository.WorkoutRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MemberWorkoutController {

    private final MemberRepository memberRepository;
    private final WorkoutRepository workoutRepository;

    public MemberWorkoutController(MemberRepository memberRepository,
                                   WorkoutRepository workoutRepository) {
        this.memberRepository = memberRepository;
        this.workoutRepository = workoutRepository;
    }

    @GetMapping("/member/workout")
    public String workout(Authentication authentication, Model model) {

        String username = authentication.getName();

        Member member = memberRepository.findByUsername(username)
                .orElse(null);
        if (member == null) return "redirect:/member/profile";

        List<Workout> workouts =
                workoutRepository.findByMemberId(member.getId());

        model.addAttribute("workouts", workouts);

        return "member/workout";
    }
}
