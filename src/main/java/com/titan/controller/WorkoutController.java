package com.titan.controller;

import com.titan.entity.Member;
import com.titan.entity.Workout;
import com.titan.repository.MemberRepository;
import com.titan.repository.WorkoutRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workout-plans")
public class WorkoutController {

    private final WorkoutRepository workoutRepository;
    private final MemberRepository memberRepository;

    public WorkoutController(WorkoutRepository workoutRepository,
                             MemberRepository memberRepository) {
        this.workoutRepository = workoutRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public String workoutPlans(Model model) {

        model.addAttribute("workouts", workoutRepository.findAll());
        model.addAttribute("members", memberRepository.findAll());

        return "workout-plans";
    }

    @PostMapping("/save")
    public String saveWorkout(
            @RequestParam Long memberId,
            @RequestParam String day,
            @RequestParam String exercise,
            @RequestParam String sets,
            @RequestParam String reps,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            redirectAttributes.addFlashAttribute("formError", "Choose a valid member before saving the workout plan.");
            return "redirect:/workout-plans";
        }

        Workout workout = new Workout();
        workout.setMember(member);
        workout.setDay(day);
        workout.setExercise(exercise);
        workout.setSets(sets);
        workout.setReps(reps);
        workout.setNotes(notes);

        workoutRepository.save(workout);

        return "redirect:/workout-plans";
    }

    @PostMapping("/delete/{id}")
    public String deleteWorkout(@PathVariable Long id) {

        workoutRepository.deleteById(id);

        return "redirect:/workout-plans";
    }
}
