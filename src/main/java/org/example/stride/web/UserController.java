package org.example.stride.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.stride.model.dto.UserRegisterDto;
import org.example.stride.model.enums.Gender;
import org.example.stride.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterPage(Model model){

        if (!model.containsAttribute("userRegisterDto")){
            model.addAttribute("userRegisterDto", new UserRegisterDto());
            model.addAttribute("genders", Gender.values());
        }

        return "register";
    }




    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRegisterDto") UserRegisterDto dto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("userRegisterDto", dto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegisterDto", bindingResult);
            return "redirect:/register";
        }
        boolean registered = userService.registerUser(dto);

        if (!registered) {
            redirectAttributes.addFlashAttribute("userRegisterDto", dto);
            redirectAttributes.addFlashAttribute("occupied", true);
            return "redirect:/register";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
        // TODO: Change a little the login html
    }
}
