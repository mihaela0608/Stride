package org.example.stride.web;

import lombok.AllArgsConstructor;
import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.model.enums.Gender;
import org.example.stride.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterPage(Model model){

        model.addAttribute("userRegisterDto", new UserRegisterDto());
        model.addAttribute("genders", Gender.values());

        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("userRegisterDto") UserRegisterDto dto) {

        boolean registered = userService.registerUser(dto);

        if (!registered) {
            return "redirect:/register?emailExists";
        }

        return "redirect:/login?registered";
    }
}
