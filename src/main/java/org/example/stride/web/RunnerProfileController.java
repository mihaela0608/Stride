package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.stride.model.dto.RunnerProfileDto;
import org.example.stride.model.enums.ExperienceLevel;
import org.example.stride.service.RunnerProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@AllArgsConstructor
@Controller
public class RunnerProfileController {
    private final RunnerProfileService runnerProfileService;

    @ModelAttribute("experienceLevels")
    public ExperienceLevel[] experienceLevels() {
        return ExperienceLevel.values();
    }

    @GetMapping("/profile/setup")
    public String viewProfileSetup(Model model){
        model.addAttribute("runnerProfileDto", new RunnerProfileDto());

        return "profile-setup";
    }

    @PostMapping("/profile/setup")
    public String createProfile(@Valid @ModelAttribute("runnerProfileDto") RunnerProfileDto runnerProfileDto, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            return "/profile/setup";
        }

        boolean created = runnerProfileService.createRunnerProfile(runnerProfileDto);

        if (!created) {
            bindingResult.reject(
                    "profile.exists",
                    "Вече имаш създаден профил."
            );

            return "profile-setup";
        }

        return "redirect:/profile";
    }
}

// TODO: There is an error with creating runner profile
