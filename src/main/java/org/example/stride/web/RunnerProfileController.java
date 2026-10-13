package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.stride.model.dto.RunnerProfileDto;
import org.example.stride.model.enums.Day;
import org.example.stride.model.enums.ExperienceLevel;
import org.example.stride.service.RunnerProfileService;
import org.example.stride.service.session.CurrentUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@AllArgsConstructor
@Controller
public class RunnerProfileController {
    private final RunnerProfileService runnerProfileService;
    private final CurrentUserService currentUserService;

    @ModelAttribute("experienceLevels")
    public ExperienceLevel[] experienceLevels() {
        return ExperienceLevel.values();
    }

    @ModelAttribute("days")
    public Day[] days(){
        return Day.values();
    }

    @GetMapping("/profile/setup")
    public String viewProfileSetup(Model model, RedirectAttributes redirectAttributes){

        if (runnerProfileService.hasProfile(currentUserService.getCurrentUser().getId())){
            redirectAttributes.addFlashAttribute("info", "Вече имаш профил. Можеш да го редактираш");
            return "redirect:/profile";
        }

        model.addAttribute("runnerProfileDto", new RunnerProfileDto());

        return "profile-setup";
    }

    @PostMapping("/profile/setup")
    public String createProfile(@Valid @ModelAttribute("runnerProfileDto") RunnerProfileDto runnerProfileDto, BindingResult bindingResult){

        if (runnerProfileService.hasProfile(currentUserService.getCurrentUser().getId())) {
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()){
            return "profile-setup";
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
