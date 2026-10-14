package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stride.model.dto.GoalCreationDto;
import org.example.stride.service.GoalService;
import org.example.stride.service.RunnerProfileService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;
    private final RunnerProfileService runnerProfileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/goals/add")
    public String viewGoalSetup(Model model){

        if (!runnerProfileService.hasProfile(currentUserService.getCurrentUser().getId())){
            return "redirect:/profile/setup";
        }

        model.addAttribute("goalCreationDto", new GoalCreationDto());

        return "goal-new";
    }

    @PostMapping("/goals/add")
    public String createGoal(@Valid @ModelAttribute GoalCreationDto goalCreationDto, BindingResult bindingResult){
        if (!runnerProfileService.hasProfile(currentUserService.getCurrentUser().getId())){
            return "redirect:/profile/setup";
        }

        if (bindingResult.hasErrors()){
            return "goal-new";
        }

        goalService.createGoal(goalCreationDto);

        return "redirect:/";
    }
}

//TODO: Препраща към /login след изпращане на формата. Не записва в базата